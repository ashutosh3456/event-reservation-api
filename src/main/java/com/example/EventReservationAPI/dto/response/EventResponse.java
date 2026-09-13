package com.example.EventReservationAPI.dto.response;

import com.example.EventReservationAPI.entity.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String city;
    private LocalDateTime eventDate;
    private Double price;
    private EventStatus status;
    private String venueName;
    private String organizerName;
    private Long availableSeats;
    private LocalDateTime createdAt;

}
