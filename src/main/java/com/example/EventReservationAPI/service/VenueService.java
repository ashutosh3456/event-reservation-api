package com.example.EventReservationAPI.service;

import com.example.EventReservationAPI.dto.request.VenueRequest;
import com.example.EventReservationAPI.dto.response.VenueResponse;
import com.example.EventReservationAPI.entity.Venue;
import com.example.EventReservationAPI.exception.ResourceNotFoundException;
import com.example.EventReservationAPI.repository.VenueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    @Transactional
    public VenueResponse createVenue(VenueRequest venueRequest){
        Venue venue = Venue.builder()
                        .name(venueRequest.getName())
                                .city(venueRequest.getCity())
                                        .address((venueRequest.getAddress()))
                                                .totalCapacity(venueRequest.getTotalCapacity())
                                                        .build();

        Venue savedVenue = venueRepository.save(venue);
        VenueResponse venueResp = mapToResponse(savedVenue);
        return venueResp;
    }

    public Page<VenueResponse> getAllVenue(int page,int size){
        Pageable pageable = PageRequest.of(page,size);

        Page<Venue> pageVenue = venueRepository.findAll(pageable);

        return pageVenue.map(venue -> mapToResponse(venue));
    }

    public VenueResponse getVenue(Long id){
       Venue venue =  venueRepository
               .findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Venue not found "));

       return mapToResponse(venue);
    }

    @Transactional
    public VenueResponse updateVenue(Long id, VenueRequest venueRequest){

        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        venue.setAddress(venueRequest.getAddress());
        venue.setTotalCapacity(venueRequest.getTotalCapacity());
        venue.setCity(venueRequest.getCity());
        venue.setName(venueRequest.getName());

        Venue updatedVenue = venueRepository.save(venue);

        return mapToResponse(updatedVenue);

    }

    public void deleteVenue(Long id){
        Venue venue =  venueRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        venueRepository.delete(venue);
    }

    public VenueResponse mapToResponse(Venue venue) {
        VenueResponse venueResponse = new VenueResponse();

        venueResponse.setId(venue.getId());
        venueResponse.setName(venue.getName());
        venueResponse.setCity(venue.getCity());
        venueResponse.setAddress(venue.getAddress());
        venueResponse.setTotalCapacity(venue.getTotalCapacity());

        return venueResponse;
    }
}
