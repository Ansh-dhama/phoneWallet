#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${BASE_URL:-http://localhost:8090}"

echo "Testing PhoneWallet at $BASE_URL"

check_status() {
  local path="$1" expected="$2"
  local code
  code=$(curl -sS -o /tmp/phonewallet-smoke-body -w '%{http_code}' "$BASE_URL$path")
  if [ "$code" != "$expected" ]; then
    echo "FAIL $path expected=$expected actual=$code"
    cat /tmp/phonewallet-smoke-body || true
    exit 1
  fi
  echo "PASS $path -> $code"
}

check_status "/" "200"
check_status "/favicon.ico" "200"
check_status "/favicon.svg" "200"
check_status "/login" "200"

# Protected API must reject anonymous access, not crash with 500.
code=$(curl -sS -o /tmp/phonewallet-smoke-body -w '%{http_code}' \
  -X POST "$BASE_URL/api/transactions/transfer" \
  -H 'Content-Type: application/json' \
  -d '{"fromWalletId":1,"toWalletId":2,"amount":1,"currency":"INR","idempotencyKey":"anonymous-smoke"}')
if [ "$code" != "401" ]; then
  echo "FAIL anonymous transfer expected=401 actual=$code"
  cat /tmp/phonewallet-smoke-body || true
  exit 1
fi
echo "PASS anonymous transfer -> 401 (security working, no 500)"

echo "Static/security smoke tests passed."
