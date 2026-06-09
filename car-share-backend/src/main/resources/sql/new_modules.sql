-- =============================================
-- 拼车协作与可信结算系统 - 新增模块建表SQL
-- =============================================

-- 1. 退款表
CREATE TABLE IF NOT EXISTS `refund` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `car_id` bigint NOT NULL COMMENT '拼车编号',
  `car_member_id` bigint NOT NULL COMMENT '拼车成员编号',
  `user_id` bigint NOT NULL COMMENT '申请退款用户编号',
  `amount` decimal(10,2) NOT NULL COMMENT '退款金额',
  `type` tinyint NOT NULL COMMENT '退款类型：1-主动退出 2-拼车失败自动退款',
  `reason` varchar(500) DEFAULT NULL COMMENT '退款原因',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '退款状态：0-申请中 1-审核通过 2-退款中 3-已到账 4-已驳回',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '驳回原因',
  `reviewed_by` bigint DEFAULT NULL COMMENT '审核人编号',
  `reviewed_at` datetime DEFAULT NULL COMMENT '审核时间',
  `refunded_at` datetime DEFAULT NULL COMMENT '退款到账时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_refund_car` (`car_id`),
  KEY `idx_refund_user` (`user_id`),
  KEY `idx_refund_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款表';

-- 2. 拼车分享记录表
CREATE TABLE IF NOT EXISTS `car_share_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `car_id` bigint NOT NULL COMMENT '拼车编号',
  `user_id` bigint NOT NULL COMMENT '分享用户编号',
  `share_code` varchar(16) NOT NULL COMMENT '分享码/口令',
  `share_type` varchar(20) DEFAULT 'friend' COMMENT '分享类型：friend-好友 poster-海报 code-口令',
  `invite_user_id` bigint DEFAULT NULL COMMENT '通过分享加入的用户编号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_share_code` (`share_code`),
  KEY `idx_share_car` (`car_id`),
  KEY `idx_share_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拼车分享记录表';

-- 3. 用户地址表
CREATE TABLE IF NOT EXISTS `user_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户编号',
  `name` varchar(50) NOT NULL COMMENT '收件人姓名',
  `phone` varchar(20) NOT NULL COMMENT '收件人电话',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `detail` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认地址：0-否 1-是',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已删除 1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_address_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户地址表';

-- 4. 拼车收藏表
CREATE TABLE IF NOT EXISTS `car_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `car_id` bigint NOT NULL COMMENT '拼车编号',
  `user_id` bigint NOT NULL COMMENT '用户编号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite` (`car_id`, `user_id`),
  KEY `idx_favorite_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拼车收藏表';

-- 5. 拼车模板表
CREATE TABLE IF NOT EXISTS `car_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户编号',
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `goods_name` varchar(100) DEFAULT NULL COMMENT '商品名称',
  `goods_image` varchar(500) DEFAULT NULL COMMENT '商品图片',
  `description` text DEFAULT NULL COMMENT '描述',
  `total_count` int DEFAULT NULL COMMENT '总份数',
  `price_total` decimal(10,2) DEFAULT NULL COMMENT '总价',
  `price_per` decimal(10,2) DEFAULT NULL COMMENT '单价',
  `distribution_type` tinyint DEFAULT NULL COMMENT '分配方式',
  `is_restricted` tinyint DEFAULT NULL COMMENT '是否限制信用',
  `min_credit_score` int DEFAULT NULL COMMENT '最低信用分',
  `versions` varchar(2000) DEFAULT NULL COMMENT '版本列表',
  `cards` varchar(2000) DEFAULT NULL COMMENT '小卡种类',
  `is_top` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_template_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拼车模板表';

-- 6. 黑名单表
CREATE TABLE IF NOT EXISTS `blacklist` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户编号',
  `blocked_user_id` bigint NOT NULL COMMENT '被拉黑用户编号',
  `reason` varchar(500) DEFAULT NULL COMMENT '拉黑原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blacklist` (`user_id`, `blocked_user_id`),
  KEY `idx_blacklist_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='黑名单表';

-- 7. 举报表
CREATE TABLE IF NOT EXISTS `report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '举报人编号',
  `target_user_id` bigint DEFAULT NULL COMMENT '被举报用户编号',
  `car_id` bigint DEFAULT NULL COMMENT '关联拼车编号',
  `type` tinyint NOT NULL COMMENT '举报类型：1-虚假凭证 2-恶意行为 3-违规内容 4-其他',
  `description` varchar(500) NOT NULL COMMENT '举报描述',
  `image_url` varchar(500) DEFAULT NULL COMMENT '举报截图',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待处理 1-已处理 2-已驳回',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `handled_by` bigint DEFAULT NULL COMMENT '处理人编号',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_report_user` (`user_id`),
  KEY `idx_report_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='举报表';

-- 8. 费用明细表
CREATE TABLE IF NOT EXISTS `fee_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `car_id` bigint NOT NULL COMMENT '拼车编号',
  `car_member_id` bigint NOT NULL COMMENT '拼车成员编号',
  `user_id` bigint NOT NULL COMMENT '用户编号',
  `goods_amount` decimal(10,2) NOT NULL COMMENT '商品金额',
  `shipping_fee` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费分摊',
  `total_amount` decimal(10,2) NOT NULL COMMENT '应付总额',
  `shipping_fee_type` tinyint NOT NULL DEFAULT 0 COMMENT '运费分摊方式：0-均摊 1-按份数比例 2-车主承担',
  `deposit_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '定金',
  `balance_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '尾款',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fee_member` (`car_id`, `car_member_id`),
  KEY `idx_fee_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='费用明细表';

-- 9. 拼车评论表
CREATE TABLE IF NOT EXISTS `car_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `car_id` bigint NOT NULL COMMENT '拼车编号',
  `user_id` bigint NOT NULL COMMENT '用户编号',
  `type` tinyint NOT NULL DEFAULT 0 COMMENT '类型：0-评论 1-动态 2-晒单',
  `content` varchar(500) NOT NULL COMMENT '内容',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图片URL',
  `reply_to_id` bigint DEFAULT NULL COMMENT '回复的评论编号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_car` (`car_id`),
  KEY `idx_comment_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拼车评论表';
