# ⚾ Spring Baseball Reservation

야구 경기 좌석 예약 및 결제 통합 플랫폼

---

> **"좌석 선택부터 결제까지, 하나의 플랫폼에서"**
> 분산락, 메시지 큐, 이벤트 기반 아키텍처를 활용한 실시간 야구 좌석 예약 시스템입니다.

---

## 🔗 Quick Links

- 📑 **Reservation API Docs**: [Swagger UI](http://localhost:8080/swagger-ui.html)
- 📑 **Payment API Docs**: [Swagger UI](http://localhost:8081/swagger-ui.html)
- 🌐 **Gateway Endpoint**: `http://localhost:8082`
- 🖥️ **Frontend**: `http://localhost:3000`
- 🐙 **GitHub Repository**: [Spring_Baseball_reservation](https://github.com/JHParrrk/Spring_Baseball_reservation)

---

## 🏗️ 프로젝트 개요 (Project Overview)

Spring Baseball Reservation은 야구 경기 좌석 예약과 결제를 분리하여 처리하는 마이크로서비스 기반 플랫폼입니다.
사용자는 경기를 조회하고 좌석을 예약한 뒤, 내 예약 목록에서 원하는 예약을 선택해 결제할 수 있습니다.

### 🎯 주요 목표

- **동시성 제어**: Redisson 분산 락으로 동일 좌석 중복 예약을 원천 차단합니다.
- **비동기 결제**: Kafka 이벤트 기반으로 예약-결제 서비스 간 결합도를 낮춥니다.
- **자동 만료 처리**: RabbitMQ TTL + DLX로 미결제 예약을 5분 후 자동 취소합니다.
- **확장성**: 각 서비스가 독립적으로 배포 및 확장 가능한 구조입니다.

---

## ✨ 핵심 기능 (Key Features)

### 🔐 1. 인증 (Google OAuth2 + JWT)

- **Google OAuth2 로그인**: Spring Cloud Gateway에서 소셜 로그인 처리
- **JWT 발급 및 검증**: 게이트웨이가 JWT를 발급하고 각 서비스가 독립적으로 검증
- **내부 서비스 시크릿**: 게이트웨이 → 내부 서비스 간 `X-Internal-Secret` 헤더로 직접 호출 방지

### 🎟️ 2. 예약 (Reservation)

- **좌석 선택 예약**: 경기별 좌석 목록 조회 후 최대 10석 동시 예약
- **분산 락(Redisson)**: 좌석 ID 오름차순 정렬로 데드락 방지, 좌석별 개별 락 획득
- **예약 상태 관리**: `PENDING → CONFIRMED / CANCELLED` 상태 흐름 자동 처리
- **5분 자동 취소**: RabbitMQ DLQ로 결제 미완료 예약 자동 만료

### 💳 3. 결제 (Payment)

- **분리된 결제 흐름**: 예약 완료 후 내 예약 목록에서 체크박스로 선택해 일괄 결제
- **CVC 기반 Mock 결제**: CVC 마지막 자리 0~3 = 실패, 4~9 = 성공
- **멱등성 보장**: `reservation_id` 유니크 제약으로 중복 결제 DB 레벨에서 차단
- **비동기 결과 전달**: `payment.result` Kafka 토픽으로 예약 서비스에 결과 전송

### 🛡️ 4. API Gateway

- **단일 진입점**: 프론트엔드는 게이트웨이(8082)만 알면 됨
- **JWT 필터**: 인증이 필요한 모든 요청에 토큰 검증
- **CORS 통합 관리**: 글로벌 CORS 설정으로 중복 헤더 제거

---

## 🛠 기술 스택 (Tech Stack)

| 영역              | 기술                                     |
| ----------------- | ---------------------------------------- |
| **Backend**       | Spring Boot 3.5, Java 21                 |
| **Gateway**       | Spring Cloud Gateway (WebFlux)           |
| **Database**      | MariaDB 10.x (예약), H2 In-Memory (결제) |
| **Cache / Lock**  | Redis 7.x, Redisson 3.43                 |
| **Message Queue** | Apache Kafka, RabbitMQ (TTL + DLX)       |
| **Auth**          | Google OAuth2, JWT (JJWT 0.12.5)         |
| **Frontend**      | Vue 3, TypeScript, Vite, Pinia, Axios    |
| **API Docs**      | Springdoc OpenAPI (Swagger)              |
| **Build**         | Maven, Node.js                           |

---

## 📂 프로젝트 구조 (Project Structure)

```text
Spring_Baseball_reservation/
├── spring_boot_reservation/   # 예약 서비스 (port 8080)
│   ├── auth/                  # JWT 인증 필터, UserPrincipal
│   ├── match/                 # 경기 관리 API
│   ├── seat/                  # 좌석 관리 API
│   ├── reservation/           # 예약 생성/조회/결제 개시/취소
│   └── user/                  # 사용자 관리
│
├── spring_boot_payment/       # 결제 서비스 (port 8081)
│   ├── consumer/              # Kafka 이벤트 소비
│   ├── service/               # Mock 결제 처리 로직
│   └── controller/            # 결제 내역 조회 API
│
├── baseball_reservation_gateway/  # API 게이트웨이 (port 8082)
│   └── filter/                # JWT 검증, 내부 시크릿 주입
│
└── baseball_reservation_FE/   # 프론트엔드 (port 3000)
    └── src/
        ├── views/             # HomeView, MatchDetailView, MyReservationsView
        └── api/               # Axios API 클라이언트
```

---

## ⚙️ 시작하기 (Getting Started)

### 1. 인프라 준비

```bash
# docker-compose로 MariaDB, Redis, RabbitMQ, Kafka 실행
cd spring_boot_reservation
docker-compose up -d
```

### 2. 환경 변수 설정

각 서비스 폴더의 `.env` 파일을 생성하세요. 아래 항목들이 필요합니다.

**`spring_boot_reservation/.env`**

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

**`baseball_reservation_gateway/.env`**

```env
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
JWT_SECRET=
INTERNAL_SECRET=
CORS_ALLOWED_ORIGINS=http://localhost:3000
RESERVATION_SERVICE_URL=http://localhost:8080
PAYMENT_SERVICE_URL=http://localhost:8081
```

**`spring_boot_payment/.env`**

```env
JWT_SECRET=
KAFKA_BOOTSTRAP_SERVERS=192.168.0.10:9092
```

### 3. 서비스 실행

```bash
# 예약 서비스
cd spring_boot_reservation && ./mvnw spring-boot:run

# 결제 서비스
cd spring_boot_payment && ./mvnw spring-boot:run

# 게이트웨이
cd baseball_reservation_gateway && ./mvnw spring-boot:run

# 프론트엔드
cd baseball_reservation_FE && npm install && npm run dev
```

---

## 🔄 예약-결제 흐름

```
[사용자]
  │
  ├─ 1. 경기 조회 / 좌석 선택
  │
  ├─ 2. POST /reservations → 예약 생성 (PENDING)
  │       └─ Redisson 분산 락으로 좌석 동시 점유 방지
  │       └─ RabbitMQ TTL 5분 타이머 시작
  │
  ├─ 3. 내 예약 목록에서 결제할 예약 체크박스 선택
  │
  ├─ 4. POST /reservations/pay + CVC 입력
  │       └─ Kafka: reservation.success 토픽 발행
  │
  ├─ 5. 결제 서비스가 Kafka 이벤트 소비 → Mock 결제 처리
  │       └─ Kafka: payment.result 토픽 발행
  │
  └─ 6. 예약 서비스가 결과 수신 → CONFIRMED / CANCELLED 전환
```

---

## 📄 라이선스 (License)

본 프로젝트는 **MIT License**를 따릅니다.
