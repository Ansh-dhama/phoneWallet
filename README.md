# 🌐 Live Application

👉 https://phonewallet-1.onrender.com

# PhoneWallet

PhoneWallet is a full-stack digital wallet application built using **Java, Spring Boot, React, MySQL, Redis, Kafka, JWT Authentication, Razorpay, Docker, and Render**.

The application supports customer wallets, merchant wallets, wallet-to-wallet transfers, merchant payments, Razorpay wallet top-ups, refunds, transaction history, statements, notifications, audit logs, role-based authorization, and admin management.

---

## 🚀 Live Deployment

**Live URL:**  
https://phonewallet-1.onrender.com

The frontend and backend are deployed together as a **single application**.

```text
Browser
   |
   v
Render Web Service
   |
   +---- Spring Boot Backend
   |
   +---- React Frontend
   |
   +---- Aiven MySQL
   |
   +---- Redis
   |
   +---- Aiven Kafka
   |
   +---- Razorpay
🧩 Tech Stack
Backend
Java 17
Spring Boot 3.3.5
Spring Security
Spring Data JPA
Hibernate
JWT Authentication
MySQL
Redis
Apache Kafka
Maven
Razorpay
Swagger / OpenAPI
Spring Boot Actuator
Frontend
React
Vite
Axios
React Router
Lucide React Icons
Deployment
Docker
Render
Aiven MySQL
Aiven Kafka
Redis / Valkey
GitHub
✨ Features
👤 User

Users can:

Sign up
Login
Logout
Refresh access token
Create wallet
Add money using Razorpay
Transfer money
Pay merchant
View wallet balance
View transactions
View statements
View notifications
🏪 Merchant

Merchants can:

Create merchant wallet
Receive customer payments
View transactions
View statements
Refund eligible payments
👑 Admin

Admins can:

View users
Change user roles
Promote user to merchant
Promote user to admin
View all wallets
View all transactions
View failed transactions
Freeze wallets
Unfreeze wallets
Blacklist wallets
Remove wallet blacklist
View audit logs
View notifications
🔐 Roles

The project supports:

USER
MERCHANT
ADMIN

Public signup always creates:

USER

Users cannot directly register themselves as ADMIN or MERCHANT.

🔐 Authentication Flow
Login
   ↓
Username + Password
   ↓
Spring Security
   ↓
JWT Access Token
   +
Refresh Token
   ↓
Client sends:

Authorization: Bearer <token>

Passwords are stored using BCrypt hashing.

💳 Wallet System

Users and merchants use the same wallet model.

Example:

{
  "id": 5,
  "walletNumber": "generated-uuid",
  "userId": 12,
  "currency": "INR",
  "balance": 1500.00,
  "status": "ACTIVE"
}

New wallets start with:

balance = 0
status = ACTIVE
💰 Razorpay Add Money Flow
Customer enters amount
        ↓
Backend creates Top-Up Intent
        ↓
Backend creates Razorpay Order
        ↓
Razorpay Checkout opens
        ↓
Customer completes payment
        ↓
Razorpay returns:

razorpay_payment_id
razorpay_order_id
razorpay_signature

        ↓
Backend verifies signature
        ↓
Backend fetches payment details
        ↓
Backend validates:

✓ Order ID
✓ Amount
✓ Currency
✓ Payment Status

        ↓
Payment must be CAPTURED
        ↓
Wallet credited
        ↓
Transaction created
        ↓
Ledger entry created
        ↓
Top-up marked COMPLETED

The wallet is not credited only because the frontend reports success.

Payment is verified by the backend first.

🪝 Razorpay Webhook

Webhook endpoint:

POST /api/topups/razorpay/webhook

Production webhook URL:

https://phonewallet-1.onrender.com/api/topups/razorpay/webhook

Recommended events:

payment.captured
payment.failed
order.paid

The webhook provides recovery if the payment succeeds but the frontend verification request is not completed.

💸 Customer to Merchant Payment
Customer Wallet
      |
      | Pay
      v
Merchant Wallet

Endpoint:

POST /api/transactions/pay

Example:

{
  "fromWalletId": 1,
  "toWalletId": 5,
  "amount": 500,
  "currency": "INR",
  "idempotencyKey": "ORDER-1001-PAYMENT",
  "merchantReference": "ORDER-1001"
}
🔁 Wallet Transfer

Endpoint:

POST /api/transactions/transfer

Example flow:

Wallet A
   |
   | ₹500
   v
Wallet B

Database locking is used to protect wallet balances from concurrent updates.

↩️ Refund

Endpoint:

POST /api/transactions/refund

The backend validates:

Original transaction
Merchant ownership
Refund amount
Transaction status
Previous refunds
Wallet balance
🛡️ Idempotency

Money-related operations use idempotency keys.

Example:

ORDER-1001-PAYMENT

Flow:

Client sends payment
       ↓
Network timeout
       ↓
Client retries
       ↓
Same idempotency key
       ↓
Existing result returned
       ↓
No duplicate payment
🔐 Concurrency Protection

Wallet operations use database locking.

Example:

Request A → Debit ₹500
Request B → Debit ₹500

Without locking, both requests could read the same balance.

The backend locks the wallet before updating it.

For two-wallet transactions, wallets are locked in ID order to reduce deadlock risk.

📚 Ledger

Financial transactions create ledger records.

Transaction
   |
   +---- Debit Entry
   |
   +---- Credit Entry

This makes wallet movement auditable.

📤 Kafka

Kafka is used for asynchronous events.

Transaction Completed
        ↓
Outbox Event
        ↓
Kafka
        ↓
Consumer
        ↓
Notification

Kafka producer configuration uses:

acks=all
retries
idempotent producer
📦 Transactional Outbox

The project uses the Outbox Pattern.

Database Transaction
      |
      +---- Wallet Update
      |
      +---- Transaction
      |
      +---- Ledger
      |
      +---- Outbox Event
              ↓
           Kafka

This prevents database/Kafka consistency problems.

⚡ Redis

Redis is used for:

Idempotency
Distributed locking
Rate limiting
Temporary security data
🧾 Statements

Wallet statements contain information such as:

Date
Transaction ID
Transaction Type
Amount
Debit
Credit
Status
Balance
🔔 Notifications

Notifications can be generated for:

Wallet Top-Up
Transfer
Merchant Payment
Refund
Transaction Failure
Administrative Actions
📝 Audit Logging

Important actions are recorded.

Examples:

USER_LOGIN
USER_SIGNUP
WALLET_CREATED
WALLET_FROZEN
TRANSACTION_CREATED
REFUND
ROLE_CHANGED
🏪 Creating a Merchant

Public signup creates a normal USER.

Admin can promote a user using:

PATCH /api/admin/users/{userId}/role

Request:

{
  "role": "MERCHANT"
}

After role update, merchant should login again.

Then create merchant wallet:

POST /api/wallets
{
  "currency": "INR"
}
👑 Creating the First Admin

Public signup intentionally does not allow creating admins.

Create a normal account first.

Then promote the trusted account once in the database:

UPDATE app_users
SET role = 'ADMIN'
WHERE username = 'your_admin_username';

After that, logout and login again.

Future role changes should be performed through the protected admin API.

🌐 Main API Endpoints
Authentication
POST /api/auth/signup
POST /api/auth/login
POST /api/auth/refresh-token
POST /api/auth/logout
Wallet
POST /api/wallets
GET  /api/wallets/mine
GET  /api/wallets/{walletId}
GET  /api/wallets/{walletId}/balance
Razorpay Top-Up
GET  /api/topups/checkout-config
POST /api/topups/wallet/{walletId}
POST /api/topups/{intentId}/verify-payment
GET  /api/topups/wallet/{walletId}
POST /api/topups/razorpay/webhook
Transactions
POST /api/transactions/transfer
POST /api/transactions/pay
POST /api/transactions/refund
POST /api/transactions/reversal

GET /api/transactions/{transactionId}
GET /api/transactions/wallet/{walletId}
Admin
GET   /api/admin/dashboard/summary
GET   /api/admin/users
PATCH /api/admin/users/{userId}/role

GET /api/admin/wallets
GET /api/admin/wallets/frozen
GET /api/admin/wallets/blacklisted

GET /api/admin/transactions
GET /api/admin/transactions/failed

GET /api/admin/audit-logs
GET /api/admin/notifications
📁 Project Structure
PhoneWallet
│
├── frontend
│   ├── src
│   │   ├── components
│   │   ├── pages
│   │   ├── services
│   │   └── App.jsx
│   │
│   ├── package.json
│   └── vite.config.js
│
├── src
│   ├── main
│   │   ├── java/com/example/phoneWallet
│   │   │   ├── controller
│   │   │   ├── dto
│   │   │   ├── entity
│   │   │   ├── enums
│   │   │   ├── Repository
│   │   │   ├── Security
│   │   │   ├── services
│   │   │   └── Util
│   │   │
│   │   └── resources
│   │       ├── application.properties
│   │       ├── application-prod.properties
│   │       ├── application-docker.properties
│   │       └── static
│   │
│   └── test
│
├── database
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
⚙️ Environment Variables
SPRING_PROFILES_ACTIVE=prod

SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

REDIS_HOST=
REDIS_PORT=6379

KAFKA_BOOTSTRAP_SERVERS=
KAFKA_USERNAME=
KAFKA_PASSWORD=

JWT_SECRET_KEY=

RAZORPAY_KEY_ID=
RAZORPAY_KEY_SECRET=
RAZORPAY_WEBHOOK_SECRET=

WALLET_TOPUP_DEMO_ENABLED=false

Never commit real passwords, JWT secrets, Kafka credentials, or Razorpay secrets.

🗄️ Database

Production database:

Aiven MySQL

Major data includes:

Users
Wallets
Transactions
Ledger
Top-Up Intents
Refresh Tokens
Notifications
Audit Logs
Outbox Events
Wallet Blocklist
🧪 Run Locally
Requirements
Java 17+
Node.js
npm
MySQL
Redis
Kafka

Clone:

git clone https://github.com/Ansh-dhama/phoneWallet.git
cd phoneWallet

Install frontend dependencies:

npm --prefix frontend install

Build frontend:

npm --prefix frontend run build

Run tests:

./mvnw clean test

Run Spring Boot:

./mvnw spring-boot:run

Open:

http://localhost:8090
🐳 Docker

Build:

docker build -t phonewallet .

Run:

docker run -p 8090:8090 phonewallet

Docker build flow:

Node
 ↓
React Build
 ↓
Maven
 ↓
Spring Boot JAR
 ↓
Java Runtime
 ↓
Single Container
☁️ Deployment

The application is deployed on Render.

Live Application:

👉 https://phonewallet-1.onrender.com

Architecture:

React + Spring Boot
        |
      Render
        |
  +-----+------+
  |     |      |
MySQL Redis   Kafka
Aiven Render  Aiven
❤️ Health Check
https://phonewallet-1.onrender.com/actuator/health
📖 Swagger

Swagger UI:

https://phonewallet-1.onrender.com/swagger-ui.html

API Docs:

https://phonewallet-1.onrender.com/api-docs
🔒 Security Highlights
BCrypt password hashing
JWT authentication
Refresh tokens
Role-based authorization
Wallet ownership validation
Admin authorization
Merchant authorization
Idempotency
Database locking
Redis locking
Rate limiting
Razorpay signature verification
Server-side Razorpay verification
Transactional outbox
Audit logs
Wallet freeze
Wallet blacklist
Secure error responses
🧠 Important Design Rule

Wallet balance is never supposed to be changed directly by frontend input.

All balance changes happen through backend-controlled operations:

Top-Up
Transfer
Payment
Refund
Reversal

The backend remains the source of truth.

⚠️ Disclaimer

PhoneWallet is an engineering project demonstrating digital wallet architecture.

Using a stored-value wallet or payment product commercially may require additional compliance, KYC, AML, RBI, payment-provider, and regulatory requirements depending on the business model.

👨‍💻 Author

Ansh Dhama

Backend-focused Software Engineer

Technologies
Java
Spring Boot
Spring Security
MySQL
Redis
Kafka
Docker
React
Razorpay
JWT
REST APIs
🔗 Links

Live Application

https://phonewallet-1.onrender.com

GitHub Repository

https://github.com/Ansh-dhama/phoneWallet
