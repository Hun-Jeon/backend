# Architecture

## 상위 구성

- Service A (Frontend): React + TypeScript + Vite, 자체 개발 UI. PoC 및 팀 내 검증 용도로 dev 환경 상시 유지
- Service B (External): 사내 기존 서비스. 리더십 결정에 따라 프로덕션 프론트엔드로 전환 가능
- Backend: Spring Boot 3.5, 순수 REST API. Service A/B 어느 쪽이든 무변경으로 대응
- Keycloak: Service A 인증의 표준 IdP 브로커 (Okta 페더레이션 포함)
- Okta: Keycloak 연동 대상 외부 IdP
- Vault: 런타임 시크릿 소스 (KV v2, AppRole 인증). Service B와의 JWT 공유 서명키 저장소 역할도 담당
- MariaDB: 애플리케이션 데이터 저장소
- Prometheus/Grafana: 메트릭 수집 및 시각화
- SonarQube: 정적 분석/품질 게이트
- CircleCI: 빌드/테스트/분석/배포 파이프라인

## 레포 구조 (모노레포)

```
/
├── frontend/        # React + TypeScript + Vite
└── backend/         # Spring Boot 3.5 (현재 레포 루트)
```

## 환경별 도메인 구성

| 환경 | Frontend | Backend |
|------|----------|---------|
| dev  | `localhost:3000` | `localhost:8080` |
| prod | `app.hjeon.i234.me` | `api.hjeon.i234.me` |

dev 환경에서는 Vite의 `/api` proxy를 통해 CORS 없이 백엔드와 통신한다.

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

### API 인가 흐름

- 인증 성공 후 `user` / `admin` 역할 기반 인가 처리 (두 클라이언트 공통)
- 인증/인가 실패는 표준 에러 응답 반환

## 인가(Authorization)

- Keycloak Realm Role 기준으로 `user` / `admin` 2단계 분리
- 역할은 Keycloak에서 중앙 관리, JWT claim으로 Backend에 전달

## 외부 연동 구조 (동적 어댑터)

외부 REST API를 코드 변경 없이 동적으로 등록·관리·호출할 수 있는 어댑터 구조를 지향한다.

- `ExternalApiAdapter` 인터페이스 기준으로 Provider별 구현체 분리
- Provider 설정(Base URL, 인증 방식, 헤더 등)은 런타임에 Vault에서 주입
- Provider별 인증 방식: API Key / OAuth2 Client Credentials / Basic 중 선택 적용
- 공통 HTTP Client 정책 일괄 적용:
  - Timeout / Retry
  - Error Mapping (표준 에러 응답 변환)
  - Trace ID Logging
- 신규 Provider 추가 시 어댑터 구현만으로 확장, 핵심 서비스 변경 없음

## 배포/운영 방향

- Kubernetes 기준 배포 매니페스트(또는 Helm)로 `dev/prod` 분리
- Vault 연동을 통해 환경별 시크릿 주입
- 관측성 및 품질 게이트를 CI/CD 파이프라인에 통합
