#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"
STATIC_DIR="$ROOT_DIR/src/main/resources/static"

printf '\n=== PhoneWallet: ONE SERVER / PROFESSIONAL UI ===\n'
printf '1) Build React  2) Embed in Spring Boot  3) Start only :8090\n\n'

cd "$FRONTEND_DIR"
if [ -f package-lock.json ]; then
  npm ci
else
  npm install
fi
npm run build

rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"
cp -R "$FRONTEND_DIR/dist/." "$STATIC_DIR/"

printf '\nProfessional frontend embedded into: %s\n' "$STATIC_DIR"
printf 'Starting PhoneWallet on ONE URL: http://localhost:8090\n\n'

cd "$ROOT_DIR"
exec ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
