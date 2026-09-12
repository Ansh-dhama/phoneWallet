package com.example.phoneWallet;

import com.example.phoneWallet.config.LegacyVersionMigration;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LegacyVersionMigrationTest {

    static class FakeJdbcTemplate extends JdbcTemplate {
        final List<String> calls = new ArrayList<>();

        @Override
        public int update(String sql) {
            calls.add("UPDATE:" + sql);
            return 2;
        }

        @Override
        public void execute(String sql) {
            calls.add("EXECUTE:" + sql);
        }

        @Override
        public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
            calls.add("QUERY:" + args[0]);
            return requiredType.cast(Integer.valueOf(1));
        }
    }

    @Test
    void backfillsLegacyNullVersionsAndHardensNullableColumns() throws Exception {
        FakeJdbcTemplate jdbc = new FakeJdbcTemplate();
        new LegacyVersionMigration(jdbc, true).run(null);

        for (String table : List.of("wallets", "transactions", "topup_intents")) {
            assertTrue(jdbc.calls.contains(
                    "UPDATE:UPDATE `" + table + "` SET `version` = 0 WHERE `version` IS NULL"
            ));
            assertTrue(jdbc.calls.contains(
                    "EXECUTE:ALTER TABLE `" + table + "` MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0"
            ));
        }
        assertEquals(9, jdbc.calls.size());
    }

    @Test
    void canBeDisabled() throws Exception {
        FakeJdbcTemplate jdbc = new FakeJdbcTemplate();
        new LegacyVersionMigration(jdbc, false).run(null);
        assertTrue(jdbc.calls.isEmpty());
    }
}
