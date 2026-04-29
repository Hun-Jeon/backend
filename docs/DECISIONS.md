# Decision Log

프로젝트 진행 중 확정된 핵심 결정을 기록합니다.

## D-001: 환경 분리 전략

- Status: Accepted
- Decision: 환경은 `dev`, `prod` 2개만 운영
- Reason: 홈랩 운영 복잡도를 낮추고 유지보수 부담을 줄이기 위함

## D-002: 인증 전략

- Status: Accepted
- Decision: API 인증은 JWT / API Key / Basic 선택 적용 가능 구조로 설계
- Reason: 시스템별 연동 방식이 다를 수 있어 유연성 필요

## D-003: UI 인증 통합

- Status: Accepted
- Decision: Keycloak OIDC 기반 로그인, Okta는 Keycloak의 IdP로 연동 (Keycloak → Okta 페더레이션)
- Reason: 표준 OIDC 플로우 유지 + 외부 IdP 실습/확장성 확보

## D-004: 시크릿 관리

- Status: Accepted
- Decision: 앱 시크릿은 HashiCorp Vault를 단일 소스로 사용
- Reason: 평문 시크릿 분산을 방지하고 운영 보안성을 높이기 위함

## D-005: 플랫폼 통합

- Status: Accepted
- Decision: Observability는 Prometheus/Grafana, 품질은 SonarQube, CI/CD는 CircleCI 사용
- Reason: 실무에서 널리 쓰이는 조합으로 운영/확장 경험 축적 목적

## D-006: 프로젝트 운영 컨텍스트

- Status: Accepted
- Decision: 홈랩 중심 운영 + 필요 시 AWS 테스트 환경을 보조 검증용으로 사용
- Reason: 비용과 운영 복잡도 간 균형을 맞추면서 실무 유사성을 확보

## D-007: 프론트엔드 기술 스택

- Status: Accepted
- Decision: React + TypeScript + Vite로 프론트엔드 구성
- Reason: 타입 안정성 확보, Vite의 빠른 개발 서버 및 빌드 속도, 생태계 성숙도

## D-008: Stateless 인증 구조

- Status: Accepted
- Decision: httpOnly Cookie + Keycloak OIDC JWT 검증 방식으로 stateless 인증 구현
- Reason: XSS 방어(httpOnly), 서버 세션 불필요(stateless), Spring Security OAuth2 Resource Server와 자연스럽게 통합
- Detail:
  - `JwtCookieFilter`: `access_token` 쿠키 → `Authorization: Bearer` 헤더 변환
  - `SecurityConfig`: CORS (`allowCredentials`), `STATELESS` 세션, OAuth2 Resource Server 설정
  - `AuthController`: `POST /api/auth/session` (쿠키 발급), `DELETE /api/auth/session` (로그아웃)

## D-009: 레포 구조

- Status: Accepted
- Decision: Frontend(React)와 Backend(Spring Boot)를 모노레포로 관리
- Reason: 공통 설정 및 스크립트 공유, 배포 단위 일치, 단일 PR로 full-stack 변경 추적 가능

## D-010: 로컬 dev 도메인 분리 전략

- Status: Accepted
- Decision: 로컬 dev는 포트로 분리 (`localhost:3000` frontend / `localhost:8080` backend), 프로덕션은 서브도메인 분리
- Reason: 로컬에서 `/etc/hosts` 수정 없이 바로 시작 가능, `localhost` 공유로 httpOnly 쿠키 동작 보장, Vite proxy로 CORS 우회 가능
- Detail:
  - dev: `localhost:3000` ↔ `localhost:8080`
  - prod: `app.hjeon.i234.me` ↔ `api.hjeon.i234.me`

## D-011: 인가(Authorization) 전략

- Status: Accepted
- Decision: 초기 역할은 `user` / `admin` 2단계로 분리, Keycloak Realm Role로 관리
- Reason: 단순한 구조로 시작해 필요 시 확장, Keycloak에서 역할 중앙 관리로 백엔드 코드 변경 최소화
