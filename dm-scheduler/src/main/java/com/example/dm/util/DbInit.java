package com.example.dm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * 独立数据库初始化工具（手动运行）
 * 数据库密码通过环境变量 DB_PASSWORD 读取，不再硬编码
 */
public class DbInit {
    public static void main(String[] args) {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("MySQL driver not found");
            return;
        }

        String url = "jdbc:mysql://localhost:3306/dm_scheduler?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC";
        String username = System.getenv().getOrDefault("DB_USERNAME", "root");
        String password = System.getenv("DB_PASSWORD");

        if (password == null || password.isEmpty()) {
            System.out.println("[错误] 请先设置环境变量 DB_PASSWORD");
            System.out.println("  PowerShell: $env:DB_PASSWORD = '你的数据库密码'");
            System.out.println("  CMD:        set DB_PASSWORD=你的数据库密码");
            return;
        }

        String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin123");
        if ("admin123".equals(adminPassword)) {
            System.out.println("[警告] 正在使用默认管理员密码 admin123，请在生产环境中通过 ADMIN_PASSWORD 环境变量设置强密码");
        }

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {

            // Create user table
            String createUserTable = "CREATE TABLE IF NOT EXISTS `user` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`username` VARCHAR(50) NOT NULL UNIQUE," +
                "`password` VARCHAR(100) NOT NULL," +
                "`role` VARCHAR(20) NOT NULL," +
                "`phone` VARCHAR(20)," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.executeUpdate(createUserTable);

            // Create DM table
            String createDmTable = "CREATE TABLE IF NOT EXISTS `dm` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id` INT NOT NULL," +
                "`dm_level` INT NOT NULL DEFAULT 1," +
                "`experience` INT NOT NULL DEFAULT 0," +
                "`rating` DOUBLE NOT NULL DEFAULT 0," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.executeUpdate(createDmTable);

            // Create script table
            String createScriptTable = "CREATE TABLE IF NOT EXISTS `script` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`script_name` VARCHAR(100) NOT NULL," +
                "`type` VARCHAR(50) NOT NULL," +
                "`difficulty` VARCHAR(20) NOT NULL," +
                "`need_dm_level` INT NOT NULL DEFAULT 1," +
                "`max_players` INT NOT NULL," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.executeUpdate(createScriptTable);

            // Create session table
            String createSessionTable = "CREATE TABLE IF NOT EXISTS `session` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`script_id` INT NOT NULL," +
                "`dm_id` INT NOT NULL," +
                "`start_time` DATETIME NOT NULL," +
                "`end_time` DATETIME NOT NULL," +
                "`status` VARCHAR(20) NOT NULL DEFAULT 'pending'," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (`script_id`) REFERENCES `script` (`id`) ON DELETE CASCADE," +
                "FOREIGN KEY (`dm_id`) REFERENCES `dm` (`id`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.executeUpdate(createSessionTable);

            // Create order table
            String createOrderTable = "CREATE TABLE IF NOT EXISTS `order` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id` INT NOT NULL," +
                "`session_id` INT NOT NULL," +
                "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "`status` VARCHAR(20) NOT NULL DEFAULT 'pending'," +
                "FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE," +
                "FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.executeUpdate(createOrderTable);

            // Insert admin user
            String insertAdmin = "INSERT IGNORE INTO `user` (`username`, `password`, `role`, `phone`) VALUES ('admin', '" + adminPassword + "', 'admin', '13800138000');";
            stmt.executeUpdate(insertAdmin);

            // Insert default DM
            String insertDm = "INSERT IGNORE INTO `dm` (`user_id`, `dm_level`, `experience`, `rating`) VALUES (1, 3, 1000, 4.8);";
            stmt.executeUpdate(insertDm);

            // Insert default scripts
            String insertScript1 = "INSERT IGNORE INTO `script` (`script_name`, `type`, `difficulty`, `need_dm_level`, `max_players`) VALUES ('Murder Mystery', 'Detective', 'Easy', 1, 6);";
            String insertScript2 = "INSERT IGNORE INTO `script` (`script_name`, `type`, `difficulty`, `need_dm_level`, `max_players`) VALUES ('Horror Rhyme', 'Horror', 'Medium', 2, 5);";
            String insertScript3 = "INSERT IGNORE INTO `script` (`script_name`, `type`, `difficulty`, `need_dm_level`, `max_players`) VALUES ('Palace Secret', 'Ancient', 'Hard', 3, 7);";
            stmt.executeUpdate(insertScript1);
            stmt.executeUpdate(insertScript2);
            stmt.executeUpdate(insertScript3);

            // Insert default sessions
            String insertSession1 = "INSERT IGNORE INTO `session` (`script_id`, `dm_id`, `start_time`, `end_time`, `status`) VALUES (1, 1, NOW(), DATE_ADD(NOW(), INTERVAL 4 HOUR), 'pending');";
            String insertSession2 = "INSERT IGNORE INTO `session` (`script_id`, `dm_id`, `start_time`, `end_time`, `status`) VALUES (2, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY 4 HOUR), 'pending');";
            stmt.executeUpdate(insertSession1);
            stmt.executeUpdate(insertSession2);

            System.out.println("Database tables created successfully");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to create database tables: " + e.getMessage());
        }
    }
}
