import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

// Custom metrics to verify isolation
export const successfulHolds = new Counter('successful_holds');
export const conflictHolds = new Counter('conflict_holds');
export const serverErrors = new Counter('server_errors');

export const options = {
    scenarios: {
        flash_sale_spike: {
            executor: 'per-vu-iterations',
            vus: 100,              // 50 concurrent virtual users
            iterations: 1,        // Each user fires exactly 1 request
            maxDuration: '15s',
        },
    },
};

// Target endpoint configuration
const BASE_URL = 'http://localhost:8081';
const EVENT_ID = 4;        // <-- Set to your actual event_id
const SEAT_ID = 5120;      // <-- Set to your AVAILABLE seat_id

export default function () {
    // Option A: If your /hold endpoint takes a JWT token from dynamic or registered test users
    // Option B: If testing with one static token or public hold endpoint
    // Set your valid attendee Bearer token here:
    const AUTH_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGV4LmpvaG5zb25AZXhhbXBsZS5jb20iLCJpYXQiOjE3OTA5NjU5NzgsImV4cCI6MTc5MTA1MjM3OH0.QuaUFLUSiKx4mi0sj9w3EAy0p2fNaM9MkZBQeFhgEZA';

    const url = `${BASE_URL}/api/bookings/hold`;
    const payload = JSON.stringify({
        eventId: EVENT_ID,
        seatId: SEAT_ID,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${AUTH_TOKEN}`,
        },
    };

    const response = http.post(url, payload, params);

    // Categorize HTTP outcomes
    if (response.status === 200 || response.status === 201) {
        successfulHolds.add(1);
        console.log(`[SUCCESS] VU ${__VU} acquired the hold.`);
    } else if (response.status === 409 || response.status === 400) {
        conflictHolds.add(1);
    } else {
        serverErrors.add(1);
        console.error(`[ERROR] VU ${__VU} got status ${response.status}: ${response.body}`);
    }

    // Assertion check
    check(response, {
        'handled cleanly (200, 409, or 400)': (r) =>
            r.status === 200 || r.status === 201 || r.status === 409 || r.status === 400,
    });
}