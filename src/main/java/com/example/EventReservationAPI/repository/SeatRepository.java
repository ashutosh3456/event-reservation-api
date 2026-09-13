package com.example.EventReservationAPI.repository;

import com.example.EventReservationAPI.entity.Seat;
import com.example.EventReservationAPI.entity.SeatStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat,Long> {

    long countByEventIdAndStatus(
            Long eventId, SeatStatus status);

    void deleteByEventId(Long eventId);

    @Modifying
    @Transactional
    @Query("UPDATE Seat s SET s.status = :status " +
            "WHERE s.event.id = :eventId")
    void updateSeatStatusByEventId(
            @Param("eventId") Long eventId,
            @Param("status") SeatStatus status);

}
