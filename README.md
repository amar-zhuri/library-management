# Library Management System

Full-stack library manager with JWT authentication, role-based admin tools, AI-assisted insights, rich search, and email notifications. The backend is Spring Boot 4 + PostgreSQL; the frontend is React + TypeScript + Vite with Tailwind styling.

## Features
- **Authentication & Security:** JWT auth with refresh-less sessions, role-based access (`USER`, `ADMIN`), email verification, password reset, logout with token blacklist, CORS configured for common local hosts.
- **Book Management:** CRUD with genres, reading status, pricing, metadata, personal stats, quick search, and an admin-curated global library that users can claim books from.
- **Search & Discovery:** Advanced filtering (title/author/ISBN, genre/status, price/year/pages ranges), suggestions, paged results, and user/system-wide search endpoints for admins.
- **Recommendations & AI:** Rule-based recommendations by genre/author/similar users plus optional OpenAI-powered answers and insights when `openai.api.key` is provided.
- **Notifications & Newsletter:** Per-user notification preferences, unsubscribe tokens, password/email verification mails, admin newsletter drafting/sending with stats.
- **Admin Console:** User list/detail, system-level book management, delete-anything, system stats, AI insights, and newsletter management.
- **Frontend:** React 19 + TypeScript SPA with protected routes, dashboards, book CRUD, library sharing, search, settings, admin pages, and unsubscribe flows.

## Tech Stack
- **Backend:** Java 21, Spring Boot 4.0, Spring Security, Spring Data JPA, Validation, Mail, Lombok, Springdoc OpenAPI, PostgreSQL.
- **Frontend:** React + TypeScript + Vite, Tailwind CSS, React Router, React Hook Form, Zod, Axios, React Hot Toast.
- **Tooling:** Maven wrapper, Docker/Docker Compose, nginx for static frontend hosting.

## Project Layout
- `src/main/java/com/library/library_management/` — Spring Boot app code (controllers, services, security, repositories, entities, DTOs).
- `src/main/resources/application.yml` — default config (dev profile active).
- `src/main/java/com/library/library_management/API_REFERENCE.md` — detailed endpoint reference.
- `front-end/` — React SPA (Tailwind, Vite). `src/utils/constants.ts` holds the API base URL.
- `docker-compose.yml` — Postgres + backend + frontend stack. Root `Dockerfile` builds the backend; `front-end/Dockerfile` builds the SPA.

## Getting Started (Local)
Prerequisites: Java 21, Maven (wrapper included), Node 20+, npm, Docker (optional), PostgreSQL 16+.

1) **Configure environment**  
The default `application.yml` points at a local Postgres instance on port 5432 and runs with `spring.profiles.active=dev`, which seeds sample data. Override via env vars or CLI flags (examples):
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/library_db
SPRING_DATASOURCE_USERNAME=library_user
SPRING_DATASOURCE_PASSWORD=123456
JWT_SECRET=your-long-secret
JWT_EXPIRATION=86400000
OPENAI_API_KEY=sk-...
OPENAI_API_URL=https://api.openai.com/v1/chat/completions
OPENAI_MODEL=gpt-4.1-nano
APP_BASE_URL=http://localhost:8081
APP_FRONTEND_URL=http://localhost:5173
SPRING_MAIL_USERNAME=your@gmail.com
SPRING_MAIL_PASSWORD=app-password
```

2) **Start PostgreSQL**  
Use your own instance or run:
```
docker run --name library-db -e POSTGRES_DB=library_db \
  -e POSTGRES_USER=library_user -e POSTGRES_PASSWORD=123456 \
  -p 5432:5432 -d postgres:16-alpine
```

3) **Run the backend**  
```
./mvnw spring-boot:run
# starts on http://localhost:8081 (profile: dev, seeds sample data)
```

4) **Run the frontend (dev server)**  
```
cd front-end
npm ci
npm run dev -- --host --port 5173
# SPA served at http://localhost:5173, talks to http://localhost:8081/api
```
If deploying elsewhere, update `front-end/src/utils/constants.ts` → `API_BASE_URL`.

## Docker Compose
Bring up the full stack (Postgres + backend + nginx-hosted frontend):
```
docker-compose up --build
```
- Backend: `http://localhost:8081`  
- Frontend: `http://localhost:8080`  
Inject mail/OpenAI secrets via env vars or a `.env` file referenced by Compose if needed.

## API Documentation
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- Full endpoint list and DTOs: `src/main/java/com/library/library_management/API_REFERENCE.md`

## Seed Accounts (dev profile)
- Admin: `admin@library.com` / `admin123`
- User: `alice@example.com` / `password123`
- User: `bob@example.com` / `password123`

## Testing
- Backend: `./mvnw test`
- Frontend lint: `cd front-end && npm run lint`

## Deployment Notes
- Set a strong `JWT_SECRET` and real mail credentials; disable `dev` profile to skip seed data.  
- Provide `OPENAI_API_KEY` to enable LLM-backed endpoints; otherwise the rule-based services remain available.  
- For production URLs, align `app.base-url`, `app.frontend-url`, CORS origins, and the SPA `API_BASE_URL`.
