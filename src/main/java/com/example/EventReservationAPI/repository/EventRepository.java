package com.example.EventReservationAPI.repository;

import com.example.EventReservationAPI.entity.Event;
import com.example.EventReservationAPI.entity.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("Select e from Event e Where e.status = :status " +
            "And (:city Is NULL or e.city = :city) " +
            "And (:category is null or e.category = :category)")
    Page<Event> findByFilters(@Param("status")EventStatus eventStatus,
                              @Param("city") String city,
                              @Param("category") String category,
                              Pageable pageable);

    Optional<Event> findByIdAndOrganizerId(Long id, Long organizerId);
}
