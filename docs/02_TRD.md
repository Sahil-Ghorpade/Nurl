# 02 — Technical Requirements Document (TRD) — v2
## LinkGuard — Intelligent URL Management & Analytics Platform

---

## 1. Architecture Overview

**Style:** Modular monolith (single deployable Spring Boot application, internally organized by domain module under `com.linkguard`).

```
                React Dashboard (SPA)
                         │
                         ▼
               Spring Boot REST API (com.linkguard)
    ┌───────────┬───────────┬────────────┬──────────────┬───────────────┬─────────────┐
    │   auth    │    url    │  redirect  │  analytics   │   security    │  qr / admin │
    │  package  │  package  │  package   │   package    │    package    │   package   │
    └───────────┴───────────┴────────────┴──────────────┴───────────────┴─────────────┘
                         │                                        │
                         ▼                                        ▼
                   PostgreSQL                                   Redis
            (system of record: users,                 (cache-aside for redirects,
             urls, click_events,                        rate-limit counters,
             blacklist, refresh_tokens)                  token blacklist)
```

---

## 2. Technology Stack (Latest Stable Versions at Implementation Time)

### Backend
- **Java** (Latest stable LTS version at implementation time)
- **Spring Boot** (Latest stable version at implementation time)
- Spring Security (JWT-based, stateless)
- Spring Data JPA + Hibernate
- MapStruct & Lombok
- Bean Validation (`jakarta.validation`)
- Flyway database migrations
- Maven

### Data Layer
- **PostgreSQL** (Latest stable version at implementation time)
- **Redis** (Latest stable version at implementation time)

### Frontend
- **React** (Latest stable version at implementation time)
- **Vite**, **Tailwind CSS** (Latest stable version at implementation time), **React Router**, **TanStack Query**, **Axios**, **Chart.js / Recharts**

### Testing & Docs
- JUnit 5, Mockito, Spring Boot Test
- OpenAPI / Swagger (`springdoc-openapi`)

### DevOps
- Docker & Docker Compose, GitHub Actions

---

## 3. Non-Functional Requirements

| Category | Requirement | Threshold | Enforcement / Measurement |
|---|---|---|---|
| **Performance** | Redirect latency | p95 < 100ms (cache hit), < 300ms (DB-only) | Actuator timers / load test |
| **Availability** | Uptime target | 99% (MVP instance) | Health checks (`/actuator/health`) |
| **Scalability** | Data volume | 100,000+ URLs, indexed `click_events` | Composite indexes on `(url_id, timestamp)` |
| **Concurrency** | Simultaneous users | 500 concurrent users | Stateless auth + HikariCP pool |
| **Security** | Auth & data protection | JWT + bcrypt + IP hashing (SHA-256 + salt) | Stateless Bearer auth, no raw IP storage |
| **Consistency** | Redirect isolation | 100% isolation from analytics failures | `@Async` + `REQUIRES_NEW` transaction |
| **Cache resilience**| Redis fault tolerance | Fail-open design | Fallback to DB lookup if Redis is unreachable |
| **Observability** | Request tracing | 100% correlation ID coverage | Servlet filter + Logback MDC filter |
| **Testability** | Coverage goal | ≥70% service-layer coverage | JaCoCo reports |

---

## 4. Key Technical Decisions & Rationale

| Decision | Rationale |
|---|---|
| Base62 short codes | Compact, URL-safe 6–8 character strings generated from numeric sequence/Snowflake IDs. |
| Collision handling via DB constraint + retry | Catch `DataIntegrityViolationException` and retry up to 3 times. |
| 302 Found over 301 Moved Permanently | Prevents aggressive client caching, ensuring expiration and status changes take immediate effect. |
| JWT + refresh token | Stateless authentication allowing horizontal scaling with token rotation for security. |
| IP hashing (SHA-256 + salt) | Anonymizes visitor IPs to record unique visits without storing PII. |
| Redis cache-aside | Accelerates high-volume redirect path while failing open to PostgreSQL if Redis is offline. |
| Asynchronous click recording | `@Async` decoupled analytics ingestion keeps the 302 redirect response unblocked. |

---

## 5. Security Deep Dive

### 5.1 Authentication & Session Security
- Access token expiry: ~15 minutes.
- Refresh token expiry: ~7–30 days, rotated on every usage.
- Stored refresh tokens are hashed in `refresh_tokens` table.
- Passwords hashed with bcrypt (cost factor ≥ 10).

### 5.2 CORS & CSRF
- Explicit allow-list for frontend origin in production.
- Stateless Bearer authentication in `Authorization` header mitigates ambient browser CSRF.

### 5.3 Injection & SSRF Protection
- Spring Data JPA parameterized queries prevent SQL injection.
- Destination URLs validated; internal/private IP ranges (`127.0.0.1`, RFC1918) blocked.

---

## 6. Standardized Error Handling

All errors are handled by `@ControllerAdvice` in `com.linkguard.common.exception.GlobalExceptionHandler` and return a standard payload shape:

```json
{
  "timestamp": "2026-08-02T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Short link does not exist or has been disabled",
  "path": "/abc123"
}
```

| HTTP Status | Meaning | Scenario |
|---|---|---|
| 400 | Bad Request | Malformed request body |
| 401 | Unauthorized | Missing/invalid/expired token or password |
| 403 | Forbidden | Accessing resource owned by another user |
| 404 | Not Found | Short code non-existent or disabled |
| 409 | Conflict | Email or custom alias already in use |
| 410 | Gone | Link past its `expiresAt` date |
| 422 | Unprocessable Entity | Validation failure or blacklisted domain |
| 429 | Too Many Requests | Exceeded Redis rate limit |
| 500 | Internal Server Error | Unhandled system exception |
