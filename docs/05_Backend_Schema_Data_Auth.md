# 05 — Backend Schema, Data Model & Authentication — v2
## LinkGuard — Intelligent URL Management & Analytics Platform

---

## 1. Relational Database Schema (PostgreSQL)

### `users`
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(255) | UNIQUE, NOT NULL, INDEXED |
| password_hash | VARCHAR(255) | NOT NULL (bcrypt) |
| role | VARCHAR(20) | NOT NULL DEFAULT `USER` (`USER`, `ADMIN`) |
| email_verified | BOOLEAN | DEFAULT `false` |
| status | VARCHAR(20) | DEFAULT `ACTIVE` (`ACTIVE`, `BANNED`) |
| created_at | TIMESTAMP | DEFAULT `now()` |

### `urls`
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| user_id | BIGINT | FK → users.id (ON DELETE CASCADE), INDEXED |
| original_url | TEXT | NOT NULL |
| short_code | VARCHAR(30) | UNIQUE, NOT NULL, INDEXED |
| password_hash | VARCHAR(255) | NULLABLE (bcrypt) |
| expires_at | TIMESTAMP | NULLABLE |
| status | VARCHAR(20) | DEFAULT `ACTIVE` (`ACTIVE`, `EXPIRED`, `DISABLED`, `UNDER_REVIEW`) |
| created_at | TIMESTAMP | DEFAULT `now()` |

### `click_events`
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| url_id | BIGINT | FK → urls.id (ON DELETE CASCADE), INDEXED |
| timestamp | TIMESTAMP | NOT NULL, INDEXED |
| ip_hash | VARCHAR(64) | NOT NULL (SHA-256 hex) |
| country | VARCHAR(2) | NULLABLE |
| browser | VARCHAR(50) | NULLABLE |
| device | VARCHAR(20) | NULLABLE |
| os | VARCHAR(50) | NULLABLE |
| referrer | VARCHAR(2048) | NULLABLE |

### `blacklist`
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| domain | VARCHAR(255) | UNIQUE, NOT NULL |
| reason | VARCHAR(255) | NULLABLE |
| created_at | TIMESTAMP | DEFAULT `now()` |

### `refresh_tokens`
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| user_id | BIGINT | FK → users.id (ON DELETE CASCADE), INDEXED |
| token_hash | VARCHAR(255) | NOT NULL, UNIQUE |
| expires_at | TIMESTAMP | NOT NULL |
| revoked | BOOLEAN | DEFAULT `false` |
| created_at | TIMESTAMP | DEFAULT `now()` |

---

## 2. Indexing Strategy

| Table | Index | Rationale |
|---|---|---|
| `urls` | `short_code` (UNIQUE) | High-frequency lookup on critical redirect hot path |
| `urls` | `user_id` | Fast fetching of owner's links for dashboard views |
| `click_events` | `url_id` | Scopes aggregation queries to a specific URL |
| `click_events` | `(url_id, timestamp)` | Optimizes time-series range filters for analytics charts |
| `users` | `email` (UNIQUE) | Fast credential lookups during authentication |

---

## 3. Standardized Error Payload Format

All API errors return a standard JSON structure defined in package `com.linkguard.common.exception`:

```json
{
  "timestamp": "2026-08-02T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Short link does not exist or has been disabled",
  "path": "/abc123"
}
```

---

## 4. API Endpoints Overview

### Authentication
- `POST /api/auth/register` — Register user account
- `POST /api/auth/login` — Login user & return JWT tokens
- `POST /api/auth/refresh` — Refresh access token using valid refresh token

### Link Management
- `POST /api/urls` — Create new shortened link
- `GET /api/urls` — Get owner's links (paginated)
- `GET /api/urls/{id}` — Get single link detail
- `PUT /api/urls/{id}` — Update link settings
- `DELETE /api/urls/{id}` — Delete link

### Public Redirect
- `GET /{shortCode}` — Public redirect (302 Found)
- `POST /{shortCode}/verify` — Verify password for protected link

### Analytics & Tools
- `GET /api/urls/{id}/analytics` — Get link analytics summary & breakdowns
- `GET /api/urls/{id}/qr` — Download QR code binary PNG stream
