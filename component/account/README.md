# Account authentication

The account service implements email/password identity only. It owns `users`,
`user_credentials`, `refresh_tokens`, `email_verifications`, and `password_reset_tokens`
in the PostgreSQL database `kelarus_account`.

Application/business callers depend on `AuthenticationService`, `PasswordService`, and
`EmailVerificationService` interfaces. Their Spring-managed implementations live in
`service/impl/`; transaction boundaries remain on the implementation methods. Request
validation constraints are declared on the interface contracts. `TokenService` remains a
concrete technical support class for JWT creation, random tokens, and hashing.

## Local setup

Use the existing PostgreSQL 17 service, exposed on port 5434:

```powershell
docker compose --env-file docker/.env -f docker/services.yml up -d kelarus-platform-db
```

Set the existing Docker environment variables required by that compose file. On a new
PostgreSQL volume, `docker/postgres/init-account.sql` creates `kelarus_account` alongside
the existing `kelarus_platform` database. Initialization scripts do not run again on an
existing volume. For an existing database container, run the same idempotent script:

```powershell
Get-Content -Raw docker/postgres/init-account.sql | docker exec -i kelarus-platform-db psql -v ON_ERROR_STOP=1 -U user -d postgres
```

Do not delete the volume. The script checks whether the database exists and creates
only the missing database. Liquibase creates the five tables on application startup;
Hibernate uses `ddl-auto=validate` and never creates the schema.

Provide these environment variables to the application/IDE without committing their values:

| Variable | Meaning |
| --- | --- |
| `KELARUS_ACCOUNT_DB_URL` | Defaults to `jdbc:postgresql://localhost:5434/kelarus_account` |
| `KELARUS_ACCOUNT_DB_USERNAME` | Required database username; existing local container uses `user` |
| `KELARUS_ACCOUNT_DB_PASSWORD` | Required database password; match the configured PostgreSQL password |
| `KELARUS_AUTH_JWT_SECRET` | Required Base64 encoding of at least 32 cryptographically random bytes |

Generate the signing secret with a trusted secret manager or cryptographic random generator.
There is no default signing secret. Missing, malformed, or undersized keys prevent startup.
Use the same key across account instances; replacing it invalidates existing access JWTs.

Run from the repository root (Java 21 is selected through the existing Gradle toolchain):

```powershell
.\gradlew.bat :component:account:bootRun
```

The account service listens on port 18101. Eureka keeps its existing localhost:18102
configuration. For standalone local execution, set `EUREKA_CLIENT_ENABLED=false`.

Token durations use typed `kelarus.auth` configuration:

| Property | Default |
| --- | --- |
| `access-token-ttl` | `15m` |
| `refresh-token-ttl` | `30d` |
| `email-verification-ttl` | `24h` |
| `password-reset-ttl` | `30m` |

## HTTP API

All endpoints accept JSON via POST. Protected endpoints require
`Authorization: Bearer <accessToken>`; authentication does not use cookies or sessions.
Use HTTPS when transmitting credentials or bearer tokens outside local development.

| Path | Request fields | Response |
| --- | --- | --- |
| `/v1/public/auth/register` | `email`, `password` | 201: `userId`, normalized `email`, `status`, `verificationRequired` |
| `/v1/public/auth/verify-email` | `token` | 200: message |
| `/v1/public/auth/login` | `email`, `password` | 200: token pair |
| `/v1/public/auth/refresh` | `refreshToken` | 200: rotated token pair |
| `/v1/public/auth/forgot-password` | `email` | 200: identical generic message for known, unknown, and inactive accounts |
| `/v1/public/auth/reset-password` | `token`, `newPassword` | 200: message |
| `/v1/auth/change-password` | `currentPassword`, `newPassword` | 200: message; user ID comes from JWT |
| `/v1/auth/logout` | `refreshToken` | 200: message; revokes only the authenticated user's token |

Token pairs contain `accessToken`, `refreshToken`, `tokenType` (`Bearer`), and `expiresIn`
(access lifetime in seconds). Email normalization trims whitespace and lowercases using
`Locale.ROOT`. Passwords are required, 8–128 characters, with no complexity rules.

Passwords use Spring Security PBKDF2-HMAC-SHA256 (v5.8 defaults: 310,000 iterations,
16-byte random salt, 256-bit derived key). PBKDF2 was selected over BCrypt because BCrypt's
72-byte input limit cannot support the full 128-character policy, especially for Unicode.
No password is truncated or reversibly encrypted.

Access JWTs use HS256 and carry only `sub` (user UUID), `email`, `iat`, and `exp`.
The decoder checks signature, algorithm, timestamps, and identity claims. Refresh,
verification, and reset tokens contain 32 SecureRandom bytes encoded as unpadded Base64url;
only their SHA-256 hashes are persisted. Token consumption, rotation, password updates,
and refresh revocation run in transactions, serialized through the user's database row lock.

Registration starts in `PENDING_VERIFICATION`; verification activates the account. Login
uses a generic invalid-credentials error for unknown email and wrong password. An inactive
status is reported only after a correct password. Logout is idempotent, including missing
tokens. Password changes and resets revoke all refresh tokens and consume outstanding reset
links. Already-issued access JWTs remain valid until their short expiration; there is no
access-token denylist in this initial stateless implementation.

Errors use `{ "code": "...", "message": "..." }`. Validation returns `INVALID_REQUEST`
(400) without echoing rejected values. Duplicate email returns `EMAIL_ALREADY_REGISTERED`
(409); invalid credentials/refresh tokens return 401; inactive accounts return 403.
Verification/reset/current-password errors return 400. Security failures return
`UNAUTHENTICATED` (401) or `ACCESS_DENIED` (403). Other routes are denied by default.

## Deferred delivery boundary

`TokenDeliveryRequested` is a confidential, in-process event for email verification and
password reset. It is published within the database transaction. A future adapter should
use `@TransactionalEventListener` with `AFTER_COMMIT`, avoid logging event fields, and
provide delivery retry/durability guarantees. No delivery listener, email transport, broker,
notification service, or durable outbox is installed here. These events are not retained
for later replay. Registration/reset initiation therefore creates tokens but does not send
email yet; end users cannot complete those flows until delivery is integrated.

Raw verification/reset tokens are never returned from normal APIs or logged. Tests capture
events in a test-only listener to exercise the complete activation/reset flows.

## Utilities and scope

Inspected `https://github.com/yahyanug/common-utilities` at commit
`4f63cdc0f72b8e08b20b5d4c7328e1f42ae16f33`. Its build declares
`id.com.flare:common-utilities:1.0.0`, publishes to Maven local, and has no remote tags.
Its current source includes identity-document, phone, network, file, and other utilities,
but no applicable email normalization or password-policy validator. No external utility
dependency or copied helper implementation was added. JDK/Spring/Jakarta provide the
required capabilities. `utilities/general` remains untouched and is not consumed because
all new types are account-specific.

Deferred: tenant/business, memberships, roles/permissions, invitations, subscription and
plans/usage, WMS, POS, CRM, and notification delivery integration.

## Verification

```powershell
.\gradlew.bat :component:account:test
.\gradlew.bat clean build
```

The tests boot the actual application with Spring Security, JPA, and the five Liquibase
changesets against H2 in PostgreSQL compatibility mode. They exercise API validation,
transactional flows, token concurrency, constraints, password hashing, and JWT validation.
H2 is test-only and does not replace PostgreSQL-specific startup verification. With the
local PostgreSQL service running, use the setup above and `bootRun` to verify migrations
and Hibernate validation against PostgreSQL. No Testcontainers dependency is required.
