package com.example.EventReservationAPI.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRequest {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotBlank(message = "Category cannot be blank")
    private String category;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @Future
    @NotNull(message = "Eventdate cannot be blank")
    private LocalDateTime eventDate;

    @Positive(message = "Price must be positive")
    @NotNull(message = "Price cannot be blank")
    private Double price;

    @Positive(message = "VenueId must be positive")
    @NotNull(message = "VenueId cannot be blank")
    private Long venueId;
}
