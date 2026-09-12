# API Integration Map

Current backend target: `http://localhost:8090`

## Authentication

| Method | Endpoint | Frontend usage |
|---|---|---|
| POST | `/api/auth/login` | Login page |
| POST | `/api/auth/signup` | Signup page |
| POST | `/api/auth/refresh-token` | Axios automatic token refresh |
| POST | `/api/auth/logout` | Profile sign out |

## Wallets

| Method | Endpoint | Frontend usage |
|---|---|---|
| POST | `/api/wallets` | Create wallet |
| GET | `/api/wallets/{walletId}` | Connect / wallet details |
| GET | `/api/wallets/{walletId}/balance` | Live wallet balance |
| POST | `/api/wallets/{walletId}/load-money` | USER load money |
| POST | `/api/wallets/{walletId}/freeze` | ADMIN wallet control |
| POST | `/api/wallets/{walletId}/unfreeze` | ADMIN wallet control |
| POST | `/api/wallets/{walletId}/blacklist` | ADMIN wallet control |
| POST | `/api/wallets/{walletId}/unblacklist` | ADMIN wallet control |

## Transactions

| Method | Endpoint | Frontend usage |
|---|---|---|
| POST | `/api/transactions/transfer` | USER transfer page |
| POST | `/api/transactions/pay` | USER pay-merchant page |
| POST | `/api/transactions/refund` | MERCHANT refund page + ADMIN console |
| POST | `/api/transactions/reversal` | ADMIN console |
| GET | `/api/transactions/{transactionId}` | Transaction details modal |
| GET | `/api/transactions/wallet/{walletId}` | Wallet transaction history |

## Statements

| Method | Endpoint | Frontend usage |
|---|---|---|
| GET | `/api/statements/wallet/{walletId}` | Full wallet statement |
| GET | `/api/statements/wallet/{walletId}/range` | Date-range statement filter |

## Administration

| Method | Endpoint | Frontend usage |
|---|---|---|
| GET | `/api/admin/dashboard/summary` | KPI dashboard |
| GET | `/api/admin/wallets` | All wallets |
| GET | `/api/admin/wallets/frozen` | Frozen-wallet server filter |
| GET | `/api/admin/wallets/blacklisted` | Blacklisted-wallet server filter |
| GET | `/api/admin/transactions` | All platform transactions |
| GET | `/api/admin/transactions/failed` | Failed-transaction server filter |
| GET | `/api/admin/transactions/status/{status}` | Status server filter |
| GET | `/api/admin/transactions/type/{type}` | Type server filter |
| GET | `/api/admin/audit-logs` | Audit tab |
| GET | `/api/admin/notifications` | Notifications tab |

## Local request flow

```text
Chrome
  |
  | http://localhost:5173
  v
Vite / React
  |
  | /api/** proxy
  v
Spring Boot
http://localhost:8090
  |
  +-- MySQL
  +-- Kafka localhost:9092
  +-- Redis
```
