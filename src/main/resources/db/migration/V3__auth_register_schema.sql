-- FlashGo V3 注册能力结构调整
-- 说明：
-- 1. 为用户、商户、骑手补充 username 登录名字段
-- 2. 允许手机号类字段为空，注册阶段仅使用用户名 + 密码

ALTER TABLE `app_user`
  ADD COLUMN `username` VARCHAR(64) DEFAULT NULL COMMENT '登录用户名' AFTER `user_no`,
  MODIFY COLUMN `mobile` VARCHAR(32) NULL COMMENT '手机号',
  ADD UNIQUE KEY `uk_username` (`username`);

ALTER TABLE `merchant`
  ADD COLUMN `username` VARCHAR(64) DEFAULT NULL COMMENT '登录用户名' AFTER `merchant_no`,
  MODIFY COLUMN `contact_mobile` VARCHAR(32) NULL COMMENT '联系人手机号',
  ADD UNIQUE KEY `uk_username` (`username`);

ALTER TABLE `rider`
  ADD COLUMN `username` VARCHAR(64) DEFAULT NULL COMMENT '登录用户名' AFTER `rider_no`,
  MODIFY COLUMN `mobile` VARCHAR(32) NULL COMMENT '手机号',
  ADD UNIQUE KEY `uk_username` (`username`);
