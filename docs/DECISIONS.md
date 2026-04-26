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
- Decision: Keycloak OIDC 기반 로그인, Okta는 Keycloak의 IdP로 연동
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

