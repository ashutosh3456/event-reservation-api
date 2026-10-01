package com.example.EventReservationAPI.scheduler;


import com.example.EventReservationAPI.repository.SeatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatCleanupScheduler {

    private final SeatRepository seatRepository;

    // Runs every 60 seconds
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredHolds() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        int releasedCount = seatRepository.releaseExpiredHolds(cutoff);

        if (releasedCount > 0) {
            log.info("Released {} expired seat holds back to AVAILABLE", releasedCount);
        }
    }
}
