# PhoneWallet Professional UI

This revision changes the customer-facing React interface only. The working Spring Boot wallet backend and API contracts remain unchanged.

## One server

Run `./build-and-run.sh`. It builds the React application, copies the production bundle into `src/main/resources/static`, and starts Spring Boot. Use only:

- App: `http://localhost:8090`
- API: `http://localhost:8090/api/**`
- Swagger: `http://localhost:8090/swagger-ui/index.html`

There is no Vite development server at runtime.

## UI improvements

- Consumer-grade sign-in and registration instead of technical role/JWT language
- Professional desktop sidebar and mobile bottom navigation
- Responsive wallet home with balance visibility control
- Real quick actions: Add money, Send, Pay, Statement
- Wallet card design and simplified wallet details
- Add-money amount shortcuts and clearer settlement status
- Cleaner send-money and business-payment flows
- Receipt-style confirmations
- Polished transaction table, status badges, filters and modals
- New Account & Security screen
- Friendly Personal / Business / Administrator labels instead of exposing internal role names in normal navigation
- Responsive mobile layout designed for wallet use
- Admin functionality preserved

## Backend

No Java wallet business logic was changed for this UI revision.
