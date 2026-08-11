# CineGo

CineGo is a cinema booking system. Treat this file as the project map; load the
project skill that matches the task for detailed rules.

## Structure

- Backend: `./backend`
- Frontend: `./frontend`
- Project skills: `./.codex/skills`
- Product and database notes: `./docs`

## Project skills

- Use `frontend-design` for React pages, components, responsive UI, and visual
  states.
- Use `backend-api` when reading or changing Spring Boot behavior, DTOs,
  validation, persistence, security, or migrations.
- Use `api-integration` when connecting frontend code to backend endpoints.
- Use both `frontend-design` and `api-integration` for a frontend feature backed
  by live API data.
- Add `backend-api` when that feature also requires a backend contract change.

## Backend

The backend is a Java 21, Spring Boot, Spring Security, Spring Data JPA,
PostgreSQL, Flyway, Lombok, and MapStruct application.

Run all backend checks from the repository root through the Maven aggregator:

```text
mvn test
```

Alternatively, run backend-only commands from `./backend`:

```text
mvn test
mvn spring-boot:run
```

Inspect controllers, request/response DTOs, enums, validation, security, and the
global exception handler before implementing frontend integration.

## Frontend

Build the frontend in `./frontend` with React, TypeScript, Tailwind CSS, React
Router, and Axios. Keep page composition separate from reusable UI and API
modules.

## Business flow

```text
Movie -> Cinema -> Showtime -> Seat selection -> Booking -> Payment -> Ticket -> QR check-in
```

## Source-of-truth rules

- Never invent an endpoint, request field, response field, enum value, auth
  requirement, or error shape.
- Treat backend controllers and DTOs as the current API contract.
- Treat Flyway migrations and domain entities as the persistence source of
  truth.
- Preserve backend enum values in TypeScript. Map them to friendly labels only
  in the UI. For example, the backend currently uses `STANDARD`, `VIP`, and
  `COUPLE`; the UI may display these as Normal, VIP, and Sweetbox.
- Prefer changing the frontend adapter over changing a stable backend contract.
  Change the backend only when the product requirement cannot be satisfied by
  the existing API.

## Definition of done

- Cover loading, empty, error, unauthorized, conflict, and expired-booking
  states where relevant.
- Keep TypeScript strict and do not use `any`.
- Run the relevant frontend checks and backend tests for the files changed.
- Summarize any contract assumption or backend gap in the handoff.
