package com.example.EventReservationAPI.dto.response;

import com.example.EventReservationAPI.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private BookingStatus status;
    private Double totalAmount;
    private LocalDateTime bookedAt;
    private String eventTitle;
    private String seatNumber;
    private String userName;
    private String userEmail;
}
