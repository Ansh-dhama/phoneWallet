# =========================================================
# STAGE 1 — BUILD REACT FRONTEND
# =========================================================

FROM node:22-alpine AS frontend-build

WORKDIR /workspace/frontend

COPY frontend/package.json frontend/package-lock.json ./

RUN npm ci

COPY frontend/ ./

RUN npm run build


# =========================================================
# STAGE 2 — BUILD SPRING BOOT
# =========================================================

FROM maven:3.9.9-eclipse-temurin-17 AS backend-build

WORKDIR /workspace

COPY pom.xml ./

RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src

# Remove old committed frontend build.
RUN rm -rf src/main/resources/static/*

# Put the freshly built React app inside Spring Boot.
COPY --from=frontend-build /workspace/frontend/dist/ \
    src/main/resources/static/

RUN mvn -q -DskipTests clean package


# =========================================================
# STAGE 3 — PRODUCTION RUNTIME
# =========================================================

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home walletapp

WORKDIR /app

COPY --from=backend-build \
    /workspace/target/phoneWallet-0.0.1-SNAPSHOT.jar \
    app.jar

USER walletapp

EXPOSE 10000

HEALTHCHECK --interval=30s \
            --timeout=5s \
            --start-period=60s \
            --retries=3 \
    CMD sh -c 'curl -fsS "http://localhost:${PORT:-10000}/actuator/health/readiness" || exit 1'

ENTRYPOINT [
    "java",
    "-XX:MaxRAMPercentage=75",
    "-jar",
    "app.jar"
]
