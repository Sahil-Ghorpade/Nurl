# 01 — Product Requirements Document (PRD) — v2
## LinkGuard — Intelligent URL Management & Analytics Platform

---

## 1. Product Summary

**Name:** LinkGuard  
**Category:** Backend-heavy SaaS utility (URL Shortener + Threat Inspection + Real-Time Analytics)  
**Elevator Pitch:** A production-grade URL management platform that lets users create secure, trackable short links with real-time analytics, security controls, QR generation, and abuse detection — designed around a modular monolith architecture.

**Why this matters (design rationale):** LinkGuard treats a link as a stateful, intelligent object with a security posture, a lifecycle, and an analytics profile — driving architecture decisions around caching strategy, async processing, rate limiting, and abuse detection.

---

## 2. Problem Statement

Freelancers, recruiters, marketers, and developers share links constantly but have no visibility into who clicked, when, from where, on what device, or whether a link is being abused. A shortened, trackable link (`linkguard.app/dev` instead of a long portfolio URL) turns a static string into a measurable, controllable asset.

---

## 3. Target Users

| Segment | Use Case |
|---|---|
| Students / Job seekers | Resume, portfolio, GitHub links with click tracking |
| Companies / Marketers | Campaign links, conversion tracking |
| Recruiters | Share/track access to interview documents |
| Content creators | YouTube/Instagram/LinkedIn/Twitter bio links |
| Developers | Temporary download links, API/doc sharing |

---

## 4. Functional Requirements

| Feature | Priority | Release | Notes |
|---|---|---|---|
| Register | High | MVP | Email + password, bcrypt hashed |
| Login | High | MVP | Issues access + refresh JWT |
| Refresh token flow | High | MVP | Rotation + reuse detection |
| Email verification | Low | MVP (optional) | Can ship post-MVP without blocking |
| Forgot / reset password | Medium | MVP | |
| Create short URL | High | MVP | Base62 or custom alias |
| Custom alias | High | MVP | Uniqueness + reserved-word check |
| Update / delete link | High | MVP | Owner-only |
| Public redirect endpoint | High | MVP | Core hot path |
| Basic analytics (clicks, unique visitors, last accessed, referrer, browser, OS, device) | High | MVP | |
| Dashboard (list, totals, most popular, recent activity) | High | MVP | |
| Password-protected links | High | v1-advanced | Security differentiator |
| Link expiration | High | v1-advanced | Scheduled job to flip status |
| Rate limiting | High | v1-advanced | Redis-backed counters |
| Blacklisted domains | Medium | v1-advanced | Checked at creation time |
| Expanded analytics (country, city, traffic, charts) | High | v1-advanced | |
| QR code generation | Medium | v1-advanced | Auto-generated per link |
| Search / filter / sort / pagination | High | v1-advanced | Table usability |
| Admin panel | Medium | v1-advanced | Disable links, ban users, manage blacklist |

---

## 5. Non-Functional Requirements (Summary)

| Category | Target | Why it matters |
|---|---|---|
| **Performance** | Redirect p95 < 100ms (cache hit), < 300ms (DB only) | Redirect is the highest-traffic, most latency-sensitive path |
| **Availability** | 99% (single-instance MVP; design allows horizontal scaling) | Realistic for single instance, architecture allows scaling |
| **Security** | JWT stateless auth, bcrypt password hashing, IP hashing (SHA-256 + salt) | Protects user links and visitor privacy |
| **Scalability** | Support 100k+ URLs and 500 concurrent users without redesign | Sets concrete schema/index/cache targets |
| **Concurrency** | Handle simultaneous writes to the same short code safely | Prevents duplicate/broken links under load |
| **Data integrity** | Analytics write failure must never affect the user's redirect | Core redirect promise is preserved |
| **Maintainability**| Modular monolith (`com.linkguard`), package-by-feature | Extensible modular architecture |

---

## 6. User Stories & Acceptance Criteria

### US-1: Account Registration
**As a** new user, **I want** to register with email and password, **so that** I can create and manage my own short links.
**Acceptance Criteria:**
- Given a valid, unused email and a password, the account is created and password is bcrypt-hashed.
- Given an email already in use, the API returns `409 Conflict`.
- Given an invalid email format or weak password, the API returns `422 Unprocessable Entity`.

### US-2: Login & Session
**As a** registered user, **I want** to log in and stay authenticated, **so that** I don't have to re-enter credentials constantly.
**Acceptance Criteria:**
- Valid credentials return an access token and refresh token.
- Invalid credentials return `401 Unauthorized`.
- Expired access token triggers refresh via `/refresh`; invalid refresh token forces login.

### US-3: Create Short Link
**As a** logged-in user, **I want** to shorten a URL with an optional custom alias, **so that** I can share a clean, trackable link.
**Acceptance Criteria:**
- Given a valid URL and no alias, a Base62 short code is generated and unique.
- Given a custom alias, system checks uniqueness and rejects reserved words with `409 Conflict`.
- Given a URL on the blacklist, creation is rejected with `422`.

### US-4: Visitor Redirect
**As a** visitor, **I want** clicking a short link to redirect me quickly to the original URL, **so that** the experience feels seamless.
**Acceptance Criteria:**
- Active link → `302 Found` redirect within latency targets.
- Expired link → `410 Gone`.
- Disabled / under-review link → `404 Not Found`.
- Password-protected link → password prompt response.

### US-5: View Analytics
**As a** user, **I want** to see who clicked my link and from where, **so that** I can understand my audience.
**Acceptance Criteria:**
- Dashboard shows total clicks, unique visitors (by hashed IP), last accessed time.
- Detail page shows breakdowns by country, browser, OS, device, and time-series data.
- No raw IP address is stored or exposed.

---

## 7. Success Metrics

| Metric | Target |
|---|---|
| Redirect latency (p95) | < 100ms (cache hit), < 300ms (DB only) |
| Test coverage (service layer) | ≥ 70% |
| Concurrent users supported | 500 without redesign |
| URLs supported | 100,000+ |
