# LinkGuard Production Deployment & Infrastructure Guide

This document covers step-by-step instructions for deploying LinkGuard to production using Vercel, Render, Neon PostgreSQL, Upstash Redis, and Cloudinary.

---

## 1. Database Provisioning (Neon PostgreSQL)
1. Log in to [Neon Console](https://console.neon.tech).
2. Create a new PostgreSQL database named `linkguard_db`.
3. Copy the pooled connection string:
   `jdbc:postgresql://<neon-host>/linkguard_db?sslmode=require`
4. Flyway database migrations (`V1` to `V6`) will execute automatically on application startup.

---

## 2. Distributed Cache Provisioning (Upstash Redis)
1. Log in to [Upstash Console](https://console.upstash.com).
2. Create a new Redis database with TLS enabled.
3. Obtain the connection string (`rediss://default:<token>@<upstash-host>:6379`).

---

## 3. Backend Service Deployment (Render)
1. Connect your GitHub repository `Sahil-Ghorpade/LinkGuard` to Render.
2. Render will automatically detect `render.yaml` blueprint.
3. Configure Environment Variables in Render Dashboard:
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://<neon-host>/linkguard_db?sslmode=require`
   - `SPRING_DATASOURCE_USERNAME`: `<neon-username>`
   - `SPRING_DATASOURCE_PASSWORD`: `<neon-password>`
   - `REDIS_URL`: `rediss://default:<token>@<upstash-host>:6379`
   - `JWT_SECRET`: `<generated-256-bit-key>`
   - `FRONTEND_URL`: `https://linkguard.vercel.app`
4. Deploy web service. Actuator readiness probe `/actuator/health/readiness` will verify deployment health.

---

## 4. Frontend Application Deployment (Vercel)
1. Import `Sahil-Ghorpade/LinkGuard` into Vercel.
2. Set Root Directory to `frontend`.
3. Set Environment Variable:
   - `VITE_API_URL`: `https://linkguard-backend.onrender.com`
4. Deploy application. `vercel.json` ensures client-side routing rewrites (`/(.*)` -> `/index.html`).

---

## 5. Cloud Media Storage (Cloudinary - Optional)
Set placeholders in Render environment variables:
- `CLOUDINARY_CLOUD_NAME`: `<your_cloud_name>`
- `CLOUDINARY_API_KEY`: `<your_api_key>`
- `CLOUDINARY_API_SECRET`: `<your_api_secret>`
