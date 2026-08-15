# SubScribe Master

Subscription management REST API built with Java 21 and Spring Boot 3.5.

## Features

- JWT access + refresh authentication
- USER/ADMIN roles and owner-level authorization
- Subscription CRUD with soft delete and optimistic locking
- Pageable + JPA Specification filtering
- Payment history and transactional payment simulation
- CBU UZS currency conversion, cached with Caffeine and protected by a Resilience4j circuit breaker with last-known-rate
  fallback
- Daily payment reminder scheduler and notification strategy, both guarded by ShedLock so only one instance runs a job
  at a time
- Statistics (most expensive, monthly total, 6/12-month trend, category breakdown) and XLSX/CSV annual reports
- Admin overview with system-wide most-used-services ranking
- Liquibase migrations, OpenAPI/Swagger, validation, global exception handling
- 30+ JUnit 5 + AssertJ unit tests covering the service and domain layer

## Run

Requirements: Java 21, Maven 3.9+, Docker.

```bash
mvn clean test
mvn clean package
docker compose up --build
```

Swagger: `http://localhost:8080/swagger-ui.html`
OpenAPI: `http://localhost:8080/v3/api-docs`

## Environment

`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_ACCESS_MINUTES`, `SPRING_PROFILES_ACTIVE`. Never use the
development JWT secret in production.

## Authentication flow

Register/login returns an access token and refresh token. Send `Authorization: Bearer <accessToken>` to protected
endpoints. POST `/auth/refresh` with the refresh token to issue a new token pair.

## API

- POST `/auth/register`, `/auth/login`, `/auth/refresh`
- POST/GET/GET by id/PUT/DELETE `/subscriptions`
- POST `/subscriptions/{id}/payments`
- GET `/statistics`, `/statistics/most-expensive`, `/statistics/monthly-expense`,
  `/statistics/monthly-trend?months=6|12`
- GET `/reports/annual?year=2026&format=xlsx|csv`
- GET `/admin/statistics` (ADMIN only): users/subscriptions/total paid + top-used services ranking

## Project structure

Layered architecture: controller -> service -> repository -> entity, with dedicated security, client, currency,
scheduler, notification, mapper and specification packages.
