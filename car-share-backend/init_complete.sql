-- ============================================================
-- 拼车协作平台 完整数据库初始化脚本
-- 合并 RuoYi 系统表 + 拼车业务表 + 测试数据
-- ============================================================

-- 1. 导入 RuoYi 框架表（系统管理）
SOURCE d:/project/ruoyi-vue-temp/sql/ry_20260417.sql;

-- 2. 创建拼车业务表（如果不存在）
USE car_share_db;

CREATE TABLE IF NOT EXISTS `user` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `openid` VARCHAR(64) NOT NULL UNIQUE COMMENT '微信openid',
  `unionid` VARCHAR(64) UNIQUE COMMENT '微信unionid',
  `session_key` VARCHAR(255) COMMENT '微信session_key',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `phone` VARCHAR(20) COMMENT '手机号',
  `password` VARCHAR(255) COMMENT '密码',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `credit_score` INT DEFAULT 60 COMMENT '信用分 0-100',
  `credit_level` TINYINT DEFAULT 3 COMMENT '信用等级 1-5',
  `role` TINYINT DEFAULT 0 COMMENT '角色 0普通用户 1车主 2管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常 2黑名单',
  `total_transactions` INT DEFAULT 0 COMMENT '累计交易次数',
  `success_transactions` INT DEFAULT 0 COMMENT '成功交易次数',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_login_at` DATETIME COMMENT '最后登录时间',
  INDEX `idx_openid` (`openid`),
  INDEX `idx_status` (`status`),
  INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `user_address` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '收件人姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '收件人电话',
  `province` VARCHAR(50) COMMENT '省份',
  `city` VARCHAR(50) COMMENT '城市',
  `district` VARCHAR(50) COMMENT '区县',
  `detail` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认地址',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0无效 1有效',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

CREATE TABLE IF NOT EXISTS `user_preference` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '偏好ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `preferred_versions` VARCHAR(1000) COMMENT '偏好版本 JSON数组',
  `preferred_cards` VARCHAR(1000) COMMENT '偏好小卡 JSON数组',
  `preferred_artists` VARCHAR(1000) COMMENT '偏好艺人 JSON数组',
  `auto_match` TINYINT DEFAULT 1 COMMENT '自动匹配开关 0关闭 1开启',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好设置表';

CREATE TABLE IF NOT EXISTS `goods` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `type` VARCHAR(50) COMMENT '类型：专辑/周边/写真等',
  `versions` VARCHAR(2000) COMMENT '可用版本 JSON数组',
  `cards` VARCHAR(2000) COMMENT '小卡信息 JSON数组',
  `market_price` DECIMAL(10,2) COMMENT '市场价',
  `image_url` VARCHAR(255) COMMENT '商品主图URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0下架 1上架',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_name` (`name`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE IF NOT EXISTS `car` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '拼车ID',
  `user_id` INT NOT NULL COMMENT '车主ID',
  `title` VARCHAR(100) NOT NULL COMMENT '拼车标题',
  `goods_id` INT COMMENT '商品ID',
  `goods_name` VARCHAR(100) COMMENT '商品名称冗余',
  `goods_image` VARCHAR(255) COMMENT '商品图片冗余',
  `description` TEXT COMMENT '拼车说明',
  `total_count` INT NOT NULL COMMENT '总人数',
  `current_count` INT DEFAULT 0 COMMENT '当前人数',
  `price_total` DECIMAL(10,2) NOT NULL COMMENT '总价',
  `price_per` DECIMAL(10,2) NOT NULL COMMENT '人均价格',
  `deposit_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '定金金额',
  `deadline` DATETIME COMMENT '截止时间',
  `distribution_type` TINYINT DEFAULT 0 COMMENT '分配方式 0随机 1偏好优先 2抽签',
  `is_restricted` TINYINT DEFAULT 0 COMMENT '是否限制信用分 0不限制 1限制',
  `min_credit_score` INT DEFAULT 0 COMMENT '最低信用分要求',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0招募中 1已截止 2成团中 3物流中 4已完成 5已取消 6已过期',
  `success_rate` DECIMAL(5,2) COMMENT '成团成功率预测',
  `match_score` DECIMAL(5,2) COMMENT '匹配度分数',
  `tags` VARCHAR(500) COMMENT '标签 JSON',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `closed_at` DATETIME COMMENT '截止时间',
  `completed_at` DATETIME COMMENT '完成时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`goods_id`) REFERENCES `goods`(`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_deadline` (`deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼车表';

CREATE TABLE IF NOT EXISTS `car_member` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '成员ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `pref_versions` VARCHAR(1000) COMMENT '偏好版本 JSON数组',
  `pref_cards` VARCHAR(1000) COMMENT '偏好小卡 JSON数组',
  `pref_priority` VARCHAR(500) COMMENT '偏好优先级 JSON',
  `phone` VARCHAR(20) COMMENT '联系电话',
  `address` VARCHAR(500) COMMENT '收货地址',
  `claim_status` TINYINT DEFAULT 0 COMMENT '认领状态 0未认领 1已认领 2已分配',
  `claimed_version` VARCHAR(100) COMMENT '已分配版本',
  `claimed_card` VARCHAR(100) COMMENT '已分配小卡',
  `match_score` DECIMAL(5,2) COMMENT '匹配度分数',
  `amount` DECIMAL(10,2) COMMENT '应付金额',
  `deposit_paid` DECIMAL(10,2) DEFAULT 0 COMMENT '已付定金',
  `balance_paid` DECIMAL(10,2) DEFAULT 0 COMMENT '已付尾款',
  `evidence_url` VARCHAR(500) COMMENT '付款凭证URL',
  `evidence_status` TINYINT DEFAULT 0 COMMENT '凭证状态 0待审 1通过 2拒绝',
  `evidence_reject_reason` VARCHAR(255) COMMENT '拒绝原因',
  `evidence_id` INT DEFAULT NULL COMMENT '最新凭证ID',
  `pay_status` TINYINT DEFAULT 0 COMMENT '付款状态 0未付 1定金已付 2全款已付',
  `distribution_status` TINYINT DEFAULT 0 COMMENT '分配状态 0未分配 1已分配 2已确认',
  `is_owner` TINYINT DEFAULT 0 COMMENT '是否车主 0否 1是',
  `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`car_id`) REFERENCES `car`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  INDEX `idx_car_id` (`car_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_pay_status` (`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼车成员表';

CREATE TABLE IF NOT EXISTS `evidence` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '凭证ID',
  `car_member_id` INT NOT NULL COMMENT '拼车成员ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `user_id` INT NOT NULL COMMENT '上传用户ID',
  `type` TINYINT DEFAULT 0 COMMENT '凭证类型 0-付款凭证 1-发货凭证',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片地址',
  `remark` VARCHAR(500) COMMENT '备注',
  `status` TINYINT DEFAULT 0 COMMENT '审核状态 0-待审核 1-已通过 2-已驳回',
  `reviewed_by` INT DEFAULT NULL COMMENT '审核人ID',
  `reviewed_at` DATETIME COMMENT '审核时间',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_car_id` (`car_id`),
  INDEX `idx_car_member_id` (`car_member_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证表';

CREATE TABLE IF NOT EXISTS `logistics` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '物流ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `express_no` VARCHAR(100) DEFAULT '' COMMENT '快递单号',
  `express_company` VARCHAR(100) DEFAULT '' COMMENT '快递公司名称',
  `express_company_code` VARCHAR(50) DEFAULT '' COMMENT '快递公司编码',
  `status` TINYINT DEFAULT 0 COMMENT '物流状态 0-待发货 1-已发货 2-运输中 3-已到达 4-已签收 5-异常',
  `sender_name` VARCHAR(50) DEFAULT '' COMMENT '发件人姓名',
  `sender_phone` VARCHAR(20) DEFAULT '' COMMENT '发件人电话',
  `sender_address` VARCHAR(500) DEFAULT '' COMMENT '发件人地址',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收件人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收件人电话',
  `receiver_address` VARCHAR(500) NOT NULL COMMENT '收件人地址',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `nodes` TEXT COMMENT '物流节点(JSON数组)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_car_id` (`car_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流表';

CREATE TABLE IF NOT EXISTS `car_order` like `order`;
-- `order` 已由 RuoYi 框架创建，这里使用 car_order 表名
DROP TABLE IF EXISTS `car_order`;
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

CREATE TABLE IF NOT EXISTS `review` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `from_user_id` INT NOT NULL COMMENT '评价人ID',
  `to_user_id` INT NOT NULL COMMENT '被评价人ID',
  `type` TINYINT NOT NULL COMMENT '类型 0评价成员 1评价车主',
  `rating` TINYINT NOT NULL COMMENT '评分 1-5',
  `content` VARCHAR(500) COMMENT '评价内容',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`car_id`) REFERENCES `car`(`id`),
  FOREIGN KEY (`from_user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`to_user_id`) REFERENCES `user`(`id`),
  INDEX `idx_car_id` (`car_id`),
  INDEX `idx_from_user_id` (`from_user_id`),
  INDEX `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

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

CREATE TABLE IF NOT EXISTS `car_favorite` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_car_user` (`car_id`, `user_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼车收藏表';

CREATE TABLE IF NOT EXISTS `car_template` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `goods_name` VARCHAR(100) COMMENT '商品名称',
  `goods_image` VARCHAR(255) COMMENT '商品图片',
  `description` TEXT COMMENT '拼车说明',
  `total_count` INT NOT NULL COMMENT '总人数',
  `price_total` DECIMAL(10,2) COMMENT '总价',
  `price_per` DECIMAL(10,2) COMMENT '人均价格',
  `distribution_type` TINYINT DEFAULT 0 COMMENT '分配方式',
  `is_restricted` TINYINT DEFAULT 0 COMMENT '是否限制信用分',
  `min_credit_score` INT DEFAULT 0 COMMENT '最低信用分要求',
  `versions` VARCHAR(2000) COMMENT '版本信息',
  `cards` VARCHAR(2000) COMMENT '小卡信息',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼车模板表';

CREATE TABLE IF NOT EXISTS `blacklist` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '黑名单ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `blocked_user_id` INT NOT NULL COMMENT '被拉黑用户ID',
  `reason` VARCHAR(255) COMMENT '拉黑原因',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_block` (`user_id`, `blocked_user_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单表';

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

-- 3. 插入测试数据
INSERT IGNORE INTO `user` (`id`, `openid`, `nickname`, `phone`, `credit_score`, `credit_level`, `role`, `status`, `total_transactions`, `success_transactions`) VALUES
(1, 'wx_dev_user_1', '追星达人', '13800138001', 92, 5, 1, 1, 28, 28),
(2, 'wx_dev_user_2', '小确幸', '13800138002', 85, 4, 0, 1, 15, 14),
(3, 'wx_dev_user_3', '快乐星球', '13800138003', 78, 4, 0, 1, 8, 7),
(4, 'wx_dev_user_4', '管理员', '13800138004', 100, 5, 2, 1, 0, 0);

INSERT IGNORE INTO `goods` (`id`, `name`, `type`, `versions`, `cards`, `market_price`, `status`) VALUES
(1, '2024 冬季特别专辑', '专辑', '["A版","B版","C版","D版"]', '["成员A小卡","成员B小卡","成员C小卡","成员D小卡","成员E小卡"]', 158.00, 1),
(2, '夏日写真集', '写真', '["标准版","豪华版"]', '["单人写真卡x5","团体写真卡x3"]', 268.00, 1);

INSERT IGNORE INTO `car` (`id`, `user_id`, `title`, `goods_id`, `goods_name`, `description`, `total_count`, `current_count`, `price_total`, `price_per`, `deposit_amount`, `deadline`, `distribution_type`, `status`, `success_rate`) VALUES
(1, 1, '【包邮】2024冬季专辑拼车', 1, '2024 冬季特别专辑', '全新未拆封，包邮到家，定金20元，到货后补尾款。支持自选版本和小卡偏好。', 5, 3, 790.00, 158.00, 20.00, DATE_ADD(NOW(), INTERVAL 7 DAY), 1, 0, 85.50),
(2, 1, '夏日写真集豪华版拼车', 2, '夏日写真集', '豪华版写真集，限量发售，先到先得。', 4, 2, 1072.00, 268.00, 50.00, DATE_ADD(NOW(), INTERVAL 5 DAY), 0, 0, 72.30);

INSERT IGNORE INTO `car_member` (`car_id`, `user_id`, `pref_versions`, `pref_cards`, `claim_status`, `amount`, `deposit_paid`, `pay_status`, `is_owner`) VALUES
(1, 1, '["A版"]', '["成员A小卡"]', 2, 158.00, 20.00, 1, 1),
(1, 2, '["B版"]', '["成员B小卡"]', 1, 158.00, 20.00, 1, 0),
(1, 3, '["C版"]', '["成员C小卡"]', 1, 158.00, 0.00, 0, 0),
(2, 1, '["豪华版"]', '["成员A小卡"]', 2, 268.00, 50.00, 1, 1),
(2, 2, '["豪华版"]', '["成员B小卡"]', 1, 268.00, 50.00, 1, 0);

SELECT CONCAT('数据库初始化完成，已创建 ', COUNT(*), ' 张业务表') as result FROM information_schema.tables WHERE table_schema = 'car_share_db';
