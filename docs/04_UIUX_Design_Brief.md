# 04 — UI/UX Design Brief — v2
## LinkGuard — Intelligent URL Management & Analytics Platform

---

## 1. Design Philosophy

The frontend for **LinkGuard** is a clean, responsive visualization and management dashboard for URL intelligence and analytics.

**Design Principles:**
- High data legibility with generous spacing around tables and charts.
- Restrained color palette using standard semantic status indicators (Green = Active, Red = Expired/Disabled, Amber = Under Review).
- Perceived speed optimization using skeleton loaders and optimistic UI state updates.
- Mobile-friendly responsive layout.

---

## 2. Color & Visual System Tokens

| Category | Token | Value |
|---|---|---|
| Surface | Neutral Background | Slate / Zinc light/dark neutrals |
| Primary Accent | Brand Accent | Indigo / Blue primary interactive state |
| Status Active | Success | Emerald green |
| Status Expired | Danger / Expired | Red |
| Status Warning | Review / Warning | Amber |
| Typography | Body / Headings | Inter or System Sans-serif stack |
| Monospace | Codes / Short URLs | Fira Code / JetBrains Mono |

---

## 3. Core Screen Layouts

### 3.1 Authentication Screen Layout
```
+-----------------------------------------+
|                LinkGuard                |
|                                         |
|          Sign in to your account        |
|                                         |
|   Email      [_______________________]  |
|   Password   [_______________________]  |
|                                         |
|              [  Sign In  ]              |
+-----------------------------------------+
```

### 3.2 Main Dashboard Layout
```
+---------+-------------------------------------------------------+
| Sidebar |  Topbar: [Search links...]           [+ Create Link]  |
|         +-------------------------------------------------------+
| Dashboard|  +----------+ +----------+ +----------+ +----------+ |
| Links    |  | Total    | | Today's  | | Unique   | | Top      | |
| Admin    |  | Links    | | Clicks   | | Visitors | | Link     | |
| Settings |  | 42       | | 128      | | 94       | | /dev     | |
|          |  +----------+ +----------+ +----------+ +----------+ |
|          |                                                       |
|          |  +----------------------+  +------------------------+ |
|          |  | Top Countries        |  | Browser Distribution   | |
|          |  +----------------------+  +------------------------+ |
+---------+-------------------------------------------------------+
```

---

## 4. Frontend Application Architecture (`/frontend`)

```
frontend/src/
├── components/
│   ├── ui/            # Reusable UI components (Button, Input, Modal, Table, StatCard)
│   ├── charts/        # Chart wrappers (LineChart, BarChart, PieChart)
│   └── layout/        # Shell layout components (Sidebar, Topbar)
├── pages/
│   ├── auth/          # Login & Register views
│   ├── dashboard/      # Main Dashboard view
│   ├── links/          # Links List & Detail views
│   └── public/         # Password prompt & Expired views
├── hooks/              # Custom TanStack Query & state hooks
├── lib/                # Axios instance configuration & helper utilities
├── context/            # AuthContext provider
└── App.jsx
```
