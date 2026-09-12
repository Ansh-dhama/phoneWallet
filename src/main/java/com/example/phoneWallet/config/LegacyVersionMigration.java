package com.example.phoneWallet.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Compatibility migration for databases created by older builds.
 * Existing rows may have NULL in Hibernate @Version columns that were added
 * later by ddl-auto=update. Hibernate cannot increment a NULL Long version.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LegacyVersionMigration implements ApplicationRunner {

    private static final String[] VERSIONED_TABLES = {
            "wallets",
            "transactions",
            "topup_intents"
    };

    private final JdbcTemplate jdbcTemplate;
    private final boolean enabled;

    public LegacyVersionMigration(
            JdbcTemplate jdbcTemplate,
            @Value("${wallet.db.legacy-version-backfill-enabled:true}") boolean enabled) {
        this.jdbcTemplate = jdbcTemplate;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Legacy @Version backfill is disabled");
            return;
        }
        for (String table : VERSIONED_TABLES) {
            migrateVersionColumn(table);
        }
    }

    void migrateVersionColumn(String table) {
        int updated = jdbcTemplate.update(
                "UPDATE `" + table + "` SET `version` = 0 WHERE `version` IS NULL"
        );

        Integer nullable = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() " +
                        "AND table_name = ? AND column_name = 'version' AND is_nullable = 'YES'",
                Integer.class,
                table
        );

        if (nullable != null && nullable > 0) {
            jdbcTemplate.execute(
                    "ALTER TABLE `" + table + "` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0"
            );
        }

        if (updated > 0) {
            log.warn("Backfilled {} legacy NULL version value(s) in {}", updated, table);
        }
    }
}
