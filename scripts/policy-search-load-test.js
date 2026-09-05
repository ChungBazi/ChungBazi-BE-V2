import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';
const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || '';
const REQUEST_SIZE = Number(__ENV.REQUEST_SIZE || 20);
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || 0.2);

const KEYWORDS = (__ENV.KEYWORDS || '적금,청약,자격증,일경험,주택청약')
    .split(',')
    .map((keyword) => keyword.trim())
    .filter(Boolean);

if (!ACCESS_TOKEN) {
    throw new Error('ACCESS_TOKEN 환경변수가 필요합니다.');
}

if (KEYWORDS.length === 0) {
    throw new Error('테스트할 검색어가 하나 이상 필요합니다.');
}

export const options = {
    scenarios: {
        warm_up: {
            executor: 'constant-vus',
            exec: 'searchPolicies',
            vus: 5,
            duration: '30s',
            tags: { phase: 'warm-up' },
        },
        policy_search: {
            executor: 'ramping-vus',
            exec: 'searchPolicies',
            startTime: '30s',
            startVUs: 1,
            stages: [
                { duration: '30s', target: 10 },
                { duration: '1m', target: 10 },
                { duration: '30s', target: 30 },
                { duration: '1m', target: 30 },
                { duration: '30s', target: 0 },
            ],
            gracefulRampDown: '10s',
            tags: { phase: 'measurement' },
        },
    },
    thresholds: {
        'http_req_failed{scenario:policy_search}': ['rate<0.01'],
        'http_req_duration{scenario:policy_search}': ['p(95)<300', 'p(99)<500'],
        'checks{scenario:policy_search}': ['rate>0.99'],
    },
};

export function searchPolicies() {
    const keyword = KEYWORDS[(__VU + __ITER) % KEYWORDS.length];
    const query = [
        `keyword=${encodeURIComponent(keyword)}`,
        'sort=LATEST',
        `size=${REQUEST_SIZE}`,
    ].join('&');

    const response = http.get(
        `${BASE_URL}/v1/policies/search?${query}`,
        {
            headers: {
                Authorization: `Bearer ${ACCESS_TOKEN}`,
            },
            tags: {
                endpoint: 'policy-search',
                keyword
            },
        }
    );

    check(response, {
        'status is 200': (result) => result.status === 200,
        'response is successful': (result) => {
            if (result.status !== 200) {
                return false;
            }

            try {
                return result.json('isSuccess') === true;
            } catch (error) {
                return false;
            }
        },
    });
    sleep(THINK_TIME_SECONDS);
}
