# System Architecture

3-tier 구조로 클라이언트, Spring Boot 백엔드, 인프라, 운영 스택을 한눈에 정리합니다.
Multi-issuer JWT 분기는 백엔드 내부 `JwtIssuerAuthenticationManagerResolver`에서 일어나며, 이후 도메인 모듈(Connector / Cache / Data)로 이어집니다.

**상태 표기**

- 실선 + teal 보더: 완료
- 실선 + orange 보더: 진행 중 / 다음 작업
- 점선 + gray 보더: 대기

```mermaid
flowchart TB

    subgraph CLI[" CLIENTS "]
        direction LR
        A["Service A<br/>React + TS + Vite<br/>localhost:3000 / app.hjeon.i234.me<br/>httpOnly Cookie · OIDC"]
        B["Service B<br/>Server-to-Server<br/>JWT Bearer · Vault 공유 서명키<br/>전환 검토 대상"]
    end

    subgraph BE[" BACKEND · Spring Boot 3.5 · Java 17 "]
        direction TB
        F["JwtCookieFilter<br/>Cookie → Authorization: Bearer"]
        R{"JwtIssuerAuthenticationManagerResolver<br/>iss claim 기준 분기"}
        M["Role Mapping<br/>user / admin"]
        subgraph DOMAIN[" Domain Modules "]
            direction LR
            CR["ConnectorRegistry<br/>(M4 동적 어댑터)"]
            CA["Cache Layer<br/>Spring Cache + Redis"]
            DA["Data / JPA<br/>HikariCP · Flyway #4"]
        end
        F --> R --> M
        M --- CR
        M --- CA
        M --- DA
    end

    subgraph INF[" INFRASTRUCTURE · Synology NAS "]
        direction LR
        K["Keycloak + Okta<br/>OIDC · Realm Role · M3"]
        V["Vault<br/>KV v2 · AppRole · ✓ M2"]
        DB[("MariaDB<br/>192.168.0.39:33306<br/>스키마 마이그 #4")]
        RD[("Redis<br/>StatefulSet · 공유 캐시")]
    end

    subgraph OPS[" OPS · CI/CD · OBSERVABILITY "]
        direction LR
        P["Prometheus + Grafana<br/>(M5)"]
        S["SonarQube<br/>(M6)"]
        CI["CircleCI<br/>(M7)"]
        K8["Kubernetes<br/>Deployment + StatefulSet"]
    end

    A -- "Cookie" --> F
    B -- "Bearer" --> R
    R -. "JWKS URI" .-> K
    R -. "shared signing key" .-> V
    DA --> DB
    CA --> RD

    classDef done stroke:#0ea5a4,stroke-width:2px,fill:#ecfeff
    classDef active stroke:#c2410c,stroke-width:2px,fill:#fff7ed,stroke-dasharray:4 3
    classDef pend stroke:#9ca3af,stroke-dasharray:4 3,fill:#f9fafb

    class V done
    class DB,DA,B active
    class A,K,RD,P,S,CI,K8,CR,CA pend
```
