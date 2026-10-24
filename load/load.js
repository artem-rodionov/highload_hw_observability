import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '20s', target: 100 },
        { duration: '2m', target: 100 },
        { duration: '20s', target: 0 },
    ],
};

export default function () {
    const isError = Math.random() < 0.1;

    const action = Math.random();

    const baseUrl = 'http://crud:8080';

    let res;

    if (action < 0.33) {
        const id = isError ? 999999 : 1;
        res = http.get(`${baseUrl}/get/order/${id}`);
        check(res, { 'GET status is 2xx or 404': (r) => r.status >= 200 && r.status < 500 });

    } else if (action < 0.66) {
        if (isError) {
            const badPayload = JSON.stringify({ wrong_field: 'error' });
            res = http.post(`${baseUrl}/post/order`, badPayload, { headers: { 'Content-Type': 'application/json' } });
        } else {
            const payload = JSON.stringify({ user_id: 1, price: 1 });
            res = http.post(`${baseUrl}/post/order`, payload, { headers: { 'Content-Type': 'application/json' } });
        }
        check(res, { 'POST status is 2xx or 4xx': (r) => r.status >= 200 && r.status < 500 });

    } else {
        if (isError) {
            const badPayload = JSON.stringify({ item: 'update_error' });
            res = http.put(`${baseUrl}/put/order/999999`, badPayload, { headers: { 'Content-Type': 'application/json' } });
        } else {
            const payload = JSON.stringify({ item: 'updated_item', quantity: 2 });
            res = http.put(`${baseUrl}/put/order/1`, payload, { headers: { 'Content-Type': 'application/json' } });
        }
        check(res, { 'PUT status is 2xx or 4xx': (r) => r.status >= 200 && r.status < 500 });
    }
    sleep(0.5);
}