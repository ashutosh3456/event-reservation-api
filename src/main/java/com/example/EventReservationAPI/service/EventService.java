package com.example.EventReservationAPI.service;

import com.example.EventReservationAPI.dto.request.EventRequest;
import com.example.EventReservationAPI.dto.response.EventResponse;
import com.example.EventReservationAPI.entity.*;
import com.example.EventReservationAPI.exception.BusinessException;
import com.example.EventReservationAPI.exception.ResourceNotFoundException;
import com.example.EventReservationAPI.repository.EventRepository;
import com.example.EventReservationAPI.repository.SeatRepository;
import com.example.EventReservationAPI.repository.UserRepository;
import com.example.EventReservationAPI.repository.VenueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final VenueRepository venueRepository;

    @Transactional
    public EventResponse createEvent(EventRequest eventRequest , String userEmail){

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        Venue venue = venueRepository.findById(eventRequest.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Wrong venue Id"));

        Event event = Event.builder()
                .title(eventRequest.getTitle())
                .description(eventRequest.getDescription())
                .category(eventRequest.getCategory())
                .city(eventRequest.getCity())
                .eventDate(eventRequest.getEventDate())
                .price(eventRequest.getPrice())
                .venue(venue)
                .organizer(user)
                .build();

        event = eventRepository.save(event);

        List<Seat> seats = new ArrayList<>();
        for(int i = 1; i <= venue.getTotalCapacity(); i++) {
            Seat seat = Seat.builder()
                    .seatNumber("SEAT-" + i)
                    .status(SeatStatus.AVAILABLE)
                    .event(event)
                    .build();
            seats.add(seat);
        }
        seatRepository.saveAll(seats);

        return mapToResponse(event);
    }

    public Page<EventResponse> getAllEvents(int page, int size, String city, String category){
        Pageable pageable = PageRequest.of(page,size);

        Page<Event> pageEvent = eventRepository.findByFilters(EventStatus.UPCOMING,city,category,pageable);

        return pageEvent.map(this :: mapToResponse);
    }

    public EventResponse getEvent(Long eventid){
        Event event = eventRepository
                .findById(eventid)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        return mapToResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(Long id,EventRequest eventRequest,String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        Venue venue = venueRepository.findById(eventRequest.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Wrong venue Id"));

        Event event = eventRepository.findByIdAndOrganizerId(id,user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found or you are not the organizer"));

        if(event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessException(
                    "Cannot update a cancelled event");
        }

        event.setTitle(eventRequest.getTitle());
        event.setDescription(eventRequest.getDescription());
        event.setCategory(eventRequest.getCategory());
        event.setCity(eventRequest.getCity());
        event.setEventDate(eventRequest.getEventDate());
        event.setPrice(eventRequest.getPrice());
        event.setVenue(venue);

        Event event1 = eventRepository.save(event);
        return mapToResponse(event1);
    }

    @Transactional
    public void cancelEvent(Long id,String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Organizer not found"));

        Event event = eventRepository
                .findByIdAndOrganizerId(id, user.getId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Event not found or you are not the organizer"));

        if(event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessException(
                    "Event is already cancelled");
        }

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    private EventResponse mapToResponse(Event event){
        long availableSeats = seatRepository
                .countByEventIdAndStatus(
                        event.getId(), SeatStatus.AVAILABLE);

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .city(event.getCity())
                .eventDate(event.getEventDate())
                .price(event.getPrice())
                .status(event.getStatus())
                .venueName(event.getVenue().getName())
                .organizerName(event.getOrganizer().getName())
                .availableSeats(availableSeats)
                .createdAt(event.getCreatedAt())
                .build();
    }
}
