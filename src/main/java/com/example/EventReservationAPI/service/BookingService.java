package com.example.EventReservationAPI.service;

import com.example.EventReservationAPI.dto.request.BookingRequest;
import com.example.EventReservationAPI.dto.response.BookingResponse;
import com.example.EventReservationAPI.entity.*;
import com.example.EventReservationAPI.exception.BusinessException;
import com.example.EventReservationAPI.exception.ResourceNotFoundException;
import com.example.EventReservationAPI.repository.BookingRepository;
import com.example.EventReservationAPI.repository.EventRepository;
import com.example.EventReservationAPI.repository.SeatRepository;
import com.example.EventReservationAPI.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponse bookSeat(BookingRequest bookingRequest , String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(bookingRequest.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        EventStatus eventStatus = event.getStatus();

        if(eventStatus != EventStatus.UPCOMING){
            throw new BusinessException("Event has completed or ongoing");
        }

        Seat seat = seatRepository.findByIdWithLock(bookingRequest.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));

        SeatStatus seatStatus = seat.getStatus();

        if(!seat.getEvent().getId().equals(event.getId())){
            throw new BusinessException("Seat does not belong to this Event");
        }

        if(seatStatus != SeatStatus.AVAILABLE){
            throw new BusinessException("Seat is already Booked");
        }

        boolean alreadyBooked = bookingRepository
                .existsByUserIdAndSeatId(user.getId() , seat.getId());

        if(alreadyBooked){
            throw new BusinessException("You have already booked this seat");
        }

        seat.setStatus(SeatStatus.BOOKED);
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
