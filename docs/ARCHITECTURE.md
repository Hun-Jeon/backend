# Architecture

## 상위 구성

- Service A (Frontend): React + TypeScript + Vite, 자체 개발 UI. PoC 및 팀 내 검증 용도로 dev 환경 상시 유지
- Service B (External): 사내 기존 서비스. 리더십 결정에 따라 프로덕션 프론트엔드로 전환 가능
- Backend: Spring Boot 3.5, 순수 REST API. Service A/B 어느 쪽이든 무변경으로 대응
- Keycloak: Service A 인증의 표준 IdP 브로커 (Okta 페더레이션 포함)
- Okta: Keycloak 연동 대상 외부 IdP
- Vault: 런타임 시크릿 소스 (KV v2, AppRole 인증). Service B와의 JWT 공유 서명키 저장소 역할도 담당
- MariaDB: 애플리케이션 데이터 저장소
- Redis: 외부 API 응답 공유 캐시 (멀티 파드 환경에서 파드 간 캐시 일관성 보장)
- Prometheus/Grafana: 메트릭 수집 및 시각화
- SonarQube: 정적 분석/품질 게이트
- CircleCI: 빌드/테스트/분석/배포 파이프라인

## 레포 구조 (모노레포)

```
/
├── frontend/        # React + TypeScript + Vite (Service A)
└── backend/         # Spring Boot 3.5 (현재 레포 루트)
```

## 환경별 도메인 구성

| 환경 | Frontend (Service A) | Backend |
|------|----------------------|---------|
| dev  | `localhost:3000`     | `localhost:8080` |
| prod | `app.hjeon.i234.me`  | `api.hjeon.i234.me` |

dev 환경에서는 Vite의 `/api` proxy를 통해 CORS 없이 백엔드와 통신한다.

## Kubernetes Pod 구성

Frontend, Backend, Redis는 각각 별도 Deployment/StatefulSet으로 운영한다. 한 Pod에 묶지 않는다.

| 컴포넌트 | Kind | 비고 |
|---------|------|------|
| Backend | Deployment | replicas N, HPA 가능 |
| Frontend (Service A) | Deployment | replicas N, HPA 가능 |
| Redis | StatefulSet | replicas 1, PVC로 데이터 보존 |

컴포넌트별로 CPU/메모리 `resources.requests/limits`를 독립 설정하고, 장애 격리 및 독립 스케일 아웃을 보장한다.

## 클라이언트 유형별 인증 전략

백엔드는 `JwtIssuerAuthenticationManagerResolver`를 통해 JWT `iss` 클레임 기준으로 검증기를 분기한다.

```
JWT 수신
  ├─ iss = Keycloak  →  Keycloak JWKS URI로 검증  (Service A)
  └─ iss = Service B →  Vault 공유 서명키로 검증   (Service B)
              ↓
       user / admin 역할 매핑 → 인가 처리
```

### Service A 인증 흐름 (브라우저, httpOnly Cookie + OIDC)

1. 사용자 → Service A(React) 접속
2. Service A → Keycloak OIDC 로그인 리다이렉트
3. Keycloak → Okta(IdP) 페더레이션 인증
4. 인증 완료 → Backend `POST /api/auth/session` 호출 → httpOnly Cookie 발급
5. Service A → 이후 API 호출 시 Cookie 자동 첨부
6. Backend `JwtCookieFilter`: Cookie → `Authorization: Bearer` 변환 → Keycloak JWKS로 검증

### Service A 로그아웃

1. Service A → Backend `DELETE /api/auth/session` 호출
2. Backend → Cookie 즉시 만료 처리

### Service B 인증 흐름 (서버-서버, Vault 공유 서명키 JWT)

1. Service B → Vault에서 공유 서명키 조회
2. Service B → 서명키로 JWT 생성 후 Backend 호출 (`Authorization: Bearer`)
3. Backend → Vault에서 동일 서명키 조회 → JWT 검증
4. 검증 성공 → 클레임 기반 역할 매핑 → 인가 처리

> 공유 서명키는 Vault KV에 저장되며, 키 교체 시 코드 변경 없이 Vault에서만 갱신

### CORS 관리

CORS allowed-origins는 코드가 아닌 설정 파일로 외부화한다. 클라이언트 전환 또는 추가 시 배포 없이 설정만 변경한다.

