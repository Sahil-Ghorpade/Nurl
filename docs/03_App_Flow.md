# 03 — Application Flow — v2
## LinkGuard — Intelligent URL Management & Analytics Platform

---

## 1. High-Level User Journey

```
Landing Page
     │
     ▼
Register / Login ──────────────► Forgot Password
     │                                  │
     ▼                                  ▼
Dashboard (authenticated)      Reset Password Email
     │
     ├──► Create Short Link
     ├──► View / Search / Filter Links
     ├──► Open Link Detail (analytics, QR, settings)
     └──► Account / Admin Settings
```

---

## 2. Authentication Flow

```
POST /api/auth/register → Hash password (bcrypt) → Save User
        │
        ▼
POST /api/auth/login → Verify credentials → Issue JWT Access Token + Refresh Token
        │
        ▼
Client attaches Access Token in Authorization header: Bearer <accessToken>
        │
        ▼
On 401 Unauthorized → Call POST /api/auth/refresh → Rotate tokens
```

---

## 3. Short Link Creation Flow

```
POST /api/urls (with JSON payload)
        │
        ▼
Validate URL format (http/https, length <= 2048, no loopback/private IPs)
        │
        ▼
Check domain against blacklist table
        │
        ├── Custom Alias provided → Check uniqueness & reserved words → 409 Conflict if taken
        └── No Alias provided    → Generate Base62 short code from numeric sequence → Retry on collision
        │
        ▼
Persist Link record in PostgreSQL (status = ACTIVE)
        │
        ▼
Return 201 Created with shortUrl & shortCode
```

---

## 4. Redirect Flow (Core Hot Path)

```
GET /{shortCode}
        │
        ▼
Check Redis Cache for shortCode key
        │
   ┌────┴─────┐
   │ CACHE    │ CACHE MISS
   │ HIT      ▼
   │      Fetch originalUrl from PostgreSQL DB
   │          │
   │          ▼
   │      Populate Redis Cache with TTL (~1h)
   │          │
   └──────────┴─────────┐
                        ▼
   Verify Link Status & Expiration:
   - EXPIRED → Return 410 Gone
   - DISABLED / UNDER_REVIEW → Return 404 Not Found
   - PASSWORD_PROTECTED → Render password prompt
   - ACTIVE → Proceed
                        │
                        ▼
   Check Redis Rate Limiter (per IP hash)
                        │
                        ▼
   Dispatch Async Click Event (@Async thread pool):
   - SHA-256 hash client IP with salt
   - Parse User-Agent (browser, OS, device)
   - Store record in click_events table
                        │
                        ▼
   Return 302 Found (Location: originalUrl)
```

---

## 5. Analytics Pipeline Flow

```
Redirect Event Triggered
        │
        ▼
Capture Request Headers (User-Agent, Referrer, Remote IP)
        │
        ▼
Normalize Metadata:
- Parse User-Agent string to Browser, OS, Device category
- Generate IP Hash = SHA-256(Client IP + IP_HASH_SALT)
        │
        ▼
Asynchronous Persistence:
- Insert row into click_events table via independent transaction
        │
        ▼
Dashboard Aggregation:
- Client requests GET /api/urls/{id}/analytics
- Aggregates total clicks, unique visitors (COUNT DISTINCT ip_hash), breakdowns, and time-series data
```

---

## 6. QR Code Flow

```
Client sends GET /api/urls/{id}/qr
        │
        ▼
Backend verifies URL ownership
        │
        ▼
Generate QR code binary encoding the full short URL
        │
        ▼
Return HTTP 200 OK with Content-Type: image/png
```
