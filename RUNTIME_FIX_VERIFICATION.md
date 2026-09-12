# Runtime Fix Verification

## Error reproduced from supplied runtime log

Hibernate failed while flushing a transfer with:

`NullPointerException: Cannot invoke "java.lang.Long.longValue()" because "current" is null`

at `LongJavaType.next()` / `Versioning.incrementVersion()`.

The stack reaches `AuditLedgerService.verifyTransaction()` -> `ledgerRepository.flush()` -> transfer processing.

## Root cause fixed

Older database rows have NULL values in newly introduced Hibernate `@Version` columns. The current build now:

1. Initializes new entity versions to `0L`.
2. Automatically backfills NULL versions in `wallets`, `transactions`, and `topup_intents` at startup.
3. Converts those columns to `BIGINT NOT NULL DEFAULT 0` after backfill.
4. Keeps existing database data; no reset/drop is required.

## Verification performed in this environment

- Changed production Java files compile successfully against the project runtime dependencies.
- `LegacyVersionMigration` executable harness passed and verified all 3 UPDATE statements and all 3 NOT NULL/default ALTER statements.
- Existing wallet core + negative-path harness passed after the entity changes:
  `ALL CORE WALLET + NEGATIVE-PATH HARNESS TESTS PASSED`
- ZIP archive integrity checked after packaging.

A full Maven run cannot be executed in this sandbox because Maven Wrapper dependency download is blocked here. `build-and-run.sh` still runs the Maven suite on the user's Mac before starting port 8090.
