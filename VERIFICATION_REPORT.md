# PhoneWallet One-Server Verification Report

## Target architecture
- One application URL: `http://localhost:8090`
- React production build is embedded in `src/main/resources/static`
- Spring Boot serves both React and `/api/**`
- No Vite dev server is required at runtime.

## Verification completed in the build environment
1. Java source compilation (all project sources except the optional OpenAPI configuration, whose dependency is Maven-managed): **PASS**
   - 124 application classes compiled with Java 17 target.
2. Executable wallet-core harness: **PASS**
   - transfer success and balanced ledger
   - idempotent retry
   - idempotency semantic mismatch rejection
   - BOLA/ownership rejection
   - same-wallet rejection
   - currency mismatch rejection
   - insufficient funds rejection
   - frozen-wallet rejection
   - missing-wallet rejection
   - merchant-payee validation
   - merchant payment
   - unauthorized refund rejection
   - partial refund
   - over-refund rejection
   - cumulative full refund
   - refund-after-full rejection
   - admin reversal
   - idempotent reversal retry
   - reversal of refunded payment rejection
   - top-up intent/settlement path
   - balanced top-up ledger
3. Embedded frontend static delivery smoke test: **PASS**
   - `/` -> 200
   - `/favicon.ico` -> 200
   - `/favicon.svg` -> 200
   - production JS asset -> 200
   - production CSS asset -> 200
4. React production assets are already embedded in Spring Boot static resources: **PASS**
5. Static/JWT/rate-limit filters were hardened so browser assets do not depend on Redis: **PASS (source compile)**

## Maven / npm tests on the developer machine
`./build-and-run.sh` intentionally runs the real frontend build and Maven test suite before starting Spring Boot. This is the final gate on the developer machine because Maven/npm dependency resolution needs normal network access and local MySQL/Redis/Kafka/Docker availability.

## Local dependencies
- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- Kafka: `localhost:9092`

## Start
```bash
./build-and-run.sh
```
Then open only:

`http://localhost:8090`

## Developer-machine test correction

The Mac run successfully built React, compiled 124 application sources, and passed all shown tests except one Mockito verification in `AuditLedgerServiceTest`. That assertion has been corrected to distinguish the debit SUM query from the credit SUM query. Docker-backed infrastructure coverage remains conditional on Docker being available.
