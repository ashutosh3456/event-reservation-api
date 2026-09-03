package com.example.EventReservationAPI.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "venues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id ;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Integer totalCapacity;

    @OneToMany(mappedBy = "venue",
    cascade = CascadeType.ALL,
    fetch = FetchType.LAZY
    )
    @ToString.Exclude
    private List<Event> events;
}
