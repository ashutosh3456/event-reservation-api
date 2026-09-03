package com.example.EventReservationAPI.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at" , updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Booking> bookings;

    @OneToMany(mappedBy = "organizer",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Event> organizedEvents;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
