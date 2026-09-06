package com.example.EventReservationAPI.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "EventId cannot be blank")
    @Positive
    private Long eventId;

    @NotNull(message = "SeatId cannot be blank")
    @Positive
    private Long seatId;

}
