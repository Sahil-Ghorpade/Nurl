# 🔗 NURL Backend — High-Performance URL Shortener & Analytics Engine

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4+-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17+-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)

**NURL** is an enterprise-grade, high-throughput RESTful backend service built with **Spring Boot 3**, **Spring Security**, and **PostgreSQL**. It provides instant URL shortening, custom alias reservation, real-time analytics, automated link expiration, soft deletion recovery, background retention cleanup, and secure JWT-based HttpOnly cookie authentication designed for modern web applications.

---

## 📋 Table of Contents

- [✨ Core Features](#-core-features)
- [🏗 System Architecture](#-system-architecture)
- [🔄 Application Execution Flows](#-application-execution-flows)
  - [1. User Authentication & Cookie Lifecycle](#1-user-authentication--cookie-lifecycle)
  - [2. Public & Authenticated Link Creation](#2-public--authenticated-link-creation)
  - [3. Short Link Redirection & Click Tracking](#3-short-link-redirection--click-tracking)
  - [4. Soft Delete, Expiration & Smart Restore](#4-soft-delete-expiration--smart-restore)
  - [5. Automated Background Cleanup](#5-automated-background-cleanup)
- [🗄 Database Schema & Migrations](#-database-schema--migrations)
- [📡 API Endpoint Reference](#-api-endpoint-reference)
- [⚙ Environment Configuration](#-environment-configuration)
- [🚀 Local Setup & Deployment Guide](#-local-setup--deployment-guide)
- [🧪 Testing & Verification](#-testing--verification)

---

## ✨ Core Features

- ⚡ **Guest Short URLs**: Unauthenticated users can generate short links with an enforced 24-hour expiration period without account registration.
- 🔐 **Dual-Token HttpOnly Auth**: Access & Refresh token rotation issued via secure, HttpOnly, SameSite-compliant cookies to mitigate XSS and CSRF vectors.
- 🎯 **Custom Short Aliases**: Authenticated users can reserve custom short codes with built-in reserved word collision checking.
- 📊 **Real-time Analytics**: Tracks total visits, link status (Active, Expired, Deleted), and owner-specific metrics.
- 🗑️ **Soft Delete & Expiration Protection**: Links can be soft-deleted to a trash bin. Restoration includes expiration checks to prevent reactivating expired links.
- 🧹 **Automated Retention Cleanup**: Scheduled Spring Cron scheduler (`LinkCleanupScheduler`) automatically purges expired soft-deleted links.
- 🐘 **Versioned Schema Migrations**: Managed PostgreSQL migrations via Flyway (`V1` to `V4`).

---

## 🏗 System Architecture

```mermaid
graph TD
    Client[Web / Mobile Client] -->|HTTPS REST API| SecurityFilter[Spring Security Filter Chain]
    SecurityFilter -->|JWT Verification| AuthFilter[JwtAuthenticationFilter]
    
    AuthFilter --> AuthCtrl[AuthController]
    AuthFilter --> LinkCtrl[LinkController]
    AuthFilter --> DashCtrl[DashboardController]
    
    AuthCtrl --> AuthService[AuthService]
    AuthCtrl --> AuthCookieService[AuthCookieService]
    LinkCtrl --> LinkService[LinkService]
    DashCtrl --> DashService[DashboardService]
    
    LinkService --> CodeGen[ShortCodeGenerator]
    LinkService --> LinkRepo[LinkRepository]
    AuthService --> UserRepo[UserRepository]
    AuthService --> TokenRepo[RefreshTokenRepository]
    
    LinkRepo --> PostgreSQL[(PostgreSQL Database)]
    UserRepo --> PostgreSQL
    TokenRepo --> PostgreSQL
    
    Scheduler[LinkCleanupScheduler] -->|Scheduled Cron| LinkRepo
```

---

## 🔄 Application Execution Flows

### 1. User Authentication & Cookie Lifecycle

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Client
    participant AuthController
    participant AuthService
    participant JwtService
    participant AuthCookieService
    participant DB as PostgreSQL Database

    User->>Client: Enter Email & Password
    Client->>AuthController: POST /auth/login
    AuthController->>AuthService: login(LoginRequest)
    AuthService->>DB: findByEmail(email)
    DB-->>AuthService: User Entity & Password Hash
    AuthService->>AuthService: Verify Password (BCrypt)
    AuthService->>JwtService: generateAccessToken(User)
    AuthService->>DB: Save Hashed RefreshToken
    AuthService-->>AuthController: LoginResult (Tokens)
    AuthController->>AuthCookieService: createAccessCookie() & createRefreshCookie()
    AuthCookieService-->>Client: Set-Cookie: nurl_access_token & nurl_refresh_token
```

---

### 2. Public & Authenticated Link Creation

```mermaid
flowchart TD
    Start[User Submits Long URL] --> CheckAuth{Is Authenticated?}
    
    CheckAuth -- No --> PublicFlow[Call POST /link/public]
    PublicFlow --> GenPublicCode[Generate 6-char Short Code]
    PublicFlow --> SetPublicExpiry[Set Expiration = Instant.now + 24 Hours]
    PublicFlow --> SavePublic[Save Link with user = null]
    SavePublic --> ReturnPublic[Return 201 Created + Short Link]
    
    CheckAuth -- Yes --> AuthFlow[Call POST /link]
    AuthFlow --> CheckAlias{Custom Alias Provided?}
    CheckAlias -- Yes --> ValidateAlias[Validate Reserved Words & Collisions]
    ValidateAlias --> SetAuthCode[Use Custom Short Code]
    CheckAlias -- No --> GenAuthCode[Generate 6-char Short Code]
    SetAuthCode --> SaveAuth[Save Link assigned to User]
    GenAuthCode --> SaveAuth
    SaveAuth --> ReturnAuth[Return 201 Created + Link Details]
```

---

### 3. Short Link Redirection & Click Tracking

```mermaid
sequenceDiagram
    autonumber
    actor Visitor
    participant Browser
    participant LinkController
    participant LinkService
    participant DB as PostgreSQL Database

    Visitor->>Browser: Visit http://nurl.onrender.com/abc123
    Browser->>LinkController: GET /abc123
    LinkController->>LinkService: redirectLink("abc123")
    LinkService->>DB: findByShortCodeAndDeletedFalse("abc123")
    alt Link Not Found
        DB-->>LinkService: Empty
        LinkService-->>LinkController: ResourceNotFoundException
        LinkController-->>Browser: Redirect to /not-found
    else Link Expired
        DB-->>LinkService: Link Entity (expiresAt < Now)
        LinkService-->>LinkController: LinkExpiredException
        LinkController-->>Browser: Redirect to /link-expired
    else Link Active
        DB-->>LinkService: Link Entity (Valid)
        LinkService->>LinkService: incrementClickCount()
        LinkService->>DB: save(link)
        LinkService-->>LinkController: Target Original URL
        LinkController-->>Browser: HTTP 302 Redirect (Location: Target URL)
    end
```

---

### 4. Soft Delete, Expiration & Smart Restore

```mermaid
flowchart LR
    Active[Active Link] -->|DELETE /link/{id}| Deleted[Soft Deleted Link]
    Deleted -->|PATCH /link/{id}/restore| RestoreCheck{Is Expired?}
    RestoreCheck -- Yes --> Reject[Throw BadRequestException: Cannot restore expired link]
    RestoreCheck -- No --> Restored[Restore to Active Status]
    Deleted -->|DELETE /link/{id}/permanent| HardDelete[Permanently Removed from DB]
```

---

### 5. Automated Background Cleanup

1. **Scheduler Trigger**: `LinkCleanupScheduler` executes daily via `@Scheduled(cron = "${app.cleanup.cron}")`.
2. **Retention Verification**: Queries soft-deleted links older than retention period (`retentionDays`).
3. **Purge Action**: Permanently deletes matching link records from PostgreSQL database to optimize disk storage.

---

## 🗄 Database Schema & Migrations

Database schema versioning is managed via **Flyway**:

- **`V1__create_initial_schema.sql`**: Creates `users`, `roles`, and `links` tables with constraints.
- **`V2__add_link_indexes.sql`**: Indexes `short_code` and `user_id` for fast query lookup performance.
- **`V3__create_refresh_tokens.sql`**: Introduces hashed `refresh_tokens` table for session management.
- **`V4__make_user_id_nullable_in_links.sql`**: Drops `NOT NULL` on `links.user_id` to allow guest link generation.

---

## 📡 API Endpoint Reference

### 🔓 Public Endpoints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/link/public` | Create a 1-day temporary short link (No auth required) |
| `GET` | `/{shortCode}` | Redirect short code to original target URL |
| `GET` | `/link/qr/{shortCode}` | Generate PNG QR code image for short code |
| `POST` | `/auth/login` | Authenticate user & set HttpOnly cookies |
| `POST` | `/auth/register` | Register new user account |
| `POST` | `/auth/refresh` | Refresh access token using refresh cookie |
| `POST` | `/auth/logout` | Revoke session & clear cookies |

### 🔒 Authenticated Endpoints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/link` | Create custom short URL (Auth required) |
| `GET` | `/link/user` | Retrieve active links owned by current user |
| `GET` | `/link/deleted` | Retrieve soft-deleted links owned by current user |
| `GET` | `/link/analytics/{shortCode}` | Get click analytics for specified link |
| `PATCH` | `/link/{id}/restore` | Restore soft-deleted link with optional new expiry |
| `DELETE` | `/link/{id}` | Soft delete link |
| `DELETE` | `/link/{id}/permanent` | Permanently delete link |
| `GET` | `/dashboard` | Retrieve user aggregated dashboard statistics |

---

## ⚙ Environment Configuration

| Variable | Description | Default (Local) | Production (Render) |
| :--- | :--- | :--- | :--- |
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/nurl` | Render PostgreSQL URL |
| `DB_USERNAME` | Database User | `postgres` | `nurl_user` |
| `DB_PASSWORD` | Database Password | `root` | Production Secret |
| `JWT_SECRET` | Base64 HMAC Key | *(Default Test Key)* | Production Secret |
| `FRONTEND_URL` | Allowed CORS Origin | `http://localhost:5173` | `https://nurl-1me1.onrender.com` |
| `AUTH_COOKIE_SAME_SITE` | Cookie SameSite | `Lax` | `None` |
| `AUTH_COOKIE_SECURE` | Cookie Secure Flag | `false` | `true` |
| `CLEANUP_CRON` | Retention Cron | `0 0 2 * * *` | `0 0 2 * * *` |

---

## 🚀 Local Setup & Deployment Guide

### Local Running
```bash
# 1. Clone backend repository
git clone https://github.com/Sahil-Ghorpade/nurl.git
cd nurl

# 2. Run application via Maven
mvn spring-boot:run
```

### Production Deployment (Render)
Ensure the following variables are configured in **Render Dashboard ➔ Backend Service Environment**:
```env
FRONTEND_URL=https://nurl-1me1.onrender.com
APP_BASE_URL=https://nurl.onrender.com
AUTH_COOKIE_SAME_SITE=None
AUTH_COOKIE_SECURE=true
```

---

## 🧪 Testing & Verification

Execute the test suite covering authentication, authorization, link shortening, analytics, and dashboard metrics:

```bash
mvn test
```

```
[INFO] Running io.github.sahilghorpade.nurl.auth.service.AuthServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running io.github.sahilghorpade.nurl.dashboard.service.DashboardServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running io.github.sahilghorpade.nurl.link.service.LinkServiceTest
[INFO] Tests run: 33, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running io.github.sahilghorpade.nurl.NurlApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
```

---

## 📜 License

Distributed under the **MIT License**.
