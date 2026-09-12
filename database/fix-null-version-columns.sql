-- Manual fallback only. The application now performs this automatically at startup.
USE phoneWallet;

UPDATE wallets SET version = 0 WHERE version IS NULL;
UPDATE transactions SET version = 0 WHERE version IS NULL;
UPDATE topup_intents SET version = 0 WHERE version IS NULL;

ALTER TABLE wallets MODIFY COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE transactions MODIFY COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE topup_intents MODIFY COLUMN version BIGINT NOT NULL DEFAULT 0;
