# Next Steps Checklist

다음 세션에서 바로 실행할 수 있는 작업 순서입니다.

## 현재 상태 요약

| 마일스톤 | 상태 |
|---------|------|
| M1 Bootstrap | ✅ 완료 |
| M2 Vault 연동 (#5) | ✅ 완료 |
| M2 MariaDB 마이그레이션 (#4) | 🔲 다음 작업 |
| M3 ~ M8 | 🔲 대기 |

## 다음 작업: `#4` MariaDB 스키마 마이그레이션

Flyway 또는 Liquibase로 초기 스키마 자동 마이그레이션 구성.
DB 연결 정보는 Vault에서 주입 (이미 완료).

### 시작 방법

```bash
# 1. NAS 서비스 확인 (Vault, MariaDB)
# 2. 환경 로드
direnv allow

# 3. 앱 실행 확인
./gradlew bootRun
```

## M3 이후 작업 순서

1. `#6` API auth framework — Multi-issuer JWT (Service A: Keycloak, Service B: Vault 공유 서명키)
2. `#8` Keycloak-Okta federation 검증 — #7의 사전 조건
3. `#7` Service A(React) UI auth 연동
4. `#9` 외부 REST API 동적 어댑터 핵심 추상화
5. `#10` 레퍼런스 Provider 구현 및 어댑터 검증
6. `#11` Prometheus/Grafana Observability
7. `#12` SonarQube 품질 게이트
8. `#13` CircleCI 파이프라인
9. `#14` 운영 런북/복구/검증 계획

## 환경 설정 참고

- **Java**: 17 (Temurin) — `/Library/Java/JavaVirtualMachines/temurin-17.jdk`
- **로컬 환경변수**: `.envrc` (direnv, gitignore됨)
  - `VAULT_ROLE_ID`, `VAULT_SECRET_ID`, `SPRING_PROFILES_ACTIVE=dev`
- **Vault**: `https://vault.hjeon.i234.me` / KV v2 / AppRole 인증
  - dev 시크릿 경로: `kv/backend/dev/db`
- **DB**: `jdbc:mysql://192.168.0.39:33306/backend` (username/password는 Vault 주입)
- **Spring Cloud**: 2025.0.0 (Spring Boot 3.5 호환)

## 주요 결정 사항 참고 (docs/DECISIONS.md)

- **D-008**: Stateless 인증 — httpOnly Cookie + Keycloak OIDC JWT
- **D-009**: 모노레포 (frontend/ + backend/)
- **D-010**: dev 포트 분리 (localhost:3000 / localhost:8080), prod 서브도메인 분리
- **D-011**: 인가 — Keycloak Realm Role `user` / `admin`
- **D-012**: 백엔드는 프론트엔드 독립적 설계 (Service A/B 전환 무변경)
- **D-013**: Multi-issuer JWT — `iss` 클레임 기준 Keycloak JWKS / Vault 공유 서명키 분기
