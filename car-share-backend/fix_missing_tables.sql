-- ============================================================
-- 修复缺失的业务表：blacklist + 其他可能缺失的表
-- 运行方式：在 MySQL 命令行或客户端中执行此文件
-- 使用数据库：USE car_share_db; SOURCE 此文件路径;
-- ============================================================

USE car_share_db;

-- 1. 黑名单表（缺失导致参与拼车失败）
CREATE TABLE IF NOT EXISTS `blacklist` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '黑名单ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `blocked_user_id` INT NOT NULL COMMENT '被拉黑用户ID',
  `reason` VARCHAR(255) COMMENT '拉黑原因',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_block` (`user_id`, `blocked_user_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单表';

-- 2. fee_detail 表
CREATE TABLE IF NOT EXISTS `fee_detail` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '费用明细ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `car_member_id` INT NOT NULL COMMENT '拼车成员ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `goods_amount` DECIMAL(10,2) COMMENT '商品金额',
  `shipping_fee` DECIMAL(10,2) DEFAULT 0 COMMENT '运费分摊',
  `total_amount` DECIMAL(10,2) COMMENT '应付总额',
  `shipping_fee_type` TINYINT DEFAULT 0 COMMENT '运费类型 0-均摊 1-按份数比例 2-车主承担',
  `deposit_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '定金',
  `balance_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '尾款',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`car_id`) REFERENCES `car`(`id`),
  INDEX `idx_car_id` (`car_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用明细表';

-- 3. notification 表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `car_id` INT COMMENT '拼车ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '内容',
  `type` TINYINT DEFAULT 1 COMMENT '类型 1系统 2成团 3分配 4凭证 5结算 6物流',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读 0未读 1已读',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 4. report 表
CREATE TABLE IF NOT EXISTS `report` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '举报ID',
  `user_id` INT NOT NULL COMMENT '举报人ID',
  `target_user_id` INT COMMENT '被举报人ID',
  `car_id` INT COMMENT '关联拼车ID',
  `type` TINYINT DEFAULT 1 COMMENT '类型 1虚假凭证 2恶意行为 3违规内容 4其他',
  `description` TEXT COMMENT '举报描述',
  `image_url` VARCHAR(500) COMMENT '图片证据',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0待处理 1已处理 2已驳回',
  `handle_result` VARCHAR(500) COMMENT '处理结果',
  `handled_by` INT COMMENT '处理人ID',
  `handled_at` DATETIME COMMENT '处理时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 5. car_share_record 表
CREATE TABLE IF NOT EXISTS `car_share_record` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '分享记录ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `user_id` INT NOT NULL COMMENT '分享人ID',
  `share_code` VARCHAR(32) UNIQUE COMMENT '分享码',
  `share_type` VARCHAR(20) DEFAULT 'friend' COMMENT '分享类型 friend/poster/code',
  `invite_user_id` INT COMMENT '通过分享加入的用户ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_car_id` (`car_id`),
  INDEX `idx_share_code` (`share_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享记录表';

-- 6. car_favorite 表
CREATE TABLE IF NOT EXISTS `car_favorite` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_car_user` (`car_id`, `user_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼车收藏表';

-- 7. car_order 表（ruoyi 有 order 表，用 car_order 避免冲突）
CREATE TABLE IF NOT EXISTS `car_order` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `car_member_id` INT COMMENT '拼车成员ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0待确认 1已确认 2已支付 3已完成',
  `settle_status` TINYINT DEFAULT 0 COMMENT '结算状态 0未结算 1已结算',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`car_id`) REFERENCES `car`(`id`),
  INDEX `idx_car_id` (`car_id`),
  INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼车订单表';

-- 8. car_comment 表
CREATE TABLE IF NOT EXISTS `car_comment` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `type` TINYINT DEFAULT 0 COMMENT '类型 0-评论 1-动态 2-晒单',
  `content` TEXT COMMENT '内容',
  `image_url` VARCHAR(500) COMMENT '图片',
  `reply_to_id` INT COMMENT '回复的评论ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_car_id` (`car_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼车评论表';

-- 9. refund 表
CREATE TABLE IF NOT EXISTS `refund` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '退款ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `car_member_id` INT COMMENT '拼车成员ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '退款金额',
  `type` TINYINT DEFAULT 1 COMMENT '类型 1主动退出 2拼车失败自动退款',
  `reason` VARCHAR(500) COMMENT '退款原因',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0申请中 1审核通过 2退款中 3已到账 4已驳回',
  `reject_reason` VARCHAR(500) COMMENT '驳回原因',
  `reviewed_by` INT COMMENT '审核人ID',
  `reviewed_at` DATETIME COMMENT '审核时间',
  `refunded_at` DATETIME COMMENT '退款时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`car_id`) REFERENCES `car`(`id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款表';

SELECT CONCAT('✅ 缺失表修复完成，共创建/确认 9 张表') AS result;
