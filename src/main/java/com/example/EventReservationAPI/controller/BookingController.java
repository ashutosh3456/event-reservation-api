package com.example.EventReservationAPI.controller;

import com.example.EventReservationAPI.dto.request.BookingRequest;
import com.example.EventReservationAPI.dto.request.HoldSeatRequest;
import com.example.EventReservationAPI.dto.response.ApiResponse;
import com.example.EventReservationAPI.dto.response.BookingResponse;
import com.example.EventReservationAPI.dto.response.HoldSeatResponse;
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

    @PostMapping("/confirm")
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

    @PostMapping("/hold")
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(summary = "Hold a seat(ATTENDEE only)")
    public ResponseEntity<ApiResponse<HoldSeatResponse>> holdBooking(@Valid @RequestBody HoldSeatRequest holdSeatRequest,
                                                                      Authentication authentication){

        HoldSeatResponse holdSeatResponse = bookingService.holdSeat(holdSeatRequest,authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Seat held successfully for 10 minutes",
                holdSeatResponse));
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

    @DeleteMapping("/hold/{id}")
    @Operation(summary = "Release hold")
    public ResponseEntity<ApiResponse<Void>> releaseHold(@PathVariable Long id,Authentication authentication){
        bookingService.releaseHold(id, authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Seat Released",null));
    }
}
