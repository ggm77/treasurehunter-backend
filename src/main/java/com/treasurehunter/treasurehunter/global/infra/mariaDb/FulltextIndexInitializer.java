package com.treasurehunter.treasurehunter.global.infra.mariaDb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Component
@RequiredArgsConstructor
public class FulltextIndexInitializer {

    private final JdbcTemplate jdbcTemplate;

    private static final String INDEX_NAME = "idx_post_fulltext";
    private static final String TABLE_NAME = "post";
    private static final String LOCK_NAME = "lock_add_fulltext_post";

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(){

        validateIdentifier(TABLE_NAME);
        validateIdentifier(INDEX_NAME);

        // 락과 릴리즈를 한 커넥션안에서 진행
        jdbcTemplate.execute((ConnectionCallback<Void>) conn -> {
            try (final Statement stmt = conn.createStatement()) {
                // GET_LOCK
                try (final ResultSet rs = stmt.executeQuery("SELECT GET_LOCK('" + LOCK_NAME + "', 15)")) {
                    if (!rs.next() || rs.getInt(1) != 1) {
                        log.warn("[DB] Could not acquire lock '{}'; skipping FULLTEXT index creation.", LOCK_NAME);
                        return null;
                    }
                }

                // 이미 존재하는지 확인
                try (final PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name = ? AND index_name = ?")) {
                    ps.setString(1, TABLE_NAME);
                    ps.setString(2, INDEX_NAME);
                    try (final ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            log.info("[DB] FULLTEXT index '{}' already exists on table '{}'", INDEX_NAME, TABLE_NAME);
                            return null;
                        }
                    }
                }

                // 인덱스 생성
                final String ddl = "ALTER TABLE `" + TABLE_NAME + "` ADD FULLTEXT INDEX `" + INDEX_NAME + "` (`title`, `content`)";
                stmt.executeUpdate(ddl);
                log.info("[DB] FULLTEXT index '{}' successfully created on table '{}'", INDEX_NAME, TABLE_NAME);
            } catch (SQLException ex) {
                log.error("[DB] Failed to create FULLTEXT index: {}", ex.getMessage(), ex);
            } finally {
                // 릴리즈
                try (final Statement releaseStmt = conn.createStatement();
                     final ResultSet rs = releaseStmt.executeQuery("SELECT RELEASE_LOCK('" + LOCK_NAME + "')")) {
                    if (rs.next()) {
                        final int v = rs.getInt(1);
                        if (v != 1) {
                            log.warn("[DB] RELEASE_LOCK('{}') returned {}", LOCK_NAME, v);
                        }
                    }
                } catch (SQLException ex) {
                    log.warn("[DB] Error while releasing lock '{}'", LOCK_NAME, ex);
                }
            }
            return null;
        });
    }

    private void validateIdentifier(String id) {
        if (id == null || !id.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid identifier: " + id);
        }
    }
}
