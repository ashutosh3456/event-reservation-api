package com.example.EventReservationAPI.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HoldSeatResponse {

    private Long eventId;
    private Long seatId;
    private String holdToken;
    private LocalDateTime expiresAt;
    private String message;
}
