import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

export const successfulBookings = new Counter('successful_bookings');
export const rejectedBookings = new Counter('rejected_bookings');
export const serverErrors = new Counter('server_errors');

export const options = {
    scenarios: {
        confirm_spike: {
            executor: 'per-vu-iterations',
            vus: 20,              // 20 simultaneous checkout requests
            iterations: 1,
            maxDuration: '15s',
        },
    },
};

const BASE_URL = 'http://localhost:8081';
const EVENT_ID = 4;        // <-- Your event_id
const SEAT_ID = 5120;      // <-- The HELD seat_id
const AUTH_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGV4LmpvaG5zb25AZXhhbXBsZS5jb20iLCJpYXQiOjE3OTA5NjU5NzgsImV4cCI6MTc5MTA1MjM3OH0.QuaUFLUSiKx4mi0sj9w3EAy0p2fNaM9MkZBQeFhgEZA';

export default function () {
    const url = `${BASE_URL}/api/bookings/confirm`;

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

    if (response.status === 200 || response.status === 201) {
        successfulBookings.add(1);
        console.log(`[SUCCESS] VU ${__VU} confirmed booking!`);
    } else if (response.status === 409 || response.status === 400 || response.status === 404) {
        rejectedBookings.add(1);
    } else {
        serverErrors.add(1);
        console.error(`[ERROR] VU ${__VU} got status ${response.status}: ${response.body}`);
    }

    check(response, {
        'handled cleanly': (r) =>
            r.status === 200 || r.status === 201 || r.status === 409 || r.status === 400 || r.status === 404,
    });
}