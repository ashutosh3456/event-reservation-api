package com.example.EventReservationAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EventReservationApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventReservationApiApplication.class, args);
	}

}
