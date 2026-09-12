# PhoneWallet Final Verification

## Architecture

This package is **ONE SERVER ONLY** at runtime.

- React is built with Vite.
- `frontend/dist/*` is copied into `src/main/resources/static/`.
- Spring Boot serves the React application and all REST APIs on **port 8090**.
- No Vite `5173` process is required at runtime.

## Fixes made for the reported 500s

1. **Ledger flush before audit SUM queries** — pending debit/credit rows are explicitly flushed before the native balance query. This prevents a valid transfer/payment/refund/top-up from being falsely reported as `debit=0, credit=amount` inside the same DB transaction.
2. **Real favicon resources** — both `favicon.ico` and `favicon.svg` are present and permitted by Spring Security.
3. **`/error` is public** — an internal error dispatch no longer gets transformed into a misleading security error.
4. **Unhandled exceptions are logged with full stack trace** — real backend failures are now diagnosable from the Spring console while clients receive a safe JSON response.
5. **Concurrency errors map to 409 instead of generic 500** — pessimistic/optimistic lock contention is treated as a retryable wallet conflict.
6. **Audit enums stored as VARCHAR** — reduces MySQL enum schema drift problems as audit actions/statuses evolve.
7. **Kafka topic creation and schedulers are switchable for integration tests** without weakening normal runtime defaults.

## Executed verification in the packaging environment

### Java source compile
Current application sources compiled against the available Spring Boot runtime dependencies. The only excluded compile target in that offline check was the OpenAPI configuration because the prior extracted runtime dependency set did not contain springdoc jars; normal Maven compilation includes the declared springdoc dependency.

### Core wallet executable harness
The harness executes the real transaction/top-up/ledger services with repository doubles and passed positive and negative cases including:

- transfer success
- duplicate/idempotent transfer retry
- changed request under the same idempotency key rejected
- BOLA/non-owner transfer rejected
- same-wallet transfer rejected
- currency mismatch rejected
- insufficient balance rejected
- frozen wallet rejected
- missing destination rejected
- payment to non-merchant rejected
- merchant payment success
- unauthorized refund rejected
- partial refund
- over-refund rejected
- full refund
- refund after fully refunded state rejected
- reversal success and idempotent retry
- reversal of fully refunded payment rejected
- top-up maximum and currency validation
- top-up intent does not mint funds
- idempotent top-up intent retry
- verified/demo settlement credits exactly once
- duplicate top-up settlement does not double-credit
- webhook signature validation and tamper rejection
- balanced debit/credit ledger checks

Result: `ALL CORE WALLET + NEGATIVE-PATH HARNESS TESTS PASSED`.

### React source verification
All JS/JSX source files parsed successfully and relative imports resolved successfully in the available parser environment.

### Actual Spring Boot HTTP smoke verification
The current application code was started in an isolated local test runtime and verified:

- `GET /` -> 200
- `GET /favicon.ico` -> 200
- `GET /favicon.svg` -> 200
- `GET /login` -> 200
- anonymous `POST /api/transactions/transfer` -> 401 JSON (not 500)

The isolated runtime used an old system HSQLDB driver only for startup/static/security smoke testing; that driver cannot exercise Hibernate 6 generated-key persistence correctly. Real persistence integration is covered by the included MySQL 8 + Redis Testcontainers test and should be run on a machine with Docker.

## Tests run automatically on your Mac

`./build-and-run.sh` now performs:

1. `npm ci`
2. `npm run build`
3. copy Vite output into Spring static resources
4. `./mvnw test -Dspring.profiles.active=local`
5. start Spring Boot with the local profile

If any frontend build or Maven test fails, the script stops and does **not** start the server.

After startup, run `./smoke-test.sh` in a second terminal for root/favicon/security checks.

## 2026-09-12 developer-machine regression fix

The real Mac Maven run exposed one brittle Mockito assertion in `AuditLedgerServiceTest`. The production ledger verification itself succeeded, but the test used `anyList()` for two calls to the same repository method, so Mockito reported both invocations against the first verification. The test now matches debit and credit entry-type lists explicitly. See `TEST_FIX.md`.
