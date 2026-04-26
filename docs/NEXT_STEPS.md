# Next Steps Checklist

다음 세션에서 바로 실행할 수 있는 작업 순서입니다.

## 0. 사전 확인

- [ ] 로컬 도구 버전 확인 (Java 17, Docker, kubectl, gh CLI)
- [ ] Synology NAS 내 의존 서비스 상태 확인 (Vault, Keycloak, MariaDB)
- [ ] GitHub 프로젝트/마일스톤/이슈 접근 확인

## 1. M1 우선 진행

- [ ] `#1` Backend bootstrap
- [ ] `#2` Frontend scaffold
- [ ] `#3` dev/prod config convention

완료 조건:
- [ ] WSL/Mac 모두에서 기동 문서 기준 실행 가능
- [ ] `dev/prod` 설정 분리 확인

## 2. M2 진행

- [ ] `#4` MariaDB + migration baseline
- [ ] `#5` Vault secret integration

완료 조건:
- [ ] Vault 기반 DB 연결 성공
- [ ] 초기 스키마 자동 마이그레이션 성공

## 3. M3 진행

- [ ] `#6` API auth framework (JWT/API Key/Basic)
- [ ] `#7` UI auth via Keycloak
- [ ] `#8` Keycloak-Okta federation validation

완료 조건:
- [ ] 보호 API 인증 모드별 동작 확인
- [ ] UI 로그인/로그아웃/보호 라우트 확인

## 4. 후속 진행 (M4~M8)

- [ ] `#9`, `#10` 외부 연동 프레임워크 + 초기 provider
- [ ] `#11` Observability
- [ ] `#12` SonarQube 품질 게이트
- [ ] `#13` CircleCI 파이프라인
- [ ] `#14` 운영 런북/복구/검증 계획

## 5. 다음 세션 시작 명령

아래 순서로 시작:

1. `gh issue view 1 --repo Hun-Jeon/backend`
2. `gh issue view 2 --repo Hun-Jeon/backend`
3. `gh issue view 3 --repo Hun-Jeon/backend`
4. M1 범위 코드 작업 착수

