#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
STATIC_DIR="$ROOT_DIR/src/main/resources/static"

printf '\n=== PhoneWallet: VERIFY + RUN ===\n\n'
cd "$FRONTEND_DIR"
npm ci
npm run build
rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"
cp -R "$FRONTEND_DIR/dist/." "$STATIC_DIR/"

cd "$ROOT_DIR"
./mvnw test -Dspring.profiles.active=local
exec ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
