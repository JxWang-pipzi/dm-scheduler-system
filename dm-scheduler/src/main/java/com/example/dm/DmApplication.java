package com.example.dm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@SpringBootApplication
@MapperScan(basePackages = "com.example.dm.mapper")
public class DmApplication {
    public static void main(String[] args) {
        SpringApplication.run(DmApplication.class, args);
    }
    
    @Bean
    public ApplicationRunner initDatabase(DataSource dataSource) {
        return args -> {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // Create user table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `user` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`username` VARCHAR(50) NOT NULL UNIQUE," +
                "`password` VARCHAR(100) NOT NULL," +
                "`email` VARCHAR(100) NULL," +
                "`role` VARCHAR(20) NOT NULL," +
                "`status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'," +
                "`phone` VARCHAR(20)," +
                "`real_name` VARCHAR(100)," +
                "`avatar` VARCHAR(255)," +
                "`version` INT NOT NULL DEFAULT 0," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            Integer hasRealName = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'real_name'",
                Integer.class
            );
            if (hasRealName != null && hasRealName == 0) {
                jdbcTemplate.execute("ALTER TABLE `user` ADD COLUMN `real_name` VARCHAR(100) NULL AFTER `phone`;");
            }

            Integer hasAvatar = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'avatar'",
                Integer.class
            );
            if (hasAvatar != null && hasAvatar == 0) {
                jdbcTemplate.execute("ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(255) NULL AFTER `real_name`;");
            }

