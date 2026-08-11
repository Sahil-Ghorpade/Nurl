# 🔗 NURL — Backend Service

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4+-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17+-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)](https://flywaydb.org/)

**NURL Backend** is a high-performance, production-ready RESTful API built with **Spring Boot 3**, **Spring Security**, and **PostgreSQL**. It powers short link generation, analytics tracking, custom link aliases, guest links, soft deletion with restoration controls, and JWT HttpOnly cookie authentication.

---

## ✨ Features

- ⚡ **Guest Short URLs**: Unauthenticated users can create short links with an automatic 24-hour expiration period.
- 🔐 **JWT & HttpOnly Cookie Auth**: Dual-token architecture (Access + Refresh tokens) configured with `SameSite=None` and `Secure=true` for cross-origin production deployments.
- 🔗 **Custom Aliases & Expiration**: Authenticated users can specify custom short codes and optional expiration timestamps.
- 📊 **Analytics & Click Tracking**: Incremental click counter tracking with analytics endpoint.
- 🗑️ **Soft Delete & Smart Restore**: Soft deletion support with restoration safety checks (expired links cannot be restored, valid links allow optional expiry updates).
- ⚙️ **Flyway Migrations**: Database schema versioning (V1 through V4) enabling seamless zero-downtime updates.
- 🧪 **100% Passing Test Suite**: Unit & integration test suites covering `AuthService`, `DashboardService`, `LinkService`, and `NurlApplication`.

---

## 🛠️ Tech Stack

- **Core Framework**: Java 21+, Spring Boot 3
- **Security**: Spring Security 6, JWT (io.jsonwebtoken), BCrypt Password Hashing
- **Database**: PostgreSQL, Spring Data JPA / Hibernate
- **Migrations**: Flyway Migration Engine
- **Build Tool**: Apache Maven
- **Testing**: JUnit 5, Mockito, Spring Boot Test

---

## 📡 API Reference

### 🔓 Public Endpoints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/link/public` | Create a 1-day temporary short link (No login required) |
| `GET` | `/{shortCode}` | Redirect short code to destination URL |
| `GET` | `/link/qr/{shortCode}` | Generate & serve QR code image for short code |
| `POST` | `/auth/login` | Authenticate user & issue HttpOnly JWT cookies |
| `POST` | `/auth/register` | Register a new user account |
| `POST` | `/auth/refresh` | Refresh access token via HttpOnly refresh cookie |
| `POST` | `/auth/logout` | Revoke session & clear cookies |

### 🔒 Authenticated Endpoints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/link` | Create a custom short link (Auth required) |
| `GET` | `/link/user` | Fetch active links owned by current user |
| `GET` | `/link/deleted` | Fetch soft-deleted links |
| `GET` | `/link/analytics/{shortCode}` | Fetch click analytics for owned short link |
| `PATCH` | `/link/{id}/restore` | Restore soft-deleted link (Optional expiry date body) |
| `DELETE` | `/link/{id}` | Soft-delete short link |
| `DELETE` | `/link/{id}/permanent` | Permanently delete link from database |
| `GET` | `/dashboard` | Fetch aggregated user metrics & total clicks |

---

## 🚀 Environment Configuration

Below are the supported environment variables for local development and Render deployment:

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/nurl` | PostgreSQL JDBC Connection String |
| `DB_USERNAME` | `postgres` | Database Username |
| `DB_PASSWORD` | `root` | Database Password |
| `JWT_SECRET` | *(Secret Key)* | Base64 HMAC SHA secret key |
| `FRONTEND_URL` | `http://localhost:5173` | Allowed CORS frontend origin |
| `AUTH_COOKIE_SAME_SITE` | `Lax` (`None` in Prod) | Cookie SameSite policy |
| `AUTH_COOKIE_SECURE` | `false` (`true` in Prod) | Cookie Secure flag (Requires HTTPS) |

---

## ⚙️ Quick Start (Local Setup)

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Sahil-Ghorpade/nurl.git
   cd nurl
   ```

2. **Ensure PostgreSQL is running** and database `nurl` exists.

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Run Unit & Integration Tests**:
   ```bash
   mvn test
   ```

---

## 📜 License

This project is open-source and available under the **MIT License**.
