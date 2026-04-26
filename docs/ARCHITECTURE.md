# Architecture Draft

## 상위 구성

- Frontend: UI, OIDC 로그인 처리, Backend API 호출
- Backend: 비즈니스 API, 인증 정책 처리, 외부 플랫폼 연동
- Keycloak: UI 인증의 표준 IdP 브로커
- Okta: Keycloak 연동 대상 외부 IdP
- Vault: 런타임 시크릿 소스
- MariaDB: 애플리케이션 데이터 저장소
- Prometheus/Grafana: 메트릭 수집 및 시각화
- SonarQube: 정적 분석/품질 게이트
- CircleCI: 빌드/테스트/분석/배포 파이프라인

## 인증 흐름 요약

### UI 인증 흐름

1. 사용자 -> Frontend 접속
2. Frontend -> Keycloak OIDC 로그인 리다이렉트
3. Keycloak -> Okta(IdP) 인증 연동
4. 인증 완료 후 Frontend 세션/토큰 확보
5. Frontend -> Backend 보호 API 호출

### API 인증 흐름

- 엔드포인트별 정책으로 JWT / API Key / Basic 중 선택 적용
- 인증 성공 시 권한 검사 및 비즈니스 처리 진행
- 인증/권한 실패는 표준 에러 응답 반환

## 외부 연동 구조

- `Connector Interface`를 기준으로 Provider별 구현체 분리
- 공통 HTTP Client 정책 적용:
  - Timeout
  - Retry
  - Error Mapping
  - Trace ID Logging
- 신규 Provider 추가 시 핵심 서비스 변경 최소화

## 배포/운영 방향

- Kubernetes 기준 배포 매니페스트(또는 Helm)로 `dev/prod` 분리
- Vault 연동을 통해 환경별 시크릿 주입
- 관측성 및 품질 게이트를 CI/CD 파이프라인에 통합

