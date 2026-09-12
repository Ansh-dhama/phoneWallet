# Test Fix Applied After Mac Verification

The developer-machine run on 2026-09-12 proved that:

- React/Vite production build completed successfully.
- 124 application source files compiled successfully with Maven.
- `IdempotencyAndSignatureTest`, `PhoneWalletApplicationTests`, `TransactionServiceLoadMoneyTest`, and `StaticFrontendPackagingTest` passed.
- `InfrastructureIntegrationTest` was skipped only because Docker was not running.
- The only Maven failure was `AuditLedgerServiceTest.flushesPendingLedgerEntriesBeforeBalanceCheck`.

## Why that test failed

The production service intentionally calls the same repository method twice: once with the debit entry-type list and once with the credit entry-type list. The old Mockito verification used `anyList()` for both calls. Because both real calls matched that same matcher, the first `InOrder.verify(...)` saw two matching invocations and failed with "Wanted 1 time, But was 2 times".

This was a test assertion bug, not a wallet ledger bug. The Spring console had already shown the balanced `TX-1` ledger verification succeeded before Mockito verification failed.

## Fix

`AuditLedgerServiceTest` now stubs and verifies the exact debit and credit type lists separately:

- Debit: `DEBIT`, `REVERSAL_DEBIT`, `REFUND_DEBIT`, `EXTERNAL_DEBIT`
- Credit: `CREDIT`, `REVERSAL_CREDIT`, `REFUND_CREDIT`, `EXTERNAL_CREDIT`

This makes the test verify the actual behavior unambiguously while still checking that `flush()` occurs before both SUM queries.

## Docker integration test

The infrastructure test remains `@Testcontainers(disabledWithoutDocker = true)`. When Docker Desktop is off it is intentionally skipped, not failed. Start Docker Desktop and run:

```bash
./mvnw -Dtest=InfrastructureIntegrationTest test
```

for the MySQL 8 + Redis Testcontainers integration test.
