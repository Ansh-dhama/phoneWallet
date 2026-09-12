# PhoneWallet — Corrected Full-Stack Project

PhoneWallet is a Java 17 / Spring Boot digital wallet backend with a React/Vite frontend. The React production build is intended to be served by the same Spring Boot application from `src/main/resources/static`, so the final application runs on one port: **8090**.

## What was corrected

- Fixed `LOAD_MONEY` ledger failure (`Debit total: 0.00, Credit total: ...`).
- Added an external settlement side to the immutable ledger so top-ups are double-entry balanced.
- Fixed `LOAD_MONEY` direction: the customer wallet is now `toWalletId`.
- Fixed reversal of `LOAD_MONEY`, including compatibility with older top-up transactions.
- Added currency checks before load/transfer/payment and refund currency validation.
- Fixed the pessimistic write lock used by wallet balance updates.
- Fixed blacklist lookup to use `walletId`, not the blacklist table primary key.
- Fixed rate-limit HTTP status to return 429.
- Updated Spring Security to serve the React frontend and BrowserRouter routes.
- Added CORS for optional Vite development on port 5173.
- Switched the project configuration from PostgreSQL to the current MySQL setup.
- Updated Redis properties to Spring Boot 3's `spring.data.redis.*` form.
- Included the professional React frontend under `frontend/`.

## Current local configuration

The defaults match the configuration requested for local development:

- Spring Boot: `http://localhost:8090`
- MySQL: `localhost:3306/phoneWallet`
- MySQL user: `root`
- MySQL password fallback: `YourNewStrongPassword`
- Kafka: `localhost:9092`
- Redis: `anshdhama:9785`

All important values can be overridden later through environment variables in `application.properties`.

## 1. Create the MySQL database

```sql
CREATE DATABASE IF NOT EXISTS phoneWallet;
```

## 2. React frontend

A browser-ready React fallback is already present in `src/main/resources/static`, so `http://localhost:8090` can render the frontend immediately. That fallback loads the React ecosystem modules from a pinned CDN and therefore needs browser internet access.

For the recommended fully bundled build (no CDN dependency), run from the project root:

```bash
./build-frontend.sh
```

That script runs `npm install`, `npm run build`, and copies `frontend/dist/*` into:

```text
src/main/resources/static/
```

For frontend development only, you can instead run:

```bash
cd frontend
npm install
npm run dev
```

and use `http://localhost:5173`. Vite proxies `/api` to Spring Boot on port 8090.

## 3. Start required services

Kafka must be available at:

```text
localhost:9092
```

Redis must match your current configured host/port, or override:

```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

## 4. Start Spring Boot

```bash
./mvnw spring-boot:run
```

Then open:

- App: `http://localhost:8090`
- Swagger: `http://localhost:8090/swagger-ui/index.html`

## Load-money flow after the fix

For a top-up of 8000 INR:

```text
External settlement account   EXTERNAL_DEBIT   8000
Customer wallet               CREDIT           8000
----------------------------------------------------
Total debit                                    8000
Total credit                                   8000
```

The wallet balance becomes 8000 and the audit ledger passes.

The external settlement side uses ledger-only account id `0`. It is not a customer wallet and therefore does not appear in the customer's wallet statement.

## API used by the frontend

The frontend uses same-origin relative URLs such as:

```text
/api/auth/login
/api/wallets/{walletId}/load-money
/api/transactions/transfer
/api/transactions/pay
/api/statements/wallet/{walletId}
/api/admin/dashboard/summary
```

When React is embedded in Spring Boot, the browser automatically resolves these against `http://localhost:8090`.

## Important production note

The current `LOAD_MONEY` endpoint is suitable for project/testing use. In a real wallet, the backend should credit the wallet only after verifying a successful payment gateway/bank/UPI settlement. The external clearing ledger entry added here prepares the ledger model for that later integration.
