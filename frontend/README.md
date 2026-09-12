# PhoneWallet Professional Frontend

Production-style React + Vite frontend wired to the supplied Spring Boot PhoneWallet API.

## Current local configuration

Your backend currently runs on:

```text
http://localhost:8090
```

The project is already configured for that backend in `.env`:

```env
VITE_API_BASE_URL=
VITE_BACKEND_TARGET=http://localhost:8090
```

For local development the browser calls `/api/...` on the Vite origin and Vite proxies those calls to `http://localhost:8090`. This avoids local CORS problems even if Spring Security does not yet contain a CORS configuration.

MySQL, Kafka and Redis are backend infrastructure. The React application never connects to them directly. Your Spring Boot service continues to use its own current configuration for MySQL, Kafka on `localhost:9092`, Redis, JWT and Swagger.

## Run

Keep Spring Boot running on port 8090, then open a second terminal:

```bash
cd phone-wallet-frontend
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

Do not open `http://localhost:8090/` expecting the React UI. Port 8090 is the Spring Boot API server.

## Main features

- Professional responsive fintech layout
- Login / signup / logout
- JWT bearer authentication
- Automatic refresh-token retry after HTTP 401
- Role-aware navigation for USER, MERCHANT and ADMIN
- Create wallet / connect existing wallet
- Wallet details and live balance
- USER load-money operation
- USER wallet-to-wallet transfer
- USER merchant payment
- USER/MERCHANT transaction history
- Dedicated transaction detail API lookup
- Wallet statements
- Date-range statement filtering
- CSV statement export
- MERCHANT refund workflow
- ADMIN summary dashboard
- ADMIN all/frozen/blacklisted wallet endpoint filters
- ADMIN freeze / unfreeze / blacklist / unblacklist operations
- ADMIN all/failed/status/type transaction endpoint filters
- ADMIN refund / reversal workflows
- ADMIN audit logs
- ADMIN notifications
- Error handling using backend response messages
- Automatic idempotency-key generation for load, transfer, payment, refund and reversal requests

## API integration

See `API_INTEGRATION.md` for the endpoint-to-screen mapping.

## Important wallet lookup limitation

The current login response gives the frontend:

```text
accessToken
refreshToken
userId
```

but the supplied wallet controller does not expose an endpoint such as:

```text
GET /api/wallets/user/{userId}
```

Therefore, after a USER or MERCHANT logs in, the frontend currently offers:

1. Create a wallet for the logged-in `userId`, or
2. Connect an existing wallet by wallet ID.

The selected wallet ID is stored in browser local storage per user.

A future backend endpoint such as the following would let the frontend discover the wallet automatically:

```java
@GetMapping("/user/{userId}")
public ResponseEntity<Wallet> findByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(walletService.findByUserId(userId));
}
```

## Role handling

The frontend reads common JWT role claims:

- `role`
- `roles`
- `authorities`
- `scope`
- `scopes`

Both `ADMIN` and `ROLE_ADMIN` forms are supported, likewise USER and MERCHANT.

The UI follows the same backend permissions:

- USER: wallet, load money, transfer, pay, transactions, statements
- MERCHANT: wallet, transactions, statements, refunds
- ADMIN: admin console, wallet security controls, platform transactions, refunds, reversals, audit logs, notifications

## Changing backend later

For local development, change only:

```env
VITE_BACKEND_TARGET=http://localhost:NEW_PORT
```

If the frontend is deployed separately from the backend, set:

```env
VITE_API_BASE_URL=https://your-api.example.com
```

and configure CORS in Spring Security for the deployed frontend origin.

## Build

```bash
npm run build
```

The production files will be generated under `dist/`.

For a local production preview using the same 8090 proxy:

```bash
npm run preview
```

Then open `http://localhost:4173`.
