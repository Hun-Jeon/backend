# Project Spec (v0.4)

## 1) 기술 스택

### Backend
- Language: Java 17
- Framework: Spring Boot 3.5
- Database: MariaDB (`192.168.0.39:33306`)
- Cache: Redis (공유 캐시, 외부 API 응답 캐싱)
- Secret Management: HashiCorp Vault (KV v2, AppRole)
- Container/Orchestration: Docker + Kubernetes
- CI/CD: CircleCI
- Observability: Prometheus, Grafana
- Code Quality: SonarQube

### Frontend (Service A)
- Framework: React + TypeScript
- Build Tool: Vite
- 인증: Keycloak OIDC (Keycloak → Okta 페더레이션)

## 2) 환경 및 운영 범위

- Environment: `dev`, `prod`
- Main Dev Device: Windows Desktop (WSL)
- Secondary Dev Device: Macbook
- Homelab Runtime: Synology NAS
- Optional Validation Runtime: AWS (Terraform 기반 테스트 환경 활용 가능)

## 3) 프론트엔드 전략

백엔드는 특정 프론트엔드에 종속되지 않는 순수 REST API 서버다.

| 구분 | 설명 |
|------|------|
| Service A | 자체 개발 React 앱. PoC 및 팀 내 검증 용도. dev 환경 상시 유지 |
| Service B | 기존 사내 서비스. 리더십 결정에 따라 prod 프론트엔드로 전환 가능 |

- 클라이언트 전환 시 백엔드 코드 변경 없음
- CORS allowed-origins만 설정 파일에서 추가하면 됨
- Service A는 Service B로 전환 후에도 dev 환경에서 팀 내 PoC 용도로 유지

## 4) 인증/보안 요구사항

백엔드는 두 가지 클라이언트 유형을 동시에 지원한다.

| 클라이언트 | 방식 | 인증 수단 |
|-----------|------|-----------|
| Service A (브라우저) | OIDC | Keycloak JWT (httpOnly Cookie) |
| Service B (서버-서버) | 공유 서명키 | Vault에 저장된 서명키로 직접 JWT 생성 |

### Service A 인증 (브라우저)

- Keycloak OIDC 기반 로그인 (Keycloak → Okta 페더레이션)
- 인증 방식: httpOnly Cookie + JWT (Stateless)
  - 로그인 후 Backend가 `access_token` httpOnly Cookie 발급
  - 이후 요청마다 Cookie → `Authorization: Bearer` 변환 처리 (JwtCookieFilter)
- 로그인/로그아웃/보호 라우트 가드 동작 필요

### Service B 인증 (서버-서버)

- Service B가 Vault에서 공유 서명키를 조회하여 JWT 직접 생성
- Backend도 Vault에서 동일 서명키를 조회하여 JWT 검증
- `JwtIssuerAuthenticationManagerResolver`로 `iss` 클레임 기준 검증기 분기
- 키 교체는 Vault에서만 처리, 코드 변경 없음

### 공통 API 인증

- JWT, API Key, Basic 인증을 선택적으로 적용 가능해야 함
- 엔드포인트 단위로 인증 정책을 지정할 수 있어야 함
- 인증 실패/권한 실패 응답 포맷을 표준화해야 함

### 인가

- Service A / Service B 모두 최종적으로 `user` / `admin` 역할로 매핑
- JWT claim으로 역할 전달, Backend에서 엔드포인트별 인가 처리

### 시크릿 관리

- 애플리케이션 시크릿은 Vault를 통해 주입
- 코드/설정 파일 내 평문 시크릿 저장 금지

## 5) 외부 연동 요구사항

외부 REST API를 유연하게 관리하고 호출할 수 있는 동적 어댑터 구조를 목표로 한다.
특정 외부 시스템(Okta, Saviynt 등)에 종속되지 않으며 런타임에 Provider를 추가/교체 가능해야 한다.

- Provider 설정(Base URL, 인증 방식 등)은 DB(`connector_config` 테이블)에서 관리
- 인증정보(API Key, Client Secret 등)는 DB 저장 금지. `vault_secret_path`만 저장하고 Vault에서 런타임 조회
- `AccessConnector` (조회), `ProvisioningConnector` (실행) 인터페이스 분리
- `ConnectorRegistry`로 런타임 등록/조회/리로드. 재시작 없이 설정 변경 반영
- 신규 Provider 추가 시 인터페이스 구현만으로 확장 가능. 핵심 서비스 코드 변경 없음
- 공통 HTTP 정책(타임아웃/재시도/에러 매핑/추적 ID) 모든 Provider에 일괄 적용

## 6) 캐시 요구사항

멀티 파드 환경에서 외부 API 응답의 파드 간 캐시 일관성을 보장해야 한다.

- 공유 캐시: Redis (인-프로세스 캐시 사용 안 함)
- TTL: 캐시별 개별 설정 (기본 5분)
- 수동 무효화: Prov/De-prov 완료 후 자동 무효화, 어드민 전체 갱신 버튼 제공
- 어드민 UI에서 마지막 캐시 갱신 시각 표시

## 7) Kubernetes 운영 요구사항

- Frontend, Backend, Redis를 각각 별도 Deployment/StatefulSet으로 운영
- 컴포넌트별 독립 스케일 아웃 및 리소스 설정
- HikariCP `maximum-pool-size`를 파드 수 기준으로 계산하여 DB 커넥션 한도 초과 방지
- `@Transactional` 범위 최소화. 외부 API 호출은 트랜잭션 밖에서 실행

## 8) 비기능 요구사항

- 배포 자동화와 재현 가능한 빌드 파이프라인
- 운영 관측 가능성(지표, 대시보드, 알람)
- 코드 품질 게이트를 통한 PR 품질 관리
- 홈랩 한계를 고려한 현실적인 운영 절차(런북/복구/롤백)

## 9) 마일스톤 범위

- M1: Bootstrap (Dev/Prod Baseline) ✅
- M2: Data & Secret Integration (Vault ✅ / MariaDB migration 진행 중)
- M3: Auth Foundation (Multi-issuer JWT / Service A UI auth / Keycloak-Okta federation)
- M4: External Platform Connector Framework (동적 어댑터 + 초기 구현체)
- M5: Observability (Prometheus/Grafana)
- M6: Quality Gate (SonarQube)
- M7: CI/CD (CircleCI + K8s Deploy)
- M8: Production Readiness (Homelab + AWS Test)
