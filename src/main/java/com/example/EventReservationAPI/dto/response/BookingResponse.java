package com.example.EventReservationAPI.dto.response;

import com.example.EventReservationAPI.entity.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String bookingReference;
    private BookingStatus status;
    private Double totalAmount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime bookedAt;
    private String eventTitle;
    private String seatNumber;
    private String userName;
    private String userEmail;
}
