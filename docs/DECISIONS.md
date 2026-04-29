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

## D-012: 프론트엔드 독립적 백엔드 API 설계

- Status: Accepted
- Decision: 백엔드는 순수 REST API만 제공하며 특정 프론트엔드에 종속되지 않는다. Service A(자체 개발 React 앱)와 Service B(기존 사내 서비스) 중 어느 쪽이 붙더라도 백엔드 변경 없이 동작해야 한다.
- Reason: PoC 단계에서 Service A로 개발하되, 리더십 의사결정 이후 Service B로 전환하거나 병행 운영할 수 있어야 함. Service A는 dev 환경에서 팀 내 PoC 용도로 유지.
- Detail:
  - Service A (브라우저): Keycloak OIDC 인증 → httpOnly Cookie 방식
  - Service B (서버-서버): Vault 공유 서명키 기반 JWT 방식
  - 백엔드는 두 방식을 모두 수용, 클라이언트 전환 시 백엔드 무변경

## D-013: 다중 JWT 발급자(Multi-issuer) 인증 전략

- Status: Accepted
- Decision: JWT `iss`(issuer) 클레임 기준으로 인증 검증기를 분기한다. Keycloak 발급 토큰은 Keycloak JWKS로 검증, Service B 발급 토큰은 Vault에 저장된 공유 서명키로 검증한다.
- Reason: 두 클라이언트의 인증 체계가 다르지만 백엔드 코드를 분기 없이 단일 Security 필터 체인으로 처리하기 위함
- Detail:
  - `JwtIssuerAuthenticationManagerResolver`로 `iss` 클레임 기준 라우팅
  - Keycloak issuer → Spring OAuth2 Resource Server (JWKS URI)
  - Service B issuer → Vault 공유 서명키(HMAC or RSA)로 검증
  - 공유 서명키는 Vault KV에서 런타임 주입, 키 교체 시 코드 변경 없음
  - 두 토큰 모두 최종적으로 `user` / `admin` 역할로 매핑
