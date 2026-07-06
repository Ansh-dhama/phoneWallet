# PhoneWallet - Digital Wallet & Payment Ledger System

PhoneWallet is a production-style digital wallet backend system built using Java 17, Spring Boot, PostgreSQL, Redis, Apache Kafka, JWT Security, Docker, and Swagger.

The project demonstrates wallet creation, money loading, wallet-to-wallet transfer, merchant payment, refund, reversal, immutable ledger, fraud checks, refresh token flow, rate limiting, admin dashboard APIs, Kafka eventing, and the transactional outbox pattern.

---

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring MVC
- Spring Data JPA
- PostgreSQL
- Redis
- Apache Kafka
- Spring Security
- JWT Authentication
- Refresh Token Flow
- Docker
- Swagger/OpenAPI
- Maven

---

## Core Features

### Authentication & Security

- User signup
- User login
- JWT access token
- Refresh token flow
- Logout and refresh token revoke
- Role-based access control
- USER, MERCHANT, ADMIN roles

### Wallet Management

- Create wallet
- View wallet balance
- Freeze wallet
- Unfreeze wallet
- Blacklist wallet
- Unblacklist wallet

### Transaction Flows

- Load money
- Wallet-to-wallet transfer
- Merchant payment
- Refund
- Reversal
- Idempotency key support
- Transaction status tracking

### Ledger System

- Immutable ledger entries
- Debit and credit entries
- Refund debit and credit entries
- Reversal debit and credit entries
- Balance after transaction
- Ledger validation

### Risk and Fraud

- Basic transaction risk validation
- Wallet blacklist validation
- Redis fraud velocity check
- API rate limiting using Redis

### Kafka and Outbox

- Kafka event publishing
- Notification consumer
- Transaction success events
- Refund completed events
- Kafka outbox pattern
- Reliable event publishing
- Retry support for failed outbox events

### Admin Dashboard APIs

- System summary
- All wallets
- Frozen wallets
- Blacklisted wallets
- All transactions
- Failed transactions
- Transactions by status
- Transactions by type
- Audit logs
- Notifications

---

## Architecture

```text
Client / Postman / Swagger
        |
        v
Spring Boot REST APIs
        |
        v
Service Layer
        |
        +--> PostgreSQL
        |       - users
        |       - wallets
        |       - transactions
        |       - ledger_entries
        |       - audit_logs
        |       - refresh_tokens
        |       - outbox_events
        |
        +--> Redis
        |       - fraud velocity check
        |       - API rate limiting
        |
        +--> Kafka Outbox Processor
                |
                v
             Kafka Topics
                |
                v
         Notification Consumer