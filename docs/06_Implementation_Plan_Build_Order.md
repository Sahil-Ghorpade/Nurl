# 06 — Implementation Plan & Build Order — v2
## LinkGuard — Engineering Build Order & Step Roadmap

---

## 1. Technology Stack Summary (Latest Stable Versions at Implementation Time)

| Layer | Technology |
|---|---|
| Backend | Java (Latest LTS), Spring Boot (Latest Stable), Spring Security, Spring Data JPA, MapStruct, Lombok, Flyway |
| Database | PostgreSQL (Latest Stable), Redis (Latest Stable) |
| Frontend | React (Latest Stable), Vite, Tailwind CSS (Latest Stable), React Router, TanStack Query, Axios |
| Testing & Docs | JUnit 5, Mockito, OpenAPI / Swagger (`springdoc-openapi`) |
| DevOps | Docker, Docker Compose, GitHub Actions |

---

## 2. Backend Package Structure (`backend/src/main/java/com/linkguard`)

```
com.linkguard
├── auth/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── security/
├── url/
│   ├── controller/
│   ├── service/
│   └── repository/
├── redirect/
│   ├── controller/
│   └── service/
├── analytics/
│   ├── controller/
│   ├── service/
│   └── repository/
├── security/
│   ├── ratelimit/
│   └── blacklist/
├── qr/
│   └── service/
├── admin/
│   ├── controller/
│   └── service/
├── cache/
├── common/
│   ├── config/
│   ├── dto/
│   ├── exception/
│   ├── mapper/
│   └── util/
└── LinkGuardApplication.java
```

---

## 3. Phase 1 Milestone Plan

- **Step 1**: Root Repository Layout, Base Config Files & Locked Specification Docs in `/docs`.
- **Step 2**: Backend Spring Boot Baseline (`/backend`), Maven Setup, `application.yml`, PostgreSQL Connection, Redis Connection, Flyway Engine, and `LinkGuardApplication.java`.
- **Step 3**: Backend MDC Logging, Global Exception Handler (`com.linkguard.common.exception`), Swagger OpenAPI Config, and Complete Package Structure.
- **Step 4**: React SPA Baseline (`/frontend`), Vite Setup, Tailwind CSS, Axios, React Router, TanStack Query, and Frontend Folder Skeleton.
- **Step 5**: Docker Multi-stage Builds (`/docker`), `docker-compose.yml`, and GitHub Actions CI Workflow (`.github/workflows/ci.yml`).
