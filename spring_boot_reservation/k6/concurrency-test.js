/**
 * k6 동시성(분산락) 검증 테스트
 *
 * 핵심 검증:
 *   - 동일한 좌석에 N명이 동시에 예약 시도
 *   - 성공은 반드시 1건, 나머지는 409 또는 실패여야 함
 *   - Redis Redisson 분산락 (lock:seat:{seatId}) 정상 동작 확인
 *
 * 실행 전 준비:
 *   1. SEAT_ID 에 테스트할 AVAILABLE 상태의 좌석 ID 입력
 *   2. 앱 실행 상태 확인 (reservation :8080, Redis :6379)
 *
 * 실행 방법:
 *   k6 run -e JWT_SECRET=<시크릿> -e BASE_URL=http://localhost:8080 -e SEAT_ID=1 concurrency-test.js
 *
 * Docker로 실행:
 *   docker run --rm -i --network host \
 *     -e JWT_SECRET=3e1789... \
 *     -e BASE_URL=http://localhost:8080 \
 *     -e SEAT_ID=1 \
 *     grafana/k6 run - < concurrency-test.js
 */

import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';
import { hmac } from 'k6/crypto';
import encoding from 'k6/encoding';

// ── 설정 ────────────────────────────────────────────────────────────────────
const BASE_URL   = __ENV.BASE_URL   || 'http://localhost:8080';
const JWT_SECRET = __ENV.JWT_SECRET || '3e1789a0956468ec6e18e1dcab184b6906c230ed90bc02068dbce99b5aaf499e';
const SEAT_ID    = parseInt(__ENV.SEAT_ID || '1');
const INTERNAL_SECRET = __ENV.INTERNAL_SECRET || 'gateway-internal-secret-2026';
const CONCURRENCY_VUS = parseInt(__ENV.CONCURRENCY_VUS || '50');
const CONCURRENCY_ITERATIONS = parseInt(__ENV.CONCURRENCY_ITERATIONS || String(CONCURRENCY_VUS));
const USER_ID_SCAN_MAX = parseInt(__ENV.USER_ID_SCAN_MAX || '100');

// ── k6 옵션 ─────────────────────────────────────────────────────────────────
export const options = {
  // 50명이 동시에 같은 좌석을 예약 시도 (1회만)
  scenarios: {
    concurrent_reservation: {
      executor:    'shared-iterations',
      vus:         CONCURRENCY_VUS,
      iterations:  CONCURRENCY_ITERATIONS,
      maxDuration: '30s',
    },
  },
  thresholds: {
    // 성공(201)은 반드시 1건이어야 함 — 분산락 검증 핵심
    'success_count': ['count==1'],
    // 순수 락 충돌(409)이 나머지 요청 수와 일치해야 함
    'conflict_count': [`count==${CONCURRENCY_ITERATIONS - 1}`],
    // 유효성 검증/인증 실패 같은 잡음은 0건이어야 함
    'unexpected_failure_count': ['count==0'],
  },
};

// ── 커스텀 메트릭 ────────────────────────────────────────────────────────────
const successCount  = new Counter('success_count');   // 예약 성공 (201)
const conflictCount = new Counter('conflict_count');  // 이미 선점됨 (400/409)
const unexpectedFailureCount = new Counter('unexpected_failure_count'); // 401/429/500 등 비정상 실패

// ── JWT 생성 유틸 ────────────────────────────────────────────────────────────
function generateJWT(userId, email) {
  const header  = encoding.b64encode(JSON.stringify({ alg: 'HS256', typ: 'JWT' }), 'rawurl');
  const now     = Math.floor(Date.now() / 1000);
  const payload = encoding.b64encode(JSON.stringify({
    sub:    email,
    role:   'ROLE_USER',
    userId: userId,
    iat:    now,
    exp:    now + 7200,
  }), 'rawurl');

  const signingInput = `${header}.${payload}`;
  const rawSig       = hmac('sha256', JWT_SECRET, signingInput, 'binary');
  const signature    = encoding.b64encode(rawSig, 'rawurl');
  return `${signingInput}.${signature}`;
}

