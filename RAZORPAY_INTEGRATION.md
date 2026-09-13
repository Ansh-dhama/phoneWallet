# PhoneWallet Razorpay Integration

## Payment flow

1. `POST /api/topups/wallet/{walletId}` validates ownership, amount and INR currency.
2. Backend calls Razorpay `POST /v1/orders` using server-side Basic authentication.
3. Backend stores the returned Razorpay `order_id` in `topup_intents` with `PENDING` status. The wallet is **not credited**.
4. React loads `https://checkout.razorpay.com/v1/checkout.js` and opens Razorpay Checkout using the server-created order id.
5. Razorpay returns `razorpay_payment_id`, `razorpay_order_id` and `razorpay_signature` after successful Checkout.
6. React calls `POST /api/topups/{intentId}/verify-payment`.
7. Backend verifies the HMAC SHA-256 Checkout signature using the **server-stored order id**, then calls Razorpay `GET /v1/payments/{paymentId}`.
8. Backend checks order id, exact amount, currency and `status=captured`.
9. Only then does `settleLocked()` credit the wallet, write balanced ledger entries, mark the transaction `SUCCESS`, mark the top-up `COMPLETED`, and emit the outbox event.
10. `POST /api/topups/razorpay/webhook` provides a server-to-server fallback for `payment.captured`, `order.paid` and `payment.failed`. It validates `X-Razorpay-Signature` against the **raw** request body.

## Important API endpoints

- `GET /api/topups/checkout-config` — authenticated USER; returns only the public Razorpay Key ID.
- `POST /api/topups/wallet/{walletId}` — authenticated USER; creates Razorpay order + PENDING top-up intent.
- `POST /api/topups/{intentId}/verify-payment` — authenticated USER; verifies Checkout result and settles only a captured payment.
- `GET /api/topups/wallet/{walletId}` — lists top-up history.
- `POST /api/topups/razorpay/webhook` — public to Razorpay, but cryptographically authenticated with the webhook secret.

There is no frontend demo-completion path and no public demo-complete endpoint in this build.

## Main Java methods

### `RazorpayPaymentService`

- `publicKeyId()` — returns Key ID only; never exposes Key Secret.
- `createOrder(...)` — calls Razorpay Orders API.
- `verifyCapturedPayment(...)` — validates Checkout signature and then fetches the provider payment.
- `verifyCapturedPaymentById(...)` — checks provider order id, amount, currency and `captured` status.
- `verifyCheckoutSignature(...)` — constant-time HMAC SHA-256 validation of `orderId|paymentId`.
- `verifyAndParseWebhook(...)` — verifies the raw webhook body before parsing it.
- `toSubunits(...)` — converts rupees to paise exactly and rejects more than 2 decimal places.

### `TopUpService`

- `initiate(...)` — creates a Razorpay order and stores a PENDING intent; does not credit wallet.
- `checkoutConfig()` — supplies the frontend with the public Key ID.
- `verifyRazorpayPayment(...)` — locks the intent, verifies Razorpay, then settles exactly once.
- `handleRazorpayWebhook(...)` — provider webhook fallback with duplicate/out-of-order safety.
- `settleLocked(...)` — idempotent wallet credit + transaction + ledger + outbox settlement.

## Properties / environment variables

The project uses Spring property placeholders. Real secrets are **not hard-coded** in the ZIP:

```properties
wallet.topup.demo-enabled=${WALLET_TOPUP_DEMO_ENABLED:false}
razorpay.key-id=${RAZORPAY_KEY_ID:}
razorpay.key-secret=${RAZORPAY_KEY_SECRET:}
razorpay.webhook-secret=${RAZORPAY_WEBHOOK_SECRET:}
razorpay.api-base-url=${RAZORPAY_API_BASE_URL:https://api.razorpay.com/v1}
```

Set these in your IDE run configuration, shell, Docker/Compose, or Render environment:

```bash
export WALLET_TOPUP_DEMO_ENABLED=false
export RAZORPAY_KEY_ID='rzp_test_xxxxxxxxxxxxx'
export RAZORPAY_KEY_SECRET='YOUR_ROTATED_TEST_KEY_SECRET'
export RAZORPAY_WEBHOOK_SECRET='YOUR_RAZORPAY_WEBHOOK_SECRET'
```

The secret pasted into chat earlier should be rotated in Razorpay before reuse.

## Razorpay Dashboard

For test mode:

- Use Test Mode API keys.
- Enable automatic payment capture.
- Add webhook URL: `https://YOUR_HOST/api/topups/razorpay/webhook`
- Subscribe at minimum to `payment.captured`, `payment.failed`, and `order.paid`.
- Set a strong webhook secret and put the same value in `RAZORPAY_WEBHOOK_SECRET`.

## Local build

```bash
./build-frontend.sh
./mvnw clean test
./mvnw spring-boot:run
```

Open `http://localhost:8090`.

For Render, the supplied Dockerfile builds the React frontend first, embeds `frontend/dist` into Spring Boot, builds the jar, and runs on Render's `$PORT`.
