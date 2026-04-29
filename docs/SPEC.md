# Project Spec (v0.3)

## 1) 기술 스택

### Backend
- Language: Java 17
- Framework: Spring Boot 3.5
- Database: MariaDB (`192.168.0.39:33306`)
- Secret Management: HashiCorp Vault (KV v2, AppRole)
- Container/Orchestration: Docker + Kubernetes
- CI/CD: CircleCI
- Observability: Prometheus, Grafana
- Code Quality: SonarQube

### Frontend
- Framework: React + TypeScript
- Build Tool: Vite
- 인증: Keycloak OIDC (Keycloak → Okta 페더레이션)

## 2) 환경 및 운영 범위

- Environment: `dev`, `prod`
- Main Dev Device: Windows Desktop (WSL)
- Secondary Dev Device: Macbook
- Homelab Runtime: Synology NAS
- Optional Validation Runtime: AWS (Terraform 기반 테스트 환경 활용 가능)

## 3) 인증/보안 요구사항

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

## 4) 외부 연동 요구사항

외부 REST API를 유연하게 관리하고 호출할 수 있는 동적 어댑터 구조를 목표로 한다.

- Provider 등록/설정(Base URL, 인증 방식, 헤더 등)을 런타임에 주입 가능한 구조
- Provider별 인증 방식(API Key, OAuth2 Client Credentials, Basic 등)을 선택 적용
- 신규 Provider 추가 시 핵심 서비스 코드 변경 없이 어댑터 구현만으로 확장 가능
- 공통 HTTP 정책(타임아웃/재시도/에러 매핑/추적 ID) 모든 Provider에 일괄 적용
- Provider 설정 및 자격증명은 Vault를 통해 주입

## 5) 비기능 요구사항

- 배포 자동화와 재현 가능한 빌드 파이프라인
- 운영 관측 가능성(지표, 대시보드, 알람)
- 코드 품질 게이트를 통한 PR 품질 관리
- 홈랩 한계를 고려한 현실적인 운영 절차(런북/복구/롤백)

## 6) 마일스톤 범위

- M1: Bootstrap (Dev/Prod Baseline) ✅
- M2: Data & Secret Integration (Vault ✅ / MariaDB migration 진행 중)
- M3: Auth Foundation (API + UI)
- M4: External Platform Connector Framework
- M5: Observability (Prometheus/Grafana)
- M6: Quality Gate (SonarQube)
- M7: CI/CD (CircleCI + K8s Deploy)
- M8: Production Readiness (Homelab + AWS Test)
