package com.example.EventReservationAPI.repository;

import com.example.EventReservationAPI.entity.Seat;
import com.example.EventReservationAPI.entity.SeatStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "2000") // 2-second fail-fast timeout
    })
    @Query("Select s from Seat s where s.id = :id")
    Optional<Seat> findByIdWithLock(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Seat s SET s.status = 'AVAILABLE', s.heldAt = null " +
            "WHERE s.status = 'HELD' AND s.heldAt < :cutoff")
    int releaseExpiredHolds(@Param("cutoff") LocalDateTime cutoff);
}
