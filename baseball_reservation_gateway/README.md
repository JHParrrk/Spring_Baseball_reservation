# 🛡️ API Gateway

모든 클라이언트 요청의 단일 진입점 — 인증, 라우팅, CORS 통합 관리

---

> **포트**: `8082` | **프레임워크**: Spring Cloud Gateway (WebFlux / Reactive) / Java 21

---

## 🏗️ 개요

API 게이트웨이는 프론트엔드와 백엔드 서비스 사이에 위치합니다.
Google OAuth2 로그인을 처리하고 JWT를 발급하며, 모든 요청을 적절한 서비스로 라우팅합니다.
클라이언트는 게이트웨이 주소만 알면 됩니다.

---

## ✨ 주요 기능

### 🔐 인증 (Google OAuth2 + JWT)

- **Google OAuth2 소셜 로그인**: 게이트웨이가 직접 인가 코드를 처리
- **JWT 발급**: 로그인 성공 시 JWT 생성 후 프론트엔드로 전달
- **JWT 검증 필터**: 인증이 필요한 모든 요청에 토큰 검증 수행
- **내부 시크릿 주입**: 검증된 요청에 `X-Internal-Secret` 헤더를 추가해 내부 서비스 직접 호출 방지

### 🔀 라우팅 규칙

| 경로 패턴            | 대상 서비스         | 포트 |
| -------------------- | ------------------- | ---- |
| `/reservations/**`   | Reservation Service | 8080 |
| `/matches/**`        | Reservation Service | 8080 |
| `/admin/**`          | Reservation Service | 8080 |
| `/api/infra-test/**` | Reservation Service | 8080 |
| `/api/payments/**`   | Payment Service     | 8081 |

### 🌐 CORS 통합 관리

- 글로벌 CORS 설정으로 모든 라우트에 일괄 적용
- `DedupeResponseHeader` 필터로 CORS 헤더 중복 제거
- `withCredentials: true` 지원

---

## 🛠 기술 스택

| 기술                          | 용도                    |
| ----------------------------- | ----------------------- |
| Spring Cloud Gateway 2024.0.1 | 리액티브 API 게이트웨이 |
| Spring Boot 3.4, Java 21      | 기반 프레임워크         |
| Spring Security OAuth2 Client | Google 소셜 로그인      |
| JJWT 0.12.5                   | JWT 생성 및 검증        |
| Spring Boot Actuator          | 헬스체크 엔드포인트     |

---

## 📂 패키지 구조

```text
src/main/java/com/firstspring/gateway/
├── config/        # SecurityConfig, CorsConfig
├── filter/        # JwtAuthenticationFilter (GlobalFilter)
└── auth/          # OAuth2 성공 핸들러 (JWT 발급)
```

---

## ⚙️ 환경 변수

`.env` 파일을 프로젝트 루트에 생성하세요.

```env
# Google OAuth2
# Google Cloud Console > 승인된 리다이렉트 URI에 추가:
# http://localhost:8082/login/oauth2/code/google
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

# JWT (reservation, payment 서비스와 동일한 값 사용)
JWT_SECRET=

# 내부 서비스 시크릿 (reservation 서비스와 동일한 값 사용)
INTERNAL_SECRET=

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000

# 하위 서비스 URL
RESERVATION_SERVICE_URL=http://localhost:8080
PAYMENT_SERVICE_URL=http://localhost:8081
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

## 🔗 OAuth2 로그인 흐름

```
[브라우저]
  │
  ├─ 1. GET http://localhost:8082/oauth2/authorization/google
  │       └─ Google 로그인 페이지로 리다이렉트
  │
  ├─ 2. Google 인가 코드 반환
  │       └─ GET /login/oauth2/code/google?code=...
  │
  ├─ 3. 게이트웨이 JWT 발급
  │       └─ 리다이렉트: http://localhost:3000/login-success?token=JWT
  │
  └─ 4. 이후 모든 요청
          └─ Authorization: Bearer {JWT}
```

---

## 💡 주의사항

- 게이트웨이는 WebFlux(리액티브) 기반으로, 블로킹 코드 사용 금지
- `spring-boot-starter-web` 대신 `spring-cloud-starter-gateway`만 사용
- 서비스 직접 호출 시 게이트웨이를 우회하므로 보안상 내부망에서만 허용

---

## 📄 라이선스

MIT License
