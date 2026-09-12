# PhoneWallet — ONE SERVER ONLY (Port 8090)

This project is intentionally configured as a single deployable Spring Boot application.

## Runtime architecture

Browser -> http://localhost:8090 -> Spring Boot

- `/` and React routes are served from `src/main/resources/static`.
- `/api/**` is served by Spring MVC controllers.
- There is NO React/Vite server at runtime.
- Vite is only a build tool used before Spring Boot starts.

## First run on macOS

Make sure MySQL, Redis and Kafka are running, then from this project root run:

```bash
./build-and-run.sh
```

The script does this automatically:

1. `npm ci` inside `frontend/`
2. `npm run build`
3. copies `frontend/dist/*` into `src/main/resources/static/`
4. starts Spring Boot
5. the only HTTP application server is `http://localhost:8090`

Open:

```text
http://localhost:8090
```

Swagger:

```text
http://localhost:8090/swagger-ui/index.html
```

## Later backend-only restarts

After the React build is already embedded, you only need:

```bash
./mvnw spring-boot:run
```

## When React code changes

Rebuild it into Spring Boot:

```bash
./build-frontend.sh
./mvnw spring-boot:run
```

## Important

Do not run `npm run dev` for the integrated deployment. That would create a second server on port 5173, which is NOT the architecture of this package.

The frontend uses same-origin API calls such as `/api/auth/login`, so when served by Spring Boot they automatically resolve to `http://localhost:8090/api/...`.

## Local profile used by the one-server script

`./build-and-run.sh` now starts Spring Boot explicitly with the `local` profile. This guarantees that local MySQL/Kafka/Redis/JWT/top-up settings are loaded. The base configuration also has safe local fallbacks so the missing `wallet.topup.webhook-secret` error cannot occur in local mode. Production still uses `application-prod.properties` and requires real secrets from environment variables.
