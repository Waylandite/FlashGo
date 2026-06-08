-- FlashGo V2 鉴权基础结构
-- 说明：
-- 1. 为现有主体表补充密码摘要字段
-- 2. 新增平台账号表，用于 ROLE_PLATFORM 登录

ALTER TABLE `app_user`
  ADD COLUMN `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '登录密码摘要' AFTER `mobile`;

ALTER TABLE `merchant`
  ADD COLUMN `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '登录密码摘要' AFTER `contact_mobile`;

ALTER TABLE `rider`
  ADD COLUMN `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '登录密码摘要' AFTER `mobile`;

CREATE TABLE `platform_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '登录密码摘要',
  `display_name` VARCHAR(64) NOT NULL COMMENT '显示名称',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ENABLED' COMMENT '账号状态：ENABLED启用，DISABLED禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台管理账号表';
