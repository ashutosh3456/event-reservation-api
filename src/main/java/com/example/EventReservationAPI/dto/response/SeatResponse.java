package com.example.EventReservationAPI.dto.response;

import com.example.EventReservationAPI.entity.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeatResponse {

    private Long id;
    private String seatNumber;
    private SeatStatus status;


}
