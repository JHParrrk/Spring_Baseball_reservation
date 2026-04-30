# 🖥️ Baseball Reservation Frontend

야구 경기 좌석 예약 및 결제 웹 클라이언트

---

> **포트**: `3000` | **프레임워크**: Vue 3 + TypeScript + Vite

---

## 🏗️ 개요

예약 시스템의 프론트엔드입니다. Google OAuth2 로그인부터 경기 조회, 좌석 선택, 예약, 결제까지 전체 사용자 흐름을 담당합니다.
API 게이트웨이(8082)를 통해 백엔드와 통신합니다.

---

## ✨ 주요 기능

### 🏠 경기 목록 (`HomeView`)

- 예정된 야구 경기 목록 조회
- 경기 카드 클릭 시 상세 페이지로 이동

### 🎟️ 경기 상세 및 예약 (`MatchDetailView`)

- 경기 정보 및 좌석 그리드 표시
- 좌석 클릭으로 선택/해제 (AVAILABLE 좌석만 선택 가능)
- **"예약하기"** 버튼 클릭 시 즉시 예약 완료 (CVC 없음)
- 예약 완료 토스트: "예약 완료! 내 예약에서 결제하실 수 있습니다. ✅"

### 📋 내 예약 (`MyReservationsView`)

- 내 전체 예약 목록 조회 (PENDING / CONFIRMED / CANCELLED)
- PENDING 예약에 체크박스 표시 — 여러 개 선택 가능
- 하단 결제 바: 선택 수량 + 합산 금액 + **"선택 결제하기"** 버튼
- 결제 모달에서 CVC 입력 후 결제 요청
- 결제 완료 후 목록 자동 새로고침

### 💳 결제 모달 (`PaymentModal`)

- 선택한 예약 목록 (좌석번호, 등급, 금액) 표시
- 합산 금액 표시
- CVC 3자리 입력 및 결제 버튼

---

## 🛠 기술 스택

| 기술                    | 용도                                      |
| ----------------------- | ----------------------------------------- |
| Vue 3 (Composition API) | UI 프레임워크                             |
| TypeScript              | 타입 안전성                               |
| Vite                    | 빌드 도구                                 |
| Pinia                   | 전역 상태 관리                            |
| Vue Router              | SPA 라우팅                                |
| Axios                   | HTTP 클라이언트 (`withCredentials: true`) |

---

## 📂 프로젝트 구조

```text
src/
├── api/
│   └── index.ts          # Axios 인스턴스 및 API 함수 모음
├── components/
│   ├── NavBar.vue         # 상단 네비게이션
│   ├── SeatGrid.vue       # 좌석 선택 그리드
│   └── PaymentModal.vue   # 결제 정보 입력 모달
├── views/
│   ├── HomeView.vue           # 경기 목록
│   ├── MatchDetailView.vue    # 경기 상세 + 예약
│   ├── MyReservationsView.vue # 내 예약 목록 + 결제
│   └── LoginSuccessView.vue   # OAuth2 로그인 성공 처리
├── stores/
│   └── auth.ts            # JWT 토큰 Pinia 스토어
├── router/
│   └── index.ts           # Vue Router 라우트 설정
└── main.ts
```

---

## ⚙️ 환경 변수

`.env` 파일을 프로젝트 루트에 생성하세요.

```env
VITE_API_BASE_URL=http://localhost:8082
```

---

## ▶️ 시작하기

```bash
# 의존성 설치
npm install

# 개발 서버 실행 (http://localhost:3000)
npm run dev

# 프로덕션 빌드
npm run build

# 타입 체크
npm run type-check
```

---

## 🔄 API 연동

모든 API 호출은 `src/api/index.ts`에서 관리합니다.

```typescript
// 게이트웨이를 통한 예약 API
reservationApi.getAll(); // 내 예약 목록
reservationApi.create(seatIds); // 예약 생성
reservationApi.pay(reservationIds, cvc); // 결제 개시
reservationApi.cancel(id); // 예약 취소
```

---

## 💡 Google OAuth2 로그인 흐름

1. 사용자 → `http://localhost:8082/oauth2/authorization/google` 접근
2. Google 로그인 완료
3. 게이트웨이 → `http://localhost:3000/login-success?token=JWT` 리다이렉트
4. `LoginSuccessView`에서 토큰 파싱 → Pinia 스토어에 저장
5. 이후 모든 요청에 `Authorization: Bearer {token}` 자동 첨부

---

## 📄 라이선스

MIT License
