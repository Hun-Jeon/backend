# Backend Homelab Project

홈랩 환경에서 실무와 유사한 방식으로 운영 가능한 플랫폼을 목표로 하는 프로젝트입니다.

## 목표

- Java 17 + Spring Boot 3.5 기반 백엔드 구축
- Kubernetes 환경에서 Frontend / Backend 분리 배포
- HashiCorp Vault 기반 시크릿 관리
- MariaDB 연동 (`192.168.0.39`)
- API 인증 전략(JWT / API Key / Basic) 선택 적용
- Keycloak + Okta(IdP) 기반 UI 인증 연동
- 외부 REST API 동적 어댑터 구조 (Provider 코드 변경 없이 확장 가능)
- Prometheus / Grafana / SonarQube / CircleCI 통합

## 현재 확정된 운영 전제

- 메인 개발: Windows Desktop (WSL)
- 서브 개발: Macbook
- 홈랩 런타임: Synology NAS (LDAP, Vault, Keycloak, MariaDB 구동 중)
- 필요 시 AWS 테스트 환경 사용 가능 (Terraform 실습 환경 보유)
- 환경 분리는 `dev`, `prod` 2개만 사용

## 프로젝트 관리 현황

- GitHub Milestone `M1` ~ `M8` 생성 완료
- 초기 Issue 생성 및 라벨/의존관계 정리 완료

## 문서 가이드

- 상세 스펙: `docs/SPEC.md`
- 아키텍처 초안: `docs/ARCHITECTURE.md`
- 결정 사항(ADR 스타일): `docs/DECISIONS.md`
- 다음 작업 체크리스트: `docs/NEXT_STEPS.md`

## 시작 순서 (권장)

1. `docs/NEXT_STEPS.md`의 M1 체크리스트 수행
2. M1 완료 후 M2(DB/Vault) 진행
3. M2 완료 후 M3(API/UI 인증) 진행

