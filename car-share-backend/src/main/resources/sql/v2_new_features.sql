CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '接收用户编号',
  `car_id` bigint DEFAULT NULL COMMENT '关联拼车编号',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` varchar(500) NOT NULL COMMENT '通知内容',
  `type` tinyint NOT NULL DEFAULT 0 COMMENT '类型:0系统通知1成员加入2拼车截止3分配完成4凭证审核5结算通知6超时提醒',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读:0未读1已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='消息通知表';

CREATE TABLE IF NOT EXISTS `statistics_cache` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `stat_type` varchar(50) NOT NULL COMMENT '统计类型',
  `stat_key` varchar(100) DEFAULT NULL COMMENT '统计维度key',
  `stat_value` int NOT NULL DEFAULT 0 COMMENT '统计值',
  `extra_data` json DEFAULT NULL COMMENT '额外数据(JSON)',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_key` (`stat_type`, `stat_key`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='统计缓存表';
