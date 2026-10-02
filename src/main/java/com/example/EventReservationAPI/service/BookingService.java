package com.example.EventReservationAPI.service;

import com.example.EventReservationAPI.dto.request.BookingRequest;
import com.example.EventReservationAPI.dto.request.HoldSeatRequest;
import com.example.EventReservationAPI.dto.response.BookingResponse;
import com.example.EventReservationAPI.dto.response.HoldSeatResponse;
import com.example.EventReservationAPI.entity.*;
import com.example.EventReservationAPI.exception.BusinessException;
import com.example.EventReservationAPI.exception.ResourceNotFoundException;
import com.example.EventReservationAPI.exception.SeatNotAvailableException;
import com.example.EventReservationAPI.exception.UnauthorizedException;
import com.example.EventReservationAPI.repository.BookingRepository;
import com.example.EventReservationAPI.repository.EventRepository;
import com.example.EventReservationAPI.repository.SeatRepository;
import com.example.EventReservationAPI.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final StringRedisTemplate stringRedisTemplate;
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @Transactional
    @CacheEvict(value = "events", key = "#bookingRequest.eventId")
    public BookingResponse bookSeat(BookingRequest bookingRequest , String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(bookingRequest.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        EventStatus eventStatus = event.getStatus();

        if(eventStatus != EventStatus.UPCOMING){
            throw new BusinessException("Event has completed or ongoing");
        }

        String rediKey = "seat_hold" + ":" + bookingRequest.getEventId() + ":" + bookingRequest.getSeatId();

        String storedKeyValue = stringRedisTemplate.opsForValue().get(rediKey);

        if(storedKeyValue == null){
            throw new ResourceNotFoundException("Hold expired. Reselect seat.");
        }

        if(!userEmail.equalsIgnoreCase(storedKeyValue)){
            throw new UnauthorizedException("You do not own this seat");
        }

        Seat seat = seatRepository.findByIdWithLock(bookingRequest.getSeatId())
                .orElseThrow(() -> new SeatNotAvailableException("Seat not found"));

        SeatStatus seatStatus = seat.getStatus();

        if(!seat.getEvent().getId().equals(event.getId())){
            throw new BusinessException("Seat does not belong to this Event");
        }

        if(seat.getStatus() != SeatStatus.HELD){
            throw new SeatNotAvailableException("Seat is already " + seat.getStatus() + ". Cannot confirm booking.");
        }

        seat.setStatus(SeatStatus.BOOKED);
        seat.setHeldAt(null);
        seatRepository.save(seat);

        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .status(BookingStatus.CONFIRMED)
                .totalAmount(event.getPrice())
                .user(user)
                .event(event)
                .seat(seat)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        stringRedisTemplate.delete(rediKey);
        return mapToResponse(savedBooking);

    }

    public Page<BookingResponse> getMyBookings(String userEmail,int page,int size){
        Pageable pageable = PageRequest.of(page,size);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Booking> pageBooking  = bookingRepository.findByUserId(user.getId() , pageable);

        return pageBooking.map(this::mapToResponse);

    }

    public BookingResponse getBookingById(Long id , String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking booking = bookingRepository.findByIdAndUserId(id,user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking with Id - " + id + " not found"));

        return mapToResponse(booking);
    }

    @Transactional
    public void cancelBooking(Long id , String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Booking booking = bookingRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking not found with id: " + id));

        if(booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException(
                    "Booking is already cancelled");

        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);


        Seat seat = booking.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);

    }

    public HoldSeatResponse holdSeat(HoldSeatRequest holdSeatRequest , String userEmail){

        // 1. Atomic Redis gatekeeper FIRST - 49 out of 50 stop here in RAM (< 1ms)
        String redisKey = "seat_hold" + ":" + holdSeatRequest.getEventId() + ":" + holdSeatRequest.getSeatId();

        Boolean lockAcquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey,userEmail,10, TimeUnit.MINUTES);

        if(Boolean.FALSE.equals(lockAcquired)){
            throw new SeatNotAvailableException("Seat is already held by another user.");
        }

        // 2. Only the 1 winning thread hits the database
        Seat seat;
        try {
            seat = seatRepository.findById(holdSeatRequest.getSeatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found with Id " + holdSeatRequest.getSeatId()));

            if (!seat.getEvent().getId().equals(holdSeatRequest.getEventId())) {
                throw new IllegalArgumentException("Seat does not belong to the specified event");
            }

            // If seat was already booked in DB, rollback Redis hold and reject
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatNotAvailableException("Seat is already " + seat.getStatus());
            }

            seat.setStatus(SeatStatus.HELD);
            seat.setHeldAt(LocalDateTime.now());
            seatRepository.save(seat);

        } catch (Exception ex) {
            // Roll back the Redis hold if DB verification or save fails
            stringRedisTemplate.delete(redisKey);
            throw ex;
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        return HoldSeatResponse.builder()
                .eventId(holdSeatRequest.getEventId())
                .seatId(holdSeatRequest.getSeatId())
                .holdToken(userEmail)
                .expiresAt(expiresAt)
                .message("Seat is held. Please make payment to book")
                .build();
    }

    public void releaseHold(Long id , String userEmail){
        Seat seat  = seatRepository.findById(id)
                .orElseThrow(() -> new SeatNotAvailableException("Seat not found"));


        String rediKey = "seat_hold" + ":" + seat.getEvent().getId() + ":" + seat.getId();

        String storedKeyValue = stringRedisTemplate.opsForValue().get(rediKey);

        if(storedKeyValue == null){
            if (seat.getStatus() == SeatStatus.HELD) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }
            return;
        }

        if(!userEmail.equalsIgnoreCase(storedKeyValue)){
            throw new UnauthorizedException("You do not own this seat");
        }

        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setHeldAt(null);
        seatRepository.save(seat);
        stringRedisTemplate.delete(rediKey);

    }

    private String generateBookingReference(){
        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID()
                .toString()
                .substring(0,6)
                .toUpperCase();

        return "BK-" + date + "-" + random;
    }

    private BookingResponse mapToResponse(Booking booking){

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .bookedAt(booking.getBookedAt())
                .eventTitle(booking.getEvent().getTitle())
                .seatNumber(booking.getSeat().getSeatNumber())
                .userName(booking.getUser().getName())
                .userEmail(booking.getUser().getEmail())
                .build();
    }
}