```yaml
# application-dev.yml
cors.allowed-origins:
  - http://localhost:3000     # Service A (dev)

# application-prod.yml
cors.allowed-origins:
  - https://app.hjeon.i234.me        # Service A (prod)
  - https://service-b.company.com    # Service B 전환 시 추가
```

### API 인가 흐름

- 인증 성공 후 `user` / `admin` 역할 기반 인가 처리 (두 클라이언트 공통)
- 인증/인가 실패는 표준 에러 응답 반환

## 인가(Authorization)

- Keycloak Realm Role 기준으로 `user` / `admin` 2단계 분리
- 역할은 Keycloak에서 중앙 관리, JWT claim으로 Backend에 전달

## 외부 연동 구조 (동적 어댑터)

외부 REST API를 코드 변경 없이 동적으로 등록·관리·호출할 수 있는 어댑터 구조를 지향한다.
특정 외부 시스템(Okta, Saviynt 등)에 종속되지 않으며, 어드민 UI에서 커넥터를 추가/수정하면 앱 재시작 없이 반영된다.

### 인터페이스 구조

```
ConnectorRegistry
  ├─ AccessConnector (조회용)
  │    ├─ fetchAccesses(query)
  │    ├─ fetchAccessById(externalId)
  │    ├─ getConnectorType()
  │    └─ healthCheck()
  └─ ProvisioningConnector (실행용)
       ├─ provision(request)
       ├─ deprovision(request)
       ├─ getProvisioningStatus(jobId)
       └─ getConnectorType()
```

### 동작 방식

- Provider 설정(Base URL, 인증 방식, Vault 경로 등)을 `connector_config` 테이블에 저장
- 인증정보(API Key, Client Secret 등)는 DB 저장 금지. `vault_secret_path`만 저장하고 런타임에 Vault에서 조회
- 어드민 UI에서 커넥터 추가 → DB 저장 → Vault 인증정보 조회 → WebClient 빌드 → Registry 등록
- 설정 변경 시 `/api/connectors/{id}/reload` 호출로 재시작 없이 반영

### 공통 HTTP Client 정책

모든 Provider에 일괄 적용:
- Timeout / Retry
- Error Mapping (표준 에러 응답 변환)
- Trace ID Logging

### 초기 구현체 (예시)

신규 Provider 추가 시 인터페이스 구현만으로 확장 가능. 핵심 서비스 변경 없음.

## 캐시 전략

외부 API 응답 캐시는 멀티 파드 환경에서 파드 간 일관성을 보장하기 위해 Redis를 공유 캐시로 사용한다.

- 캐시 구현: Spring Cache + Redis (`spring-boot-starter-data-redis`)
- TTL: 외부 API 특성에 따라 캐시별 개별 설정 (기본 5분)
- 수동 무효화: Prov/De-prov 액션 완료 후 `@CacheEvict` 자동 무효화, 어드민 전체 갱신 버튼 제공
- 캐시 키: 조회 필터 조합 기준 (예: `accesses::status=ACTIVE&resource=APP_A`)
- 인-프로세스 캐시(Caffeine)는 사용하지 않음. 파드 간 불일치 방지

## DB 커넥션 관리

멀티 파드 환경에서 총 커넥션 수가 DB max_connections를 초과하지 않도록 HikariCP를 설정한다.

```
총 커넥션 = 파드 수 × maximum-pool-size ≤ DB max_connections - 관리 여유(10)
```

- `maximum-pool-size`: `(DB max_connections - 10) ÷ 최대 파드 수`로 계산
- `minimum-idle`: 0 (유휴 시 커넥션 반환)
- `connection-timeout`: 3000ms (장애 시 빠른 실패)
- `max-lifetime`: MariaDB `wait_timeout`보다 짧게 설정
- `@Transactional` 범위는 최대한 짧게 유지. 외부 API 호출은 반드시 트랜잭션 밖에서 실행
- Grafana에서 `hikaricp_connections_active`, `hikaricp_connections_pending` 지표로 실시간 모니터링

## 배포/운영 방향

- Kubernetes 기준 배포 매니페스트(또는 Helm)로 `dev/prod` 분리
- Vault 연동을 통해 환경별 시크릿 주입
- 관측성 및 품질 게이트를 CI/CD 파이프라인에 통합
