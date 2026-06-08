-- FlashGo V1 初始数据库结构
-- 说明：
-- 1. 本脚本由 database/init_v1.sql 迁移而来，作为 Flyway 的首个正式迁移文件
-- 2. 当前仅覆盖第一版核心闭环：下单、接单、抢单、配送、结算、平台查看
-- 3. 用户表命名使用 app_user，避免与数据库系统对象混淆

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `app_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_no` VARCHAR(64) NOT NULL COMMENT '用户编号',
  `nickname` VARCHAR(64) NOT NULL COMMENT '用户昵称',
  `mobile` VARCHAR(32) NOT NULL COMMENT '手机号',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ENABLED' COMMENT '用户状态：ENABLED启用，DISABLED禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_no` (`user_no`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息表';

CREATE TABLE `merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_no` VARCHAR(64) NOT NULL COMMENT '商户编号',
  `name` VARCHAR(128) NOT NULL COMMENT '商户名称',
  `contact_name` VARCHAR(64) NOT NULL COMMENT '联系人姓名',
  `contact_mobile` VARCHAR(32) NOT NULL COMMENT '联系人手机号',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ENABLED' COMMENT '商户状态：ENABLED启用，DISABLED禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_no` (`merchant_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户主体信息表';

CREATE TABLE `shop` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_no` VARCHAR(64) NOT NULL COMMENT '店铺编号',
  `merchant_id` BIGINT NOT NULL COMMENT '所属商户ID',
  `name` VARCHAR(128) NOT NULL COMMENT '店铺名称',
  `logo_url` VARCHAR(255) DEFAULT NULL COMMENT '店铺Logo地址',
  `address` VARCHAR(255) NOT NULL COMMENT '店铺地址',
  `phone` VARCHAR(32) NOT NULL COMMENT '店铺联系电话',
  `business_status` VARCHAR(32) NOT NULL DEFAULT 'OPEN' COMMENT '营业状态：OPEN营业中，CLOSED休息中',
  `delivery_radius_km` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '配送半径，单位公里',
  `min_order_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '起送金额',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ENABLED' COMMENT '店铺状态：ENABLED启用，DISABLED禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_no` (`shop_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_business_status` (`business_status`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_shop_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户店铺信息表';

CREATE TABLE `rider` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rider_no` VARCHAR(64) NOT NULL COMMENT '骑手编号',
  `name` VARCHAR(64) NOT NULL COMMENT '骑手姓名',
  `mobile` VARCHAR(32) NOT NULL COMMENT '手机号',
  `vehicle_type` VARCHAR(32) NOT NULL DEFAULT 'ELECTRIC_BIKE' COMMENT '交通工具类型',
  `work_status` VARCHAR(32) NOT NULL DEFAULT 'OFFLINE' COMMENT '工作状态：IDLE空闲，BUSY忙碌，OFFLINE离线',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ENABLED' COMMENT '账号状态：ENABLED启用，DISABLED禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rider_no` (`rider_no`),
  UNIQUE KEY `uk_rider_mobile` (`mobile`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='骑手基础信息表';

CREATE TABLE `product_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` BIGINT NOT NULL COMMENT '所属店铺ID',
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ENABLED' COMMENT '分类状态：ENABLED启用，DISABLED禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_no` (`sort_no`),
  CONSTRAINT `fk_product_category_shop_id` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `shop_id` BIGINT NOT NULL COMMENT '所属店铺ID',
  `category_id` BIGINT NOT NULL COMMENT '商品分类ID',
  `name` VARCHAR(128) NOT NULL COMMENT '商品名称',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '商品图片地址',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品售价',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ON_SHELF' COMMENT '商品状态：ON_SHELF上架，OFF_SHELF下架',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '商品描述',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_product_shop_id` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`),
  CONSTRAINT `fk_product_category_id` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品信息表';

CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
  `rider_id` BIGINT DEFAULT NULL COMMENT '骑手ID，抢单前为空',
  `status` VARCHAR(64) NOT NULL COMMENT '订单主状态',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
  `delivery_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送费',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '用户实付金额',
  `receiver_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
  `receiver_mobile` VARCHAR(32) NOT NULL COMMENT '收货人手机号',
  `receiver_address` VARCHAR(255) NOT NULL COMMENT '收货地址',
  `user_remark` VARCHAR(255) DEFAULT NULL COMMENT '用户备注',
  `merchant_remark` VARCHAR(255) DEFAULT NULL COMMENT '商户备注',
  `placed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `paid_at` DATETIME DEFAULT NULL COMMENT '支付时间',
  `merchant_accepted_at` DATETIME DEFAULT NULL COMMENT '商户接单时间',
  `rider_assigned_at` DATETIME DEFAULT NULL COMMENT '骑手抢单时间',
  `picked_up_at` DATETIME DEFAULT NULL COMMENT '取货时间',
  `delivering_at` DATETIME DEFAULT NULL COMMENT '开始配送时间',
  `delivered_at` DATETIME DEFAULT NULL COMMENT '送达时间',
  `settled_at` DATETIME DEFAULT NULL COMMENT '结算完成时间',
  `cancelled_at` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `cancel_by_role` VARCHAR(32) DEFAULT NULL COMMENT '取消角色：USER用户，MERCHANT商户，RIDER骑手，PLATFORM平台，SYSTEM系统',
  `cancel_by_id` BIGINT DEFAULT NULL COMMENT '取消操作人ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_paid_at` (`paid_at`),
  CONSTRAINT `fk_orders_user_id` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`),
  CONSTRAINT `fk_orders_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`),
  CONSTRAINT `fk_orders_shop_id` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`),
  CONSTRAINT `fk_orders_rider_id` FOREIGN KEY (`rider_id`) REFERENCES `rider` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(128) NOT NULL COMMENT '下单时商品名称快照',
  `product_price` DECIMAL(10,2) NOT NULL COMMENT '下单时商品单价快照',
  `quantity` INT NOT NULL COMMENT '购买数量',
  `line_amount` DECIMAL(10,2) NOT NULL COMMENT '商品行金额',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_order_item_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_order_item_product_id` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品明细表';

CREATE TABLE `order_status_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `from_status` VARCHAR(64) DEFAULT NULL COMMENT '变更前状态，首次创建可为空',
  `to_status` VARCHAR(64) NOT NULL COMMENT '变更后状态',
  `operator_role` VARCHAR(32) NOT NULL COMMENT '操作角色：USER用户，MERCHANT商户，RIDER骑手，PLATFORM平台，SYSTEM系统',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人名称快照',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_order_status_log_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态流转日志表';

CREATE TABLE `order_settlement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `rider_id` BIGINT DEFAULT NULL COMMENT '骑手ID',
  `goods_amount` DECIMAL(10,2) NOT NULL COMMENT '商品金额',
  `delivery_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送费',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '用户实付金额',
  `platform_commission_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '平台抽佣金额',
  `merchant_income_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商户应结金额',
  `rider_income_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '骑手应结金额',
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '结算状态：PENDING待结算，SETTLED已结算',
  `settled_at` DATETIME DEFAULT NULL COMMENT '结算完成时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_rider_id` (`rider_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_order_settlement_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_order_settlement_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchant` (`id`),
  CONSTRAINT `fk_order_settlement_rider_id` FOREIGN KEY (`rider_id`) REFERENCES `rider` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单结算表';

SET FOREIGN_KEY_CHECKS = 1;
