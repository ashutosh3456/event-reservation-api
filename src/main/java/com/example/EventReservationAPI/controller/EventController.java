package com.example.EventReservationAPI.controller;

import com.example.EventReservationAPI.dto.request.EventRequest;
import com.example.EventReservationAPI.dto.response.ApiResponse;
import com.example.EventReservationAPI.dto.response.EventResponse;
import com.example.EventReservationAPI.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @PostMapping()
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody EventRequest eventRequest,Authentication authentication){

        EventResponse eventResponse = eventService.createEvent(eventRequest,authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Event created Succesfully" ,
                eventResponse));

    }

    @GetMapping()
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getAllEvents(@RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "5") int size,
                                                                         @RequestParam(required = false) String city,
                                                                         @RequestParam(required = false) String category){
        Page<EventResponse> eventResponses = eventService.getAllEvents(page,size,city,category);

        return ResponseEntity.ok(ApiResponse.success("All events" , eventResponses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long id){
        EventResponse eventResponses = eventService.getEvent(id);

        return ResponseEntity.ok(ApiResponse.success("Event" , eventResponses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(@PathVariable Long id,
                                                                  @Valid @RequestBody EventRequest eventRequest,
                                                                  Authentication authentication){

        EventResponse eventResponse = eventService.updateEvent(id,eventRequest,authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Event updated" , eventResponse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<Void>> cancelEvent(@PathVariable Long id,Authentication authentication){

        eventService.cancelEvent(id,authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Event cancelled " , null));
    }
}
