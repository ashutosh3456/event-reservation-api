package com.example.EventReservationAPI.repository;

import com.example.EventReservationAPI.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VenueRepository extends JpaRepository<Venue,Long> {}

