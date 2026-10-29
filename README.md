# SubScribe Master

**A Java backend for managing recurring subscriptions, payment history, and spending reports.**

Built with **Java 21 · Spring Boot 3.5 · PostgreSQL · Spring Security · JPA/Hibernate**. This portfolio project brings authentication, ownership checks, scheduled jobs, external API integration, and reporting into one REST API.

[Architecture notes](ARCHITECTURE.md) · [Application source](src/main/java/com/examplesubscribemaster) · [Tests](src/test/java/com/examplesubscribemaster)

## Highlights

| Area | Implementation |
| --- | --- |
| Authentication | JWT access and refresh tokens, USER/ADMIN roles |
| Subscription management | CRUD, soft deletion, optimistic locking, pagination and JPA Specification filters |
| Payments | Transactional payment simulation and payment history |
| Currency conversion | CBU exchange rates, Caffeine caching, Resilience4j circuit breaker and last-known-rate fallback |
| Background jobs | Payment reminders and exchange-rate refresh with ShedLock coordination |
| Reporting | Spending statistics, 6/12-month trends, annual XLSX/CSV reports |
| API documentation | OpenAPI/Swagger, validation and global exception handling |
| Persistence | PostgreSQL with Liquibase migrations |

## Architecture

```text
HTTP request → Controller / DTO → Service → JPA Repository → PostgreSQL
                                  ├─ Currency API + cache / fallback
                                  ├─ Report generation
                                  └─ Scheduled reminders
```

Controllers handle HTTP requests; services coordinate application operations; repositories handle persistence. Dedicated packages cover security, currency conversion, notifications, mapping, and filtering. See [ARCHITECTURE.md](ARCHITECTURE.md) for the design.

## Run locally

### Docker Compose

Install Docker with Compose, then run:

```bash
git clone https://github.com/shaxbozalimardonv87-cmyk/SubScribe-Master.git
cd SubScribe-Master
export JWT_SECRET="$(openssl rand -base64 48)"
docker compose -f docker-compose.yml up --build
```

The explicit Compose file uses the base configuration: PostgreSQL on host port **5432**, application on **8080**. The checked-in override changes the database host port to **5433** when running plain `docker compose up`; use the base command above for the setup shown here. Docker builds the application with Java 21.

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI document: <http://localhost:8080/v3/api-docs>

Stop the local stack with `docker compose -f docker-compose.yml down`.

### Java / Maven

With Java 21, Maven 3.9+, and a running PostgreSQL database named `subscribe_master`:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/subscribe_master
export DB_USERNAME=subscribe
export DB_PASSWORD=subscribe
export JWT_SECRET="$(openssl rand -base64 48)"
mvn spring-boot:run
```

Create the database and matching local credentials first, or start only the database with `docker compose -f docker-compose.yml up -d postgres`. Liquibase manages application tables.

## API overview

| Endpoint | Purpose |
| --- | --- |
| `POST /auth/register`, `/auth/login`, `/auth/refresh` | Register, sign in, refresh tokens |
| `/subscriptions` and `/subscriptions/{id}` | Manage subscriptions |
| `POST /subscriptions/{id}/payments` | Simulate and record a payment |
| `GET /statistics` | Spending overview |
| `GET /statistics/monthly-trend?months=6` | Monthly spending trend |
| `GET /reports/annual?year=2026&format=xlsx` | Download annual report |
| `GET /admin/statistics` | ADMIN-only overview |

Register or sign in, then use `Authorization: Bearer <accessToken>` for protected endpoints. Swagger contains the request schemas and full endpoint list.

## Configuration

| Variable | Purpose |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL connection |
| `JWT_SECRET` | Signing key; provide your own value |
| `JWT_ACCESS_MINUTES` | Access token lifetime; default 15 minutes |
| `PORT` | HTTP port; default 8080 |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile |

## Tests

```bash
mvn test
```

JUnit tests cover authentication, subscriptions, payments, currency conversion, statistics, reminders, and domain behavior. This documentation update did not execute the test suite.

## Scope

Payments are simulated; this is not a live payment gateway. The email notifier is an extension point and has no email provider configured. Local default database credentials are for development; use separate credentials and a unique JWT key for deployment.

## Author

**Shahbozjon Alimardonov** — Java Backend Developer

[GitHub](https://github.com/shaxbozalimardonv87-cmyk) · [LinkedIn](https://www.linkedin.com/in/shaxboz-alimardonov-774303381/) · [Email](mailto:shaxbozalimardonv87@gmail.com)
