package com.example.EventReservationAPI.repository;

import com.example.EventReservationAPI.entity.Booking;
import jakarta.persistence.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Id> {
    Page<Booking> findByUserId(Long UserId , Pageable pageable);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    Boolean existsByUserIdAndSeatId(Long userId , Long seatId);
}