            Integer hasEmail = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'email'",
                Integer.class
            );
            if (hasEmail != null && hasEmail == 0) {
                jdbcTemplate.execute("ALTER TABLE `user` ADD COLUMN `email` VARCHAR(100) NULL AFTER `password`;");
            }

            Integer hasStatus = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'status'",
                Integer.class
            );
            if (hasStatus != null && hasStatus == 0) {
                jdbcTemplate.execute("ALTER TABLE `user` ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER `role`;");
            }

            Integer hasVersion = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'version'",
                Integer.class
            );
            if (hasVersion != null && hasVersion == 0) {
                jdbcTemplate.execute("ALTER TABLE `user` ADD COLUMN `version` INT NOT NULL DEFAULT 0 AFTER `avatar`;");
            }

            Integer hasUpdatedAt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'updated_at'",
                Integer.class
            );
            if (hasUpdatedAt != null && hasUpdatedAt == 0) {
                jdbcTemplate.execute("ALTER TABLE `user` ADD COLUMN `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`;");
            }
            
            // Create DM table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `dm` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id` INT NOT NULL," +
                "`dm_level` INT NOT NULL DEFAULT 1," +
                "`experience` INT NOT NULL DEFAULT 0," +
                "`rating` DOUBLE NOT NULL DEFAULT 0," +
                "`status` VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'," +
                "`specialty` VARCHAR(255) DEFAULT ''," +
                "`total_sessions` INT NOT NULL DEFAULT 0," +
                "`weekly_max_sessions` INT NOT NULL DEFAULT 10," +
                "`bio` TEXT," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            Integer hasDmStatus = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm' AND COLUMN_NAME = 'status'",
                Integer.class
            );
            if (hasDmStatus != null && hasDmStatus == 0) {
                jdbcTemplate.execute("ALTER TABLE `dm` ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' AFTER `rating`;");
            }

            Integer hasDmSpecialty = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm' AND COLUMN_NAME = 'specialty'",
                Integer.class
            );
            if (hasDmSpecialty != null && hasDmSpecialty == 0) {
                jdbcTemplate.execute("ALTER TABLE `dm` ADD COLUMN `specialty` VARCHAR(255) DEFAULT '' AFTER `status`;");
            }

            Integer hasDmTotalSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm' AND COLUMN_NAME = 'total_sessions'",
                Integer.class
            );
            if (hasDmTotalSessions != null && hasDmTotalSessions == 0) {
                jdbcTemplate.execute("ALTER TABLE `dm` ADD COLUMN `total_sessions` INT NOT NULL DEFAULT 0 AFTER `specialty`;");
            }

            Integer hasDmWeeklyMaxSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm' AND COLUMN_NAME = 'weekly_max_sessions'",
                Integer.class
            );
            if (hasDmWeeklyMaxSessions != null && hasDmWeeklyMaxSessions == 0) {
                jdbcTemplate.execute("ALTER TABLE `dm` ADD COLUMN `weekly_max_sessions` INT NOT NULL DEFAULT 10 AFTER `total_sessions`;");
            }

            Integer hasDmBio = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm' AND COLUMN_NAME = 'bio'",
                Integer.class
            );
            if (hasDmBio != null && hasDmBio == 0) {
                jdbcTemplate.execute("ALTER TABLE `dm` ADD COLUMN `bio` TEXT AFTER `weekly_max_sessions`;");
            }

            Integer hasDmUpdatedAt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dm' AND COLUMN_NAME = 'updated_at'",
                Integer.class
            );
            if (hasDmUpdatedAt != null && hasDmUpdatedAt == 0) {
                jdbcTemplate.execute("ALTER TABLE `dm` ADD COLUMN `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`;");
            }
            
            // Create script table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `script` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`script_name` VARCHAR(100) NOT NULL," +
                "`type` VARCHAR(50) NOT NULL," +
                "`difficulty` VARCHAR(20) NOT NULL," +
                "`need_dm_level` INT NOT NULL DEFAULT 1," +
                "`min_players` INT NOT NULL DEFAULT 4," +
                "`max_players` INT NOT NULL," +
                "`duration` INT NOT NULL DEFAULT 120," +
                "`price` DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                "`cover_image` VARCHAR(255)," +
                "`tags` VARCHAR(255)," +
                "`description` TEXT," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ",`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            Integer hasScriptMinPlayers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'script' AND COLUMN_NAME = 'min_players'",
                Integer.class
            );
            if (hasScriptMinPlayers != null && hasScriptMinPlayers == 0) {
                jdbcTemplate.execute("ALTER TABLE `script` ADD COLUMN `min_players` INT NOT NULL DEFAULT 4 AFTER `need_dm_level`;");
            }

            Integer hasScriptDuration = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'script' AND COLUMN_NAME = 'duration'",
                Integer.class
            );
            if (hasScriptDuration != null && hasScriptDuration == 0) {
                jdbcTemplate.execute("ALTER TABLE `script` ADD COLUMN `duration` INT NOT NULL DEFAULT 120 AFTER `max_players`;");
            }

            Integer hasScriptPrice = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'script' AND COLUMN_NAME = 'price'",
                Integer.class
            );
            if (hasScriptPrice != null && hasScriptPrice == 0) {
                jdbcTemplate.execute("ALTER TABLE `script` ADD COLUMN `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER `duration`;");
            }

            Integer hasScriptCoverImage = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'script' AND COLUMN_NAME = 'cover_image'",
                Integer.class
            );
            if (hasScriptCoverImage != null && hasScriptCoverImage == 0) {
                jdbcTemplate.execute("ALTER TABLE `script` ADD COLUMN `cover_image` VARCHAR(255) NULL AFTER `price`;");
            }

            Integer hasScriptTags = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'script' AND COLUMN_NAME = 'tags'",
                Integer.class
            );
            if (hasScriptTags != null && hasScriptTags == 0) {
                jdbcTemplate.execute("ALTER TABLE `script` ADD COLUMN `tags` VARCHAR(255) NULL AFTER `cover_image`;");
            }

            Integer hasScriptDescription = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'script' AND COLUMN_NAME = 'description'",
                Integer.class
            );
            if (hasScriptDescription != null && hasScriptDescription == 0) {
                jdbcTemplate.execute("ALTER TABLE `script` ADD COLUMN `description` TEXT AFTER `tags`;");
            }

            Integer hasScriptUpdatedAt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'script' AND COLUMN_NAME = 'updated_at'",
                Integer.class
            );
            if (hasScriptUpdatedAt != null && hasScriptUpdatedAt == 0) {
                jdbcTemplate.execute("ALTER TABLE `script` ADD COLUMN `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`;");
            }
            
            // Create game_session table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `game_session` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`script_id` INT NOT NULL," +
                "`dm_id` INT NOT NULL," +
                "`start_time` DATETIME NOT NULL," +
                "`end_time` DATETIME NOT NULL," +
                "`status` VARCHAR(20) NOT NULL DEFAULT 'PENDING'," +
                "`max_players` INT NOT NULL DEFAULT 6," +
                "`current_players` INT NOT NULL DEFAULT 0," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (`script_id`) REFERENCES `script` (`id`) ON DELETE CASCADE," +
                "FOREIGN KEY (`dm_id`) REFERENCES `dm` (`id`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            
            // Create reservation table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `reservation` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id` INT NOT NULL," +
                "`session_id` INT NOT NULL," +
                "`players_count` INT NOT NULL DEFAULT 1," +
                "`status` VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE," +
                "FOREIGN KEY (`session_id`) REFERENCES `game_session` (`id`) ON DELETE CASCADE," +
                "UNIQUE KEY `uk_user_session` (`user_id`, `session_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            Integer hasReservationPlayersCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reservation' AND COLUMN_NAME = 'players_count'",
                Integer.class
            );
            if (hasReservationPlayersCount != null && hasReservationPlayersCount == 0) {
                jdbcTemplate.execute("ALTER TABLE `reservation` ADD COLUMN `players_count` INT NOT NULL DEFAULT 1 AFTER `session_id`;");
                jdbcTemplate.execute("UPDATE `reservation` SET `players_count` = 1 WHERE `players_count` IS NULL OR `players_count` < 1;");
            }
            
            // 订单主表由 DatabaseSchemaMigrationRunner 统一维护（orders）。不再创建 legacy `order` 表。

            // Create evaluation table (new schema)
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `evaluation` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id` INT NOT NULL," +
                "`session_id` INT NOT NULL," +
                "`dm_id` INT NOT NULL," +
                "`rating` INT NOT NULL DEFAULT 5," +
                "`comment` TEXT," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // Create store table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `store` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`store_name` VARCHAR(100) NOT NULL," +
                "`address` VARCHAR(200) NOT NULL," +
                "`phone` VARCHAR(20) NOT NULL," +
                "`description` TEXT," +
                "`room_count` INT DEFAULT 0," +
                "`business_hours` VARCHAR(100)," +
                "`latitude` DECIMAL(10,6)," +
                "`longitude` DECIMAL(10,6)," +
                "`status` VARCHAR(20) DEFAULT 'OPEN'," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            Integer hasStoreRoomCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store' AND COLUMN_NAME = 'room_count'",
                Integer.class
            );
            if (hasStoreRoomCount != null && hasStoreRoomCount == 0) {
                jdbcTemplate.execute("ALTER TABLE `store` ADD COLUMN `room_count` INT DEFAULT 0 AFTER `description`;");
            }

            Integer hasStoreBusinessHours = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store' AND COLUMN_NAME = 'business_hours'",
                Integer.class
            );
            if (hasStoreBusinessHours != null && hasStoreBusinessHours == 0) {
                jdbcTemplate.execute("ALTER TABLE `store` ADD COLUMN `business_hours` VARCHAR(100) NULL AFTER `room_count`;");
            }

            Integer hasStoreLatitude = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store' AND COLUMN_NAME = 'latitude'",
                Integer.class
            );
            if (hasStoreLatitude != null && hasStoreLatitude == 0) {
                jdbcTemplate.execute("ALTER TABLE `store` ADD COLUMN `latitude` DECIMAL(10,6) NULL AFTER `business_hours`;");
            }

            Integer hasStoreLongitude = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store' AND COLUMN_NAME = 'longitude'",
                Integer.class
            );
            if (hasStoreLongitude != null && hasStoreLongitude == 0) {
                jdbcTemplate.execute("ALTER TABLE `store` ADD COLUMN `longitude` DECIMAL(10,6) NULL AFTER `latitude`;");
            }

            Integer hasStoreStatus = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'store' AND COLUMN_NAME = 'status'",
                Integer.class
            );
            if (hasStoreStatus != null && hasStoreStatus == 0) {
                jdbcTemplate.execute("ALTER TABLE `store` ADD COLUMN `status` VARCHAR(20) DEFAULT 'OPEN' AFTER `longitude`;");
            }

            // Create system_config table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `system_config` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`config_key` VARCHAR(100) NOT NULL UNIQUE," +
                "`config_value` TEXT NOT NULL," +
                "`description` VARCHAR(200)," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // Create operation_log table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `operation_log` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id` INT NULL," +
                "`action` VARCHAR(50) NOT NULL," +
                "`target` VARCHAR(50) NULL," +
                "`detail` TEXT NULL," +
                "`ip` VARCHAR(50) NULL," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // Create dm_schedule table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `dm_schedule` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`dm_id` INT NOT NULL," +
                "`schedule_date` DATE NOT NULL," +
                "`time_slot` VARCHAR(20) NOT NULL," +
                "`session_id` INT NULL," +
                "`status` VARCHAR(20) DEFAULT 'AVAILABLE'," +
                "`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "`updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `notification` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id` INT NULL," +
                "`type` VARCHAR(50) NULL," +
                "`title` VARCHAR(200) NULL," +
                "`content` TEXT NULL," +
                "`is_read` INT DEFAULT 0," +
                "`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

            // Ensure admin account exists for clean-environment login.
            jdbcTemplate.execute("INSERT INTO `user` (`username`, `password`, `email`, `role`, `status`, `phone`, `version`) " +
                "VALUES ('admin', 'admin123', 'admin@example.com', 'ADMIN', 'ACTIVE', '13800138000', 0) " +
                "ON DUPLICATE KEY UPDATE " +
                "`password` = IF(`password` IS NULL OR `password` = '', VALUES(`password`), `password`), " +
                "`email` = IF(`email` IS NULL OR `email` = '', VALUES(`email`), `email`), " +
                "`role` = IF(`role` IS NULL OR `role` = '', VALUES(`role`), `role`), " +
                "`status` = IF(`status` IS NULL OR `status` = '', VALUES(`status`), `status`);");
            
            System.out.println("Database tables initialized successfully");
        };
    }
}
