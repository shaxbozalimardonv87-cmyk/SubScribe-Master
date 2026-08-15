# Architecture

## Layering

Controllers only translate HTTP to service calls. Business rules live in services; persistence is isolated behind Spring
Data repositories.

## Authentication/Authorization

BCrypt stores passwords. JWT contains user id, email and role. The filter builds a `UserPrincipal`. Subscription
services always query by both subscription id and authenticated user id, preventing cross-user access.

## Refresh tokens

Refresh tokens are persisted, have an expiry, and are tied to a user. A future production hardening step can
rotate/revoke tokens atomically.

## Currency

CBU is the external source. Rates are interpreted as UZS per unit of USD/EUR; UZS rate is 1. Conversion uses the current
cached rate at request time. For historical accounting, persisted payment-time FX rates would be preferable; this
implementation deliberately keeps the scope aligned with the task.

## Cache and resilience

Currency rates use Spring Cache + Caffeine with one-day expiry. The project includes Resilience4j circuit-breaker
configuration; external-client failure should be isolated from the application. A production deployment should persist
the last successful rate and use it as a durable fallback rather than relying only on in-memory cache.

## Scheduler

The 09:00 scheduler only delegates to `ReminderService`. Both it and the midnight currency-rate refresh job are
annotated with `@SchedulerLock` (ShedLock, backed by a `shedlock` table via Liquibase), so only one application instance
executes a given job even when scaled horizontally. `@EnableScheduling` and `@EnableCaching` are required for
`@Scheduled`/`@Cacheable` to be processed at all; both are now explicitly enabled (`ShedLockConfig`, `CacheConfig`).

## Notification strategy

`NotificationService` is the strategy abstraction. `LogNotifier` is the active implementation; `EmailNotifier` documents
the future provider integration.

## Soft delete and optimistic locking

Subscription deletion flips `isDeleted`. Ordinary queries exclude deleted rows. `@Version` prevents silent lost updates.

## Transactions

Create/update/delete/payment operations are transactional where state changes span multiple persistence operations.
Payment history is preserved when a subscription is cancelled.

## N+1

Associations are LAZY and normal owner queries do not expose entities. Statistics access the subscription category from
payment rows; if a large dataset requires it, projections/entity graphs should be introduced based on measured query
plans.

## Ambiguities

- `nextPaymentDate` is initialized as one periodicity after `startDate`, matching the explicit examples.
- Currency conversion uses the latest cached CBU rate at calculation time because the task does not require historical
  FX storage.
- Both XLSX (Apache POI) and CSV are exposed via `GET /reports/annual?format=xlsx|csv` (defaults to xlsx) so either
  export path from the task description is available.
- Admin analytics expose an aggregate overview plus a system-wide most-used-services ranking (`topServices`), matching
  the task's example without adding per-user admin drill-down, which was out of scope.
- The monthly trend window is a `months` query parameter (default 12, e.g. `?months=6`) rather than a fixed 12-month
  lookback.
