# 🎟️ Reservation Service

야구 경기 좌석 예약을 담당하는 핵심 백엔드 서비스

---

> **포트**: `8080` | **프레임워크**: Spring Boot 3.5 / Java 21

---

## 🏗️ 개요

예약 서비스는 경기 조회, 좌석 선택, 예약 생성부터 결제 개시까지의 전체 예약 흐름을 담당합니다.
Redisson 분산 락으로 동시 예약 충돌을 방지하고, RabbitMQ TTL + DLX로 미결제 예약을 자동 만료시킵니다.

---

## ✨ 주요 기능

### 🔐 인증 (JWT)

- Spring Security + JJWT 기반 JWT 검증
- `@AuthenticationPrincipal UserPrincipal`로 userId 추출
- 내부 서비스 시크릿(`X-Internal-Secret`) 헤더 검증

### 🎟️ 예약 흐름

- **예약 생성**: 최대 10석 동시 선택, 좌석 ID 오름차순 순서로 Redisson 락 획득 (데드락 방지)
- **결제 개시**: 내 예약 목록에서 선택한 예약들을 한 번에 결제 요청 → Kafka 이벤트 발행
- **예약 취소**: PENDING 상태 예약 수동 취소
- **자동 취소**: RabbitMQ 5분 TTL 만료 → DLQ 수신 → PENDING 자동 CANCELLED 전환
- **결제 확정**: `payment.result` Kafka 토픽 수신 → CONFIRMED 전환

### 🛡️ 동시성 제어

- 좌석당 `lock:seat:{seatId}` Redisson 분산 락
- 여러 좌석 동시 점유 시 seatId 오름차순 정렬로 데드락 방지
- `@TransactionalEventListener(AFTER_COMMIT)` — 트랜잭션 커밋 후 이벤트 발행으로 유령 메시지 방지

---

## 🛠 기술 스택

| 기술                      | 용도             |
| ------------------------- | ---------------- |
| Spring Boot 3.5, Java 21  | 웹 프레임워크    |
| Spring Data JPA + MariaDB | 데이터 영속성    |
| Spring Security + JJWT    | JWT 인증/인가    |
| Redis + Redisson          | 분산 락          |
| RabbitMQ (TTL + DLX)      | 예약 자동 만료   |
| Apache Kafka              | 결제 이벤트 통신 |
| Springdoc OpenAPI         | API 문서화       |

---

## 📂 패키지 구조

```text
src/main/java/com/firstspring/reservation/
├── auth/             # JWT 필터, UserPrincipal, SecurityConfig
├── common/           # 공통 예외, 응답 포맷
├── config/           # Kafka, Redis, RabbitMQ, Swagger 설정
├── match/            # 경기 엔티티 및 CRUD API
├── seat/             # 좌석 엔티티 및 조회 API
├── reservation/
│   ├── controller/   # ReservationController (예약 CRUD + /pay)
│   ├── service/      # ReservationServiceImpl, ReservationExecutorService
│   ├── event/        # EventListener (RabbitMQ 만료), EventPublisher (Kafka)
│   ├── dto/          # ReservationDto, ReservationPaymentDto, ReservationResponse
│   ├── entity/       # Reservation (PENDING/CONFIRMED/CANCELLED)
│   └── repository/   # ReservationRepository (JOIN FETCH N+1 방지)
└── user/             # User 엔티티, 관리자 API
```

---

## ⚙️ 환경 변수

`.env` 파일을 프로젝트 루트에 생성하세요.

```env
INFRA_HOST=192.168.0.10
DB_PORT=3307
DB_DATABASE=reservation
DB_USER=admin
DB_PASSWORD=
REDIS_PASSWORD=
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
JWT_SECRET=
INTERNAL_SECRET=
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

---

## ▶️ 실행

```bash
./mvnw spring-boot:run
```

또는 `run.bat` (Windows):

```bat
run.bat
```

---

## 📑 API 목록

### 인증 필요 (Bearer JWT)

| Method   | Endpoint             | 설명                           |
| -------- | -------------------- | ------------------------------ |
| `POST`   | `/reservations`      | 예약 생성 (좌석 ID 목록)       |
| `POST`   | `/reservations/pay`  | 결제 개시 (예약 ID 목록 + CVC) |
| `DELETE` | `/reservations/{id}` | 예약 취소                      |
| `GET`    | `/reservations/my`   | 내 예약 목록 조회              |

### 공개 API

| Method | Endpoint              | 설명             |
| ------ | --------------------- | ---------------- |
| `GET`  | `/matches`            | 경기 목록 조회   |
| `GET`  | `/matches/{id}/seats` | 경기별 좌석 조회 |

> 전체 API: [Swagger UI](http://localhost:8080/swagger-ui.html)

---

## � 성능 테스트 결과 (k6)

### 테스트 환경

- 도구: k6
- 동시 사용자: 100 VU
- 테스트 시간: 50초 (10s ramp-up / 30s sustain / 10s ramp-down)
- 테스트 흐름: 경기 조회 → 좌석 조회 → 예약 생성 → 결제 요청

### 부하 테스트 결과

| 항목 | 결과 |
| --- | --- |
| 총 HTTP 요청 수 | 3,521건 |
| 요청 실패율 (http_req_failed) | **0.00%** |
| 응답시간 p95 | **322.32ms** |
| 예약 오류율 (reservation_errors) | **0.00%** |
| 예약 성공 건수 | 760건 |
| 예약 충돌 건수 (seat contention) | 127건 |

> 좌석 선점 충돌(400/409)은 비즈니스 경쟁 결과로 분리 집계 — 장애성 실패와 구분

### 동시성 테스트 결과

동일 좌석에 50명이 동시에 예약 요청:

| 항목 | 결과 |
| --- | --- |
| 예약 성공 | **1건** |
| 예약 차단 (중복 방지) | **49건** |
| 비정상 실패 | **0건** |
| 결론 | **PASS — 중복 예약 0건** (Redisson 분산락 검증) |

---

## �📄 라이선스

MIT License
