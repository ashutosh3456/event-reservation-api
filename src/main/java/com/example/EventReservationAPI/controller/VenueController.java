package com.example.EventReservationAPI.controller;

import com.example.EventReservationAPI.dto.request.VenueRequest;
import com.example.EventReservationAPI.dto.response.ApiResponse;
import com.example.EventReservationAPI.dto.response.VenueResponse;
import com.example.EventReservationAPI.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody VenueRequest venueRequest){
        VenueResponse venueResponse = venueService.createVenue(venueRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse
                .success("Venue Created Succesfully" , venueResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VenueResponse>>> getAllVenues(@RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "5") int size){
        Page<VenueResponse> venueResponses = venueService.getAllVenue(page,size);

        return ResponseEntity.ok(ApiResponse.success("All Venue" , venueResponses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenue(@PathVariable Long id){
        VenueResponse venueResponse = venueService.getVenue(id);

        return ResponseEntity.ok(ApiResponse.success("Venue requested with id : " + id , venueResponse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VenueResponse>> updateVenue(@PathVariable Long id ,@Valid @RequestBody VenueRequest venueRequest){
        VenueResponse venueResponse = venueService.updateVenue(id,venueRequest);

        return ResponseEntity.ok(ApiResponse.success("Venue updated" , venueResponse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVenue(@PathVariable Long id){
        venueService.deleteVenue(id);

        return ResponseEntity.ok(ApiResponse.success("Venue Deleted",null));
    }

}
