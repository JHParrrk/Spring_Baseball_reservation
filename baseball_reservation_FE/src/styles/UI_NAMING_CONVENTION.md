# UI Naming Convention

이 문서는 `src/styles` 기준의 공통 UI 클래스 네이밍 규칙입니다.

## 1) 기본 원칙

- 공통 재사용 클래스는 반드시 `ui-` 접두사를 사용한다.
- 화면 전용(로컬) 클래스는 화면 루트 네임스페이스 하위에서만 사용한다.
- 의미 중심으로 이름을 짓고, 구현 디테일(색상/px)을 이름에 넣지 않는다.

## 2) Prefix 규칙

- `ui-btn-*`: 공통 버튼
- `ui-badge-*`: 공통 배지
- `ui-*`: 그 외 공통 UI 토큰/패턴

예시:

- `ui-btn-primary`
- `ui-btn-secondary`
- `ui-btn-danger-sm`
- `ui-badge-confirmed`
- `ui-badge-pending`
- `ui-badge-cancelled`

## 3) 상태/변형 규칙

- 변형(variant)은 뒤에 하이픈으로 연결한다.
- 크기/강조 등 의미 있는 suffix만 허용한다.

패턴:

- `ui-btn-{variant}`
- `ui-badge-{state}`

## 4) 화면 전용 클래스 규칙

공통으로 올리지 않은 스타일은 화면 루트로 스코프를 제한한다.

예시:

- `.admin-page .tabs`
- `.home-page .match-grid`
- `.match-detail-page .booking-panel`
- `.my-reservations-page .reservation-card`

## 5) 파일 배치 규칙

- 공통 UI 규칙: `src/styles/common-ui.css.ts`
- 전역 리셋/기본값: `src/styles/global.css.ts`
- 테마 변수: `src/styles/theme.css.ts`, `src/styles/theme.ts`
- 화면 전용: `src/views/[ViewName].css.ts`

## 6) Do / Don't

Do:

- 공통 버튼/배지는 `ui-` 네이밍으로 통일
- 중복 스타일이 2곳 이상이면 `common-ui.css.ts`로 승격
- 템플릿에서도 동일한 이름 사용 (`class="ui-btn-primary"`)

Don't:

- `btn-primary`, `badge-*` 같은 비접두사 이름 신규 추가
- 화면 전용 규칙을 전역 선택자만으로 선언
- 같은 의미의 클래스를 다른 이름으로 중복 생성

## 7) 신규 추가 체크리스트

1. 이미 `common-ui.css.ts`에 동일 의미 클래스가 있는지 확인
2. 없다면 `ui-` 접두사로 추가
3. 1개 화면에서만 쓰면 우선 화면 css.ts에 두고, 재사용 시 공통으로 승격
4. `npm run build`로 스타일/타입 검증