function findExistingUserIds(targetCount) {
  const found = [];
  const maxIdToScan = Math.max(targetCount, USER_ID_SCAN_MAX);

  for (let id = 1; id <= maxIdToScan; id++) {
    const token = generateJWT(id, `test${id}@example.com`);
    const res = http.get(`${BASE_URL}/users/me`, {
      headers: { Authorization: `Bearer ${token}` },
      tags: { name: 'discover_user' },
      responseCallback: http.expectedStatuses(200, 404),
    });
    if (res.status === 200) {
      found.push(id);
    }
    if (found.length >= targetCount) {
      break;
    }
  }
  return found;
}

function createTestUser(index) {
  const email = `k6-concurrency-${index}@example.com`;
  const res = http.post(
    `${BASE_URL}/internal/users/oauth2`,
    JSON.stringify({
      email,
      name: `k6cuser${index}`,
      provider: 'google',
      providerId: `k6c-${index}`,
    }),
    {
      headers: {
        'Content-Type': 'application/json',
        'X-Internal-Key': INTERNAL_SECRET,
      },
      tags: { name: 'create_user' },
      responseCallback: http.expectedStatuses(200, 201),
    }
  );

  if (res.status >= 200 && res.status < 300) {
    const body = res.json();
    if (body && body.id) {
      return Number(body.id);
    }
  }
  return null;
}

function ensureUserIds(userIds, targetCount) {
  const unique = new Set(userIds);
  let idx = 1;
  while (unique.size < targetCount) {
    const createdId = createTestUser(idx);
    if (createdId) {
      unique.add(createdId);
    }
    idx += 1;
    if (idx > targetCount * 5) {
      break;
    }
  }
  return Array.from(unique);
}

export function setup() {
  const discovered = findExistingUserIds(CONCURRENCY_VUS);
  const userIds = ensureUserIds(discovered, CONCURRENCY_VUS);
  console.log(`Using concurrency userIds: ${userIds.join(', ')}`);
  return { userIds };
}

// ── 메인 시나리오 ────────────────────────────────────────────────────────────
export default function (data) {
  // VU마다 다른 유저로 동일 좌석을 동시에 요청
  const userIds = data && data.userIds && data.userIds.length > 0 ? data.userIds : [1];
  const userId = userIds[(__VU - 1) % userIds.length];
  const token  = generateJWT(userId, `test${userId}@example.com`);

  const res = http.post(
    `${BASE_URL}/reservations`,
    JSON.stringify({ seatIds: [SEAT_ID] }),
    {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      responseCallback: http.expectedStatuses(201, 400, 409),
    }
  );

  check(res, {
    '201 또는 400/409': (r) => r.status === 201 || r.status === 400 || r.status === 409,
  });

  if (res.status === 201) {
    successCount.add(1);
  } else if (res.status === 400 || res.status === 409) {
    conflictCount.add(1);
  } else {
    unexpectedFailureCount.add(1);
    console.log(`[⚠️ 비정상] VU ${__VU} 예약 실패 - status: ${res.status}`);
  }
}

// ── 결과 요약 ────────────────────────────────────────────────────────────────
export function handleSummary(data) {
  const success  = data.metrics.success_count  ? data.metrics.success_count.values.count  : 0;
  const conflict = data.metrics.conflict_count ? data.metrics.conflict_count.values.count : 0;
  const unexpected = data.metrics.unexpected_failure_count
    ? data.metrics.unexpected_failure_count.values.count
    : 0;
  const lockOk   = success === 1;

  return {
    stdout: `
╔══════════════════════════════════════════════════════╗
║          분산락(Redisson) 동시성 테스트 결과           ║
╠══════════════════════════════════════════════════════╣
║  대상 좌석 ID : ${String(SEAT_ID).padEnd(36)}║
║  총 동시 요청 : 50명                                  ║
╠══════════════════════════════════════════════════════╣
║  예약 성공    : ${String(success).padEnd(36)}║
║  예약 차단    : ${String(conflict).padEnd(36)}║
║  비정상 실패  : ${String(unexpected).padEnd(36)}║
╠══════════════════════════════════════════════════════╣
║  분산락 검증  : ${lockOk ? '✅ PASS — 중복 예약 0건' : '❌ FAIL — 중복 예약 발생!'}${' '.repeat(lockOk ? 17 : 14)}║
╚══════════════════════════════════════════════════════╝
`,
  };
}
