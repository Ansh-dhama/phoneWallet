# Runtime fix: Hibernate @Version NULL crash

The transfer 500 was caused by legacy rows with `version = NULL` after `@Version` columns were added by `ddl-auto=update`.
Hibernate then reached `LongJavaType.next()` during flush and tried to increment a NULL `Long`.

This build fixes both sides:

- `Wallet.version`, `Transaction.version`, and `TopUpIntent.version` initialize to `0L` for new rows.
- `LegacyVersionMigration` automatically updates old rows to `version = 0` at startup.
- It then changes those columns to `BIGINT NOT NULL DEFAULT 0` so the problem cannot recur.
- Existing data is preserved; no database reset is required.
