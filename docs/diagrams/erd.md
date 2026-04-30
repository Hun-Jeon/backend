# ERD (v0.3)

플랫폼 운영 메타데이터의 ERD입니다. 상세 사양·DDL·결정 사항은 [`docs/DATA_MODEL.md`](../DATA_MODEL.md), 결정 배경은 [`docs/DECISIONS.md`](../DECISIONS.md) 참고.

**테이블 구성**

- Phase 1 (M2 #4 · 확정): `connector_config`, `audit_log`
- Phase 2 (M4): `provisioning_request`, `connector_health_log`
- 별도 (M2 #4): Spring Batch 메타테이블 (spring-batch-core 제공 스크립트, V3로 적용)

**주요 결정사항**

- PK는 `id`, FK는 `<참조테이블>_id` (D-018)
- 시간 컬럼은 `TIMESTAMP` + UTC 컨벤션 (D-020)
- JSON 컬럼은 `JSON` 타입 (D-021)
- `connector_health_log`는 status 변화 시점만 INSERT (D-024)

```mermaid
erDiagram
    CONNECTOR_CONFIG ||--o{ PROVISIONING_REQUEST : "issues"
    CONNECTOR_CONFIG ||--o{ CONNECTOR_HEALTH_LOG : "logs transition"

    CONNECTOR_CONFIG {
        bigint id PK "auto_increment"
        varchar name UK "varchar(64) unique"
        varchar connector_type "ACCESS / PROVISIONING / BOTH"
        varchar base_url "외부 API base URL"
        varchar auth_type "API_KEY / OAUTH2 / BASIC / BEARER"
        varchar vault_secret_path "Vault KV 경로 (인증정보)"
        int timeout_ms "default 5000"
        int retry_count "default 3"
        boolean enabled "활성화 여부"
        varchar description "옵션"
        timestamp created_at "UTC"
        timestamp updated_at "UTC"
    }

    PROVISIONING_REQUEST {
        bigint id PK
        bigint connector_config_id FK
        varchar request_type "PROVISION / DEPROVISION"
        varchar external_id "외부 시스템 jobId"
        varchar status "PENDING / IN_PROGRESS / SUCCESS / FAILED"
        json payload "요청 본문"
        text error_message "실패 시 메시지"
        varchar initiated_by "principal name"
        varchar trace_id "분산 추적 ID"
        timestamp created_at "UTC"
        timestamp updated_at "UTC"
        timestamp completed_at "UTC, nullable"
    }

    CONNECTOR_HEALTH_LOG {
        bigint id PK
        bigint connector_config_id FK
        varchar status "HEALTHY / DEGRADED / UNHEALTHY (transition only)"
        int response_time_ms
        text error_message
        timestamp checked_at "UTC"
    }

    AUDIT_LOG {
        bigint id PK
        varchar actor "principal name"
        varchar actor_type "USER / SERVICE_B"
        varchar action "수행 작업"
        varchar target_type "리소스 종류"
        varchar target_id "리소스 식별자"
        varchar result "SUCCESS / FAILURE"
        json metadata "추가 컨텍스트"
        varchar trace_id
        timestamp created_at "UTC"
    }
```
