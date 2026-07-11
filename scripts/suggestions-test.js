import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    vus: Number(__ENV.VUS || 50),
    duration: __ENV.DURATION || '30s',
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';
const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || '';

const KEYWORDS = (__ENV.KEYWORDS || '청년,청년 일,월세,취업,교육')
    .split(',')
    .map((keyword) => keyword.trim())
    .filter(Boolean);

export default function () {
    const keyword = KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)];
    const url = `${BASE_URL}/v1/policies/search-suggestions?keyword=${encodeURIComponent(keyword)}`;

    const params = ACCESS_TOKEN
        ? {
            headers: {
                Authorization: `Bearer ${ACCESS_TOKEN}`,
            },
        }
        : {};

    const res = http.get(url, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'fast enough': (r) => r.timings.duration < 200,
    });

    sleep(Number(__ENV.SLEEP || 0.3));
}