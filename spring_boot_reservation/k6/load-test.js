/**
 * k6 부하 테스트 - 예약 서비스 전체 플로우
 *
 * 테스트 시나리오:
 *   1. 경기 목록 조회 (공개 API)
 *   2. 잔여 좌석 조회 (공개 API)
 *   3. 좌석 예약 생성 (JWT 필요)
 *   4. 예약 결제 개시 (JWT 필요, CVC 3~4자리 숫자)
 *
 * 실행 방법:
 *   k6 run -e JWT_SECRET=<시크릿> -e BASE_URL=http://localhost:8080 -e MATCH_ID=1 load-test.js
 *
 * Docker로 실행:
 *   docker run --rm -i --network host \
 *     -e JWT_SECRET=3e1789... \
 *     -e BASE_URL=http://localhost:8080 \
 *     -e MATCH_ID=1 \
 *     grafana/k6 run - < load-test.js
 */

import http from "k6/http";
import { check, sleep, group } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";
import { hmac } from "k6/crypto";
import encoding from "k6/encoding";

// ── 설정 ────────────────────────────────────────────────────────────────────
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const JWT_SECRET =
  __ENV.JWT_SECRET ||
  "3e1789a0956468ec6e18e1dcab184b6906c230ed90bc02068dbce99b5aaf499e";
const MATCH_ID = parseInt(__ENV.MATCH_ID || "1");
const USER_ID_COUNT = parseInt(__ENV.USER_ID_COUNT || "100");
const USER_ID_SCAN_MAX = parseInt(__ENV.USER_ID_SCAN_MAX || "50");
const INTERNAL_SECRET =
  __ENV.INTERNAL_SECRET || "gateway-internal-secret-2026";
const STAGE_WARMUP_DURATION = __ENV.STAGE_WARMUP_DURATION || "10s";
const STAGE_MAIN_DURATION = __ENV.STAGE_MAIN_DURATION || "30s";
const STAGE_COOLDOWN_DURATION = __ENV.STAGE_COOLDOWN_DURATION || "10s";
const STAGE_WARMUP_TARGET = parseInt(__ENV.STAGE_WARMUP_TARGET || "20");
const STAGE_MAIN_TARGET = parseInt(__ENV.STAGE_MAIN_TARGET || "100");

// ── k6 옵션 ─────────────────────────────────────────────────────────────────
export const options = {
  stages: [
    { duration: STAGE_WARMUP_DURATION, target: STAGE_WARMUP_TARGET },
    { duration: STAGE_MAIN_DURATION, target: STAGE_MAIN_TARGET },
    { duration: STAGE_COOLDOWN_DURATION, target: 0 },
  ],
  thresholds: {
    http_req_failed: ["rate<0.05"], // 실패율 5% 미만
    http_req_duration: ["p(95)<500"], // 95th percentile 500ms 미만
    reservation_errors: ["rate<0.1"], // 예약 오류율 10% 미만
  },
};

// ── 커스텀 메트릭 ────────────────────────────────────────────────────────────
const reservationErrors = new Rate("reservation_errors");
const reservationDuration = new Trend("reservation_duration");
const reservationCreatedCount = new Counter("reservation_created_count");
const reservationConflictCount = new Counter("reservation_conflict_count");
const inventoryDepletedCount = new Counter("inventory_depleted_count");

// ── JWT 생성 유틸 ────────────────────────────────────────────────────────────
function generateJWT(userId, email) {
  const header = encoding.b64encode(
    JSON.stringify({ alg: "HS256", typ: "JWT" }),
    "rawurl",
  );
  const now = Math.floor(Date.now() / 1000);
  const payload = encoding.b64encode(
    JSON.stringify({
      sub: email,
      role: "ROLE_USER",
      userId: userId,
      iat: now,
      exp: now + 7200,
    }),
    "rawurl",
  );

  const signingInput = `${header}.${payload}`;
  const rawSig = hmac("sha256", JWT_SECRET, signingInput, "binary");
  const signature = encoding.b64encode(rawSig, "rawurl");
  return `${signingInput}.${signature}`;
}

function findExistingUserIds() {
  const found = [];
  const maxIdToScan = Math.max(USER_ID_COUNT, USER_ID_SCAN_MAX);

  for (let id = 1; id <= maxIdToScan; id++) {
    const token = generateJWT(id, `test${id}@example.com`);
    const res = http.get(`${BASE_URL}/users/me`, {
      headers: { Authorization: `Bearer ${token}` },
      tags: { name: "discover_user" },
      responseCallback: http.expectedStatuses(200, 404),
    });
    if (res.status === 200) {
      found.push(id);
    }
    if (found.length >= USER_ID_COUNT) {
      break;
    }
  }

  if (found.length === 0) {
    // 완전히 비어 있는 환경에서도 스크립트가 깨지지 않도록 fallback 유지
    return [1];
  }

  return found;
}

function createTestUser(index) {
  const email = `k6-load-${index}@example.com`;
  const res = http.post(
    `${BASE_URL}/internal/users/oauth2`,
    JSON.stringify({
      email,
      name: `k6user${index}`,
      provider: "google",
      providerId: `k6-${index}`,
    }),
    {
      headers: {
        "Content-Type": "application/json",
        "X-Internal-Key": INTERNAL_SECRET,
      },
      tags: { name: "create_user" },
    },
  );

  if (res.status >= 200 && res.status < 300) {
    const body = res.json();
    if (body && body.id) {
      return Number(body.id);
    }
  }
  return null;
}

function ensureUserIds(userIds) {
  const unique = new Set(userIds);
  let idx = 1;
  while (unique.size < USER_ID_COUNT) {
    const createdId = createTestUser(idx);
    if (createdId) {
      unique.add(createdId);
    }
    idx += 1;
    // 내부 시크릿 불일치 등으로 생성이 계속 실패하면 무한 루프 방지
    if (idx > USER_ID_COUNT * 5) {
      break;
    }
  }
  return Array.from(unique);
}

export function setup() {
  const discovered = findExistingUserIds();
  const userIds = ensureUserIds(discovered);
  console.log(`Using discovered userIds: ${userIds.join(", ")}`);
  return { userIds };
}

// ── 메인 시나리오 ────────────────────────────────────────────────────────────
export default function (data) {
  const userIds = data && data.userIds && data.userIds.length > 0 ? data.userIds : [1];
  const userId = userIds[(__VU - 1) % userIds.length];
  const token = generateJWT(userId, `test${userId}@example.com`);
  const authHeaders = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };

  // ── STEP 1: 경기 목록 조회 ─────────────────────────────────────────────────
  group("1. 경기 목록 조회", () => {
    const res = http.get(`${BASE_URL}/matches`);
    check(res, {
      "경기 목록 200": (r) => r.status === 200,
    });
  });

  sleep(0.5);

  // ── STEP 2: 잔여 좌석 조회 ─────────────────────────────────────────────────
  let availableSeatIds = [];
  group("2. 잔여 좌석 조회", () => {
    const res = http.get(`${BASE_URL}/matches/${MATCH_ID}/seats`);
    const ok = check(res, {
      "좌석 조회 200": (r) => r.status === 200,
    });
    if (ok && res.json() && res.json().length > 0) {
      const seats = res.json();
      // 각 VU가 다른 좌석을 선택하여 충돌 분산
      const idx = __VU % seats.length;
      availableSeatIds = [seats[idx].id];
    }
  });

  if (availableSeatIds.length === 0) {
    // 좌석 재고 소진은 예약 API 오류가 아니라 테스트 데이터 상태 이슈입니다.
    inventoryDepletedCount.add(1);
    sleep(1);
    return;
  }

  sleep(0.5);

  // ── STEP 3: 예약 생성 ─────────────────────────────────────────────────────
  let reservationId = null;
  group("3. 예약 생성 (분산락)", () => {
    const start = Date.now();
    const res = http.post(
      `${BASE_URL}/reservations`,
      JSON.stringify({ seatIds: availableSeatIds }),
      {
        headers: authHeaders,
        // 선점 경쟁에서 400/409는 정상 충돌 결과로 간주합니다.
        responseCallback: http.expectedStatuses(201, 400, 409),
      },
    );
    reservationDuration.add(Date.now() - start);

    check(res, {
      "예약 생성 상태(201 또는 400/409)": (r) =>
        r.status === 201 || r.status === 400 || r.status === 409,
    });

    if (res.status === 201 && res.json() && res.json().length > 0) {
      reservationId = res.json()[0].id;
      reservationCreatedCount.add(1);
    } else if (res.status === 400 || res.status === 409) {
      reservationConflictCount.add(1);
    }
    reservationErrors.add(res.status !== 201 && res.status !== 400 && res.status !== 409 ? 1 : 0);
  });

  if (!reservationId) {
    sleep(1);
    return;
  }

  sleep(0.5);

  // ── STEP 4: 결제 개시 ─────────────────────────────────────────────────────
  group("4. 결제 개시 (Kafka 이벤트)", () => {
    // 서버 검증 규칙: CVC는 3~4자리 숫자
    const cvc = String(Math.floor(Math.random() * 900) + 100);
    const res = http.post(
      `${BASE_URL}/reservations/pay`,
      JSON.stringify({
        reservationIds: [reservationId],
        cvc,
      }),
      { headers: authHeaders },
    );
    check(res, {
      "결제 요청 200": (r) => r.status === 200,
    });
  });

  sleep(1);
}
