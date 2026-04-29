# Next Steps Checklist

다음 세션에서 바로 실행할 수 있는 작업 순서입니다.

## 0. 사전 확인

- [x] 로컬 도구 버전 확인 (Java 17, Docker, kubectl, gh CLI)
- [ ] Synology NAS 내 의존 서비스 상태 확인 (Vault, Keycloak, MariaDB)
- [ ] GitHub 프로젝트/마일스톤/이슈 접근 확인

## 1. M1 완료

- [x] `#1` Backend bootstrap
- [x] `#2` Frontend scaffold
- [x] `#3` dev/prod config convention

## 2. M2 진행

- [ ] `#4` MariaDB + migration baseline
- [x] `#5` Vault secret integration

완료 조건:
- [x] Vault 기반 DB 연결 성공
- [ ] 초기 스키마 자동 마이그레이션 성공

### M2 다음 작업

- [ ] `#4` Flyway 또는 Liquibase로 초기 스키마 마이그레이션 구성

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

```bash
# NAS 서비스 상태 확인 후
./gradlew bootRun   # dev 프로파일 자동 적용 (direnv)
```

## 환경 설정 참고

- Java 17 (Temurin): `/Library/Java/JavaVirtualMachines/temurin-17.jdk`
- 로컬 dev 환경변수: `.envrc` (direnv, gitignore됨)
  - `VAULT_ROLE_ID`, `VAULT_SECRET_ID`, `SPRING_PROFILES_ACTIVE=dev`
- Vault: `https://vault.hjeon.i234.me` / KV v2 / AppRole 인증
- DB: `jdbc:mysql://192.168.0.39:33306/backend` (username/password는 Vault 주입)
