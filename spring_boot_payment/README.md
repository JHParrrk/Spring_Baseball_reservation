# 💳 Payment Service

야구 좌석 예약 결제를 담당하는 마이크로서비스

---

> **포트**: `8081` | **프레임워크**: Spring Boot 3.5 / Java 21

---

## 🏗️ 개요

결제 서비스는 예약 서비스로부터 Kafka 이벤트를 수신하여 Mock 결제를 처리하고, 결과를 다시 Kafka로 전달합니다.
예약 서비스와 완전히 분리된 독립 서비스로, 인메모리 H2 DB를 사용합니다.

---

## ✨ 주요 기능

### 💳 Mock 결제 처리
- **CVC 기반 결과 결정**: CVC 마지막 자리 `0~3` = 실패, `4~9` = 성공
- **멱등성 보장**: `reservation_id` 유니크 제약으로 동일 예약 중복 결제 DB 레벨 차단
- **결제 상태**: `PENDING → SUCCESS / FAILED`

### 📨 Kafka 이벤트 연동
- **수신 토픽** (`reservation.success`): 예약 서비스로부터 결제 요청 이벤트 수신
- **발행 토픽** (`payment.result`): 결제 처리 결과를 예약 서비스로 전달

### 🔐 인증
- JWT 검증 (gateway와 동일한 시크릿)
- 결제 내역 조회 시 JWT 필수

---

## 🛠 기술 스택

| 기술 | 용도 |
|---|---|
| Spring Boot 3.5, Java 21 | 웹 프레임워크 |
| Spring Data JPA + H2 | 결제 데이터 영속성 (인메모리) |
| Apache Kafka | 예약-결제 비동기 이벤트 통신 |
| JJWT 0.12.5 | JWT 토큰 검증 |
| Springdoc OpenAPI | API 문서화 |

---

## 📂 패키지 구조

```text
src/main/java/com/firstspring/payment/
├── auth/         # JWT 필터, UserPrincipal
├── config/       # Kafka Consumer/Producer, SecurityConfig
├── consumer/     # ReservationEventConsumer (reservation.success 수신)
├── controller/   # PaymentController (결제 내역 조회)
├── dto/          # PaymentResultEvent, PaymentResponse
├── entity/       # Payment (PENDING/SUCCESS/FAILED)
├── event/        # PaymentResultPublisher (payment.result 발행)
├── repository/   # PaymentRepository
└── service/      # PaymentService (Mock 결제 처리)
```

---

## 📊 결제 흐름

```
예약 서비스
  │
  ├─ Kafka: reservation.success 발행
  │         { reservationId, userId, seatId, cvc, ... }
  │
결제 서비스 (consumer)
  │
  ├─ 멱등성 체크: 이미 처리된 reservationId?
  │
  ├─ CVC 마지막 자리 판별
  │       0~3 → FAILED
  │       4~9 → SUCCESS
  │
  └─ Kafka: payment.result 발행
            { reservationId, status: "SUCCESS" | "FAILED" }
              ↓
            예약 서비스 → CONFIRMED / CANCELLED 전환
```

---

## ⚙️ 환경 변수

`.env` 파일을 프로젝트 루트에 생성하세요.

```env
JWT_SECRET=
KAFKA_BOOTSTRAP_SERVERS=192.168.0.10:9092
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

| Method | Endpoint | 설명 | 인증 |
|---|---|---|---|
| `GET` | `/api/payments/my` | 내 결제 내역 조회 | JWT 필요 |
| `GET` | `/api/payments/{id}` | 결제 단건 조회 | JWT 필요 |

> H2 콘솔: [http://localhost:8081/h2-console](http://localhost:8081/h2-console)  
> 전체 API: [Swagger UI](http://localhost:8081/swagger-ui.html)

---

## 📄 라이선스

MIT License
