-- Real testing reset: drop unused legacy tables and clear business data.
-- Database: dm_scheduler

SET FOREIGN_KEY_CHECKS = 0;

-- 1) Drop tables that are not referenced by current business code.
DROP TABLE IF EXISTS `payment_transaction`;
DROP TABLE IF EXISTS `message_record`;
DROP TABLE IF EXISTS `message_template`;
DROP TABLE IF EXISTS `payment_config`;
DROP TABLE IF EXISTS `location`;
DROP TABLE IF EXISTS `ticket`;
DROP TABLE IF EXISTS `rule`;
DROP TABLE IF EXISTS `order`;

-- 2) Clear business data (keep only admin account).
TRUNCATE TABLE `dm_schedule`;
TRUNCATE TABLE `evaluation`;
TRUNCATE TABLE `notification`;
TRUNCATE TABLE `operation_log`;
TRUNCATE TABLE `reservation`;
TRUNCATE TABLE `orders`;
TRUNCATE TABLE `game_session`;
TRUNCATE TABLE `dm`;
TRUNCATE TABLE `script`;
TRUNCATE TABLE `store`;
TRUNCATE TABLE `system_config`;

DELETE FROM `user` WHERE `username` <> 'admin';

INSERT INTO `user` (`username`, `password`, `email`, `role`, `status`, `phone`, `version`)
VALUES ('admin', 'admin123', 'admin@example.com', 'ADMIN', 'ACTIVE', '13800138000', 0)
ON DUPLICATE KEY UPDATE
  `password` = VALUES(`password`),
  `email` = VALUES(`email`),
  `role` = 'ADMIN',
  `status` = 'ACTIVE';

SET FOREIGN_KEY_CHECKS = 1;
