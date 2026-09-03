package com.example.EventReservationAPI.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(name = "created_at" , updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id" ,nullable = false)
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id" , nullable = false)
    private Venue venue;

    @OneToMany(mappedBy = "event",
    cascade = CascadeType.ALL,
    fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Seat> seats;

    @OneToMany(mappedBy = "event",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Booking> bookings;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = EventStatus.UPCOMING;
    }
}
