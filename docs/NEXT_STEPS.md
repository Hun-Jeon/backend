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

1. `#6` API auth framework — Multi-issuer JWT
   - Service A: Keycloak JWKS URI 검증
   - Service B: Vault 공유 서명키 검증
   - `JwtIssuerAuthenticationManagerResolver` 구현
2. `#8` Keycloak-Okta federation 검증 — #7의 사전 조건
3. `#7` Service A(React) UI auth 연동 — Keycloak OIDC, httpOnly Cookie
4. `#9` 외부 REST API 동적 어댑터 핵심 추상화
   - `AccessConnector` / `ProvisioningConnector` 인터페이스 정의
   - `ConnectorRegistry` 구현 (동적 등록/리로드)
   - `connector_config` 테이블 + Flyway 마이그레이션
   - 공통 WebClient 빌더 (Timeout/Retry/ErrorMapping/TraceId)
5. `#10` 레퍼런스 Provider 구현 및 어댑터 검증
6. `#11` Prometheus/Grafana Observability
   - Spring Actuator + Micrometer 연동
   - HikariCP 커넥션 메트릭 대시보드 추가
   - Redis 캐시 메트릭 연동
7. `#12` SonarQube 품질 게이트
8. `#13` CircleCI 파이프라인
9. `#14` 운영 런북/복구/검증 계획

## Redis 추가 작업 (M3~M4 사이)

- Kind 클러스터에 Redis StatefulSet 추가
- `spring-boot-starter-data-redis` 의존성 추가
- `spring.cache.type=redis` 설정
- HikariCP 설정값 계산 및 적용

```yaml
spring.datasource.hikari:
  maximum-pool-size: 5       # (151 - 10) ÷ 최대 파드 수
  minimum-idle: 0
  connection-timeout: 3000
  max-lifetime: 580000       # MariaDB wait_timeout보다 짧게
  keepalive-time: 60000
```

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
- **D-014**: Pod 분리 — Frontend/Backend(Deployment), Redis(StatefulSet) 각각 독립 운영
- **D-015**: 공유 캐시 — Redis (인-프로세스 캐시 사용 안 함)
- **D-016**: HikariCP `maximum-pool-size` = (DB max_connections - 10) ÷ 최대 파드 수
- **D-017**: 외부 API 동적 어댑터 — ConnectorRegistry + Vault 인증정보 런타임 주입
