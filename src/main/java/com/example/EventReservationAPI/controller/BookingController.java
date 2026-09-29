package com.example.EventReservationAPI.controller;

import com.example.EventReservationAPI.dto.request.BookingRequest;
import com.example.EventReservationAPI.dto.response.ApiResponse;
import com.example.EventReservationAPI.dto.response.BookingResponse;
import com.example.EventReservationAPI.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/bookings")
@Tag(name = "Bookings",
        description = "Seat booking endpoints")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping()
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(
            summary = "Book a seat (ATTENDEE only)",
            description = "Uses pessimistic locking " +
                    "to prevent double booking"
    )
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest bookingRequest,
                                                                      Authentication authentication){

        BookingResponse bookingResponse = bookingService.bookSeat(bookingRequest,authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Booking created Successfully",
                bookingResponse));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my bookings")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAllBooking(Authentication authentication,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "5") int size){
        Page<BookingResponse> bookingResponses = bookingService.getMyBookings(authentication.getName(),page,size);

        return ResponseEntity.ok(ApiResponse.success("All Bookings " , bookingResponses));
    }

    @GetMapping("/my/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id, Authentication authentication){
        BookingResponse bookingResponse = bookingService.getBookingById(id, authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Booking " , bookingResponse));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel booking")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Long id,Authentication authentication){
        bookingService.cancelBooking(id, authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Booking cancelled",null));
    }
}
