# Project Spec (v0.2)

## 1) 기술 스택

- Language: Java 17
- Framework: Spring Boot 3.5
- Database: MariaDB (`192.168.0.39`)
- Secret Management: HashiCorp Vault
- Container/Orchestration: Docker + Kubernetes
- CI/CD: CircleCI
- Observability: Prometheus, Grafana
- Code Quality: SonarQube

## 2) 환경 및 운영 범위

- Environment: `dev`, `prod`
- Main Dev Device: Windows Desktop (WSL)
- Secondary Dev Device: Macbook
- Homelab Runtime: Synology NAS
- Optional Validation Runtime: AWS (Terraform 기반 테스트 환경 활용 가능)

## 3) 인증/보안 요구사항

### API 인증

- JWT, API Key, Basic 인증을 선택적으로 적용 가능해야 함
- 엔드포인트 단위로 인증 정책을 지정할 수 있어야 함
- 인증 실패/권한 실패 응답 포맷을 표준화해야 함

### UI 인증

- Keycloak OIDC 로그인 연동
- Keycloak에서 Okta(IdP) 연계 구성 사용
- 로그인/로그아웃/보호 라우트 가드 동작 필요

### 시크릿 관리

- 애플리케이션 시크릿은 Vault를 통해 주입
- 코드/설정 파일 내 평문 시크릿 저장 금지

## 4) 외부 연동 요구사항

- Okta, Saviynt 외부 API 연동 필요
- Provider 추가가 쉬운 어댑터(플러그인) 구조 지향
- 공통 HTTP 정책(타임아웃/재시도/에러 매핑/추적 ID) 적용

## 5) 비기능 요구사항

- 배포 자동화와 재현 가능한 빌드 파이프라인
- 운영 관측 가능성(지표, 대시보드, 알람)
- 코드 품질 게이트를 통한 PR 품질 관리
- 홈랩 한계를 고려한 현실적인 운영 절차(런북/복구/롤백)

## 6) 마일스톤 범위

- M1: Bootstrap (Dev/Prod Baseline)
- M2: Data & Secret Integration
- M3: Auth Foundation (API + UI)
- M4: External Platform Connector Framework
- M5: Observability (Prometheus/Grafana)
- M6: Quality Gate (SonarQube)
- M7: CI/CD (CircleCI + K8s Deploy)
- M8: Production Readiness (Homelab + AWS Test)

