package com.example.dm.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时做最小增量迁移，兜底历史库结构差异。
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DatabaseSchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureOrdersTableBaseline();
        migrateLegacyOrderTable();
        ensureEvaluationTableBaseline();
        ensureReservationTableBaseline();
        reconcileReservationOrderConsistency();
        dropNotificationTableIfExists();
    }

    private void ensureOrdersTableBaseline() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS orders ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "user_id INT NOT NULL,"
                + "session_id INT NOT NULL,"
                + "order_no VARCHAR(50) NULL,"
                + "total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,"
                + "status VARCHAR(20) NOT NULL DEFAULT 'PENDING',"
                + "pay_time DATETIME NULL,"
                + "pay_method VARCHAR(20) NULL,"
                + "remark VARCHAR(255) NULL,"
                + "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        ensureColumn("orders", "order_no", "ALTER TABLE orders ADD COLUMN order_no VARCHAR(50) NULL AFTER session_id");
        ensureColumn("orders", "total_price", "ALTER TABLE orders ADD COLUMN total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER session_id");
        ensureColumn("orders", "status", "ALTER TABLE orders ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' AFTER total_price");
        ensureColumn("orders", "pay_time", "ALTER TABLE orders ADD COLUMN pay_time DATETIME NULL AFTER status");
        ensureColumn("orders", "pay_method", "ALTER TABLE orders ADD COLUMN pay_method VARCHAR(20) NULL AFTER pay_time");
        ensureColumn("orders", "remark", "ALTER TABLE orders ADD COLUMN remark VARCHAR(255) NULL AFTER pay_method");
        ensureColumn("orders", "updated_at", "ALTER TABLE orders ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER create_time");

        if (!indexExists("orders", "uk_orders_order_no")) {
            try {
                jdbcTemplate.execute("ALTER TABLE orders ADD UNIQUE INDEX uk_orders_order_no(order_no)");
            } catch (Exception ignored) {
            }
        }

        jdbcTemplate.execute("UPDATE orders SET status = UPPER(status) WHERE status IS NOT NULL");
        jdbcTemplate.execute("UPDATE orders SET order_no = CONCAT('ORD', LPAD(id, 8, '0')) WHERE (order_no IS NULL OR order_no = '')");
        jdbcTemplate.execute("UPDATE orders SET total_price = 0.00 WHERE total_price IS NULL");
    }

    private void migrateLegacyOrderTable() {
        if (!tableExists("order")) {
            return;
        }
        jdbcTemplate.execute("INSERT INTO orders (user_id, session_id, create_time, status, total_price, updated_at) "
                + "SELECT o.user_id, o.session_id, COALESCE(o.create_time, NOW()), UPPER(COALESCE(o.status, 'PENDING')), 0.00, COALESCE(o.create_time, NOW()) "
                + "FROM `order` o "
                + "INNER JOIN game_session gs ON o.session_id = gs.id "
                + "INNER JOIN user u ON o.user_id = u.id "
                + "WHERE NOT EXISTS ("
                + "SELECT 1 FROM orders n "
                + "WHERE n.user_id = o.user_id "
                + "AND n.session_id = o.session_id "
                + "AND n.create_time = o.create_time"
                + ")");

        List<Integer> maxIdList = jdbcTemplate.query("SELECT COALESCE(MAX(id), 0) FROM orders", (rs, rowNum) -> rs.getInt(1));
        int nextId = maxIdList.isEmpty() ? 1 : maxIdList.get(0) + 1;
        if (nextId < 1) {
            nextId = 1;
        }
        jdbcTemplate.execute("ALTER TABLE orders AUTO_INCREMENT = " + nextId);
    }

    private void ensureEvaluationTableBaseline() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS evaluation ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "user_id INT NOT NULL,"
                + "session_id INT NOT NULL,"
                + "dm_id INT NOT NULL,"
                + "rating INT NOT NULL DEFAULT 5,"
                + "comment TEXT NULL,"
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        ensureColumn("evaluation", "session_id", "ALTER TABLE evaluation ADD COLUMN session_id INT NULL AFTER user_id");
        ensureColumn("evaluation", "dm_id", "ALTER TABLE evaluation ADD COLUMN dm_id INT NULL AFTER session_id");
        ensureColumn("evaluation", "rating", "ALTER TABLE evaluation ADD COLUMN rating INT NOT NULL DEFAULT 5 AFTER dm_id");
        ensureColumn("evaluation", "comment", "ALTER TABLE evaluation ADD COLUMN comment TEXT NULL AFTER rating");

        if (columnExists("evaluation", "score")) {
            jdbcTemplate.execute("UPDATE evaluation SET rating = CAST(score AS SIGNED) WHERE (rating IS NULL OR rating = 0) AND score IS NOT NULL");
        }
        if (columnExists("evaluation", "content")) {
            jdbcTemplate.execute("UPDATE evaluation SET comment = content WHERE (comment IS NULL OR comment = '') AND content IS NOT NULL");
        }
        if (columnExists("evaluation", "target_type") && columnExists("evaluation", "target_id")) {
            jdbcTemplate.execute("UPDATE evaluation SET session_id = target_id WHERE session_id IS NULL AND target_type = 'session'");
        }
    }

    private void ensureReservationTableBaseline() {
        if (!tableExists("reservation")) {
            return;
        }
        ensureColumn("reservation", "players_count",
                "ALTER TABLE reservation ADD COLUMN players_count INT NOT NULL DEFAULT 1 AFTER session_id");
        jdbcTemplate.execute("UPDATE reservation SET players_count = 1 WHERE players_count IS NULL OR players_count < 1");
    }

    private void reconcileReservationOrderConsistency() {
        if (!tableExists("reservation") || !tableExists("orders") || !tableExists("game_session")) {
            return;
        }

        // 历史数据修复：已支付/已完成订单对应的预约不应是已取消。
        jdbcTemplate.execute("UPDATE reservation r "
                + "JOIN orders o ON o.user_id = r.user_id AND o.session_id = r.session_id "
                + "SET r.status = 'CONFIRMED', r.updated_at = NOW() "
                + "WHERE UPPER(IFNULL(r.status, '')) = 'CANCELLED' "
                + "AND UPPER(IFNULL(o.status, '')) IN ('PAID', 'COMPLETED')");

        // 统一回填场次当前玩家数，避免展示与预约数据不一致。
        jdbcTemplate.execute("UPDATE game_session gs "
                + "LEFT JOIN ("
                + "SELECT session_id, IFNULL(SUM(players_count), 0) AS confirmed_players "
                + "FROM reservation "
                + "WHERE UPPER(IFNULL(status, '')) = 'CONFIRMED' "
                + "GROUP BY session_id"
                + ") r ON r.session_id = gs.id "
                + "SET gs.current_players = IFNULL(r.confirmed_players, 0)");
    }

    private void dropNotificationTableIfExists() {
        if (tableExists("notification")) {
            jdbcTemplate.execute("DROP TABLE IF EXISTS notification");
        }
    }

    private void ensureColumn(String tableName, String columnName, String alterSql) {
        if (!columnExists(tableName, columnName)) {
            jdbcTemplate.execute(alterSql);
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }

    private boolean indexExists(String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?",
                Integer.class,
                tableName,
                indexName
        );
        return count != null && count > 0;
    }
}
