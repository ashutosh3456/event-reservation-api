package com.example.EventReservationAPI.dto.response;

import com.example.EventReservationAPI.entity.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeatResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String seatNumber;
    private SeatStatus status;


}
