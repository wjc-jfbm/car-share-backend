CREATE DATABASE IF NOT EXISTS car_share_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE car_share_db;

CREATE TABLE IF NOT EXISTS `user` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `openid` VARCHAR(64) NOT NULL UNIQUE COMMENT '微信openid',
  `unionid` VARCHAR(64) UNIQUE COMMENT '微信unionid',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `phone` VARCHAR(20) COMMENT '手机号',
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
  `disliked_versions` VARCHAR(1000) COMMENT '不喜欢的版本 JSON数组',
  `disliked_cards` VARCHAR(1000) COMMENT '不喜欢的小卡 JSON数组',
  `auto_match` TINYINT DEFAULT 1 COMMENT '自动匹配开关 0关闭 1开启',
  `match_radius` INT DEFAULT 50 COMMENT '匹配推荐数量',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好设置表';

CREATE TABLE IF NOT EXISTS `goods` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `type` VARCHAR(50) COMMENT '类型：专辑/周边/写真等',
  `brand` VARCHAR(50) COMMENT '品牌/公司',
  `versions` VARCHAR(2000) COMMENT '可用版本 JSON数组',
  `cards` VARCHAR(2000) COMMENT '小卡信息 JSON数组',
  `artists` VARCHAR(1000) COMMENT '艺人信息 JSON数组',
  `market_price` DECIMAL(10,2) COMMENT '市场价',
  `image_url` VARCHAR(255) COMMENT '商品主图URL',
  `image_list` VARCHAR(2000) COMMENT '商品图片列表 JSON数组',
  `description` TEXT COMMENT '商品描述',
  `stock` INT DEFAULT 0 COMMENT '库存数量',
  `sales_count` INT DEFAULT 0 COMMENT '销量',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0下架 1上架',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_name` (`name`),
  INDEX `idx_type` (`type`),
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
  `status` TINYINT DEFAULT 0 COMMENT '状态 0招募中 1已截止 2成团中 3已完成 4已取消 5失败',
  `success_rate` DECIMAL(5,2) COMMENT '成团成功率预测 0-100',
  `match_score` DECIMAL(5,2) COMMENT '匹配度分数',
  `tags` VARCHAR(500) COMMENT '标签 JSON数组',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `closed_at` DATETIME COMMENT '截止时间',
  `completed_at` DATETIME COMMENT '完成时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`goods_id`) REFERENCES `goods`(`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_deadline` (`deadline`),
  INDEX `idx_success_rate` (`success_rate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼车表';

CREATE TABLE IF NOT EXISTS `car_member` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '成员ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `pref_versions` VARCHAR(1000) COMMENT '偏好版本 JSON数组',
  `pref_cards` VARCHAR(1000) COMMENT '偏好小卡 JSON数组',
  `pref_priority` VARCHAR(500) COMMENT '偏好优先级 JSON',
  `claim_status` TINYINT DEFAULT 0 COMMENT '认领状态 0未认领 1已认领 2已分配',
  `claimed_version` VARCHAR(100) COMMENT '已分配版本',
  `claimed_card` VARCHAR(100) COMMENT '已分配小卡',
  `match_score` DECIMAL(5,2) COMMENT '匹配度分数',
  `amount` DECIMAL(10,2) COMMENT '应付金额',
  `deposit_paid` DECIMAL(10,2) DEFAULT 0 COMMENT '已付定金',
  `balance_paid` DECIMAL(10,2) DEFAULT 0 COMMENT '已付尾款',
  `evidence_url` VARCHAR(255) COMMENT '付款凭证URL',
  `evidence_status` TINYINT DEFAULT 0 COMMENT '凭证状态 0待审 1通过 2拒绝',
  `evidence_reject_reason` VARCHAR(255) COMMENT '拒绝原因',
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
  `user_id` INT NOT NULL COMMENT '用户ID',
  `type` TINYINT NOT NULL COMMENT '类型 0付款凭证 1收货凭证 2其他',
  `image_url` VARCHAR(255) NOT NULL COMMENT '凭证图片URL',
  `image_list` VARCHAR(2000) COMMENT '凭证图片列表 JSON数组',
  `amount` DECIMAL(10,2) COMMENT '金额',
  `remark` VARCHAR(500) COMMENT '备注',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0待审 1通过 2拒绝',
  `reviewed_by` INT COMMENT '审核人ID',
  `reviewed_at` DATETIME COMMENT '审核时间',
  `review_remark` VARCHAR(255) COMMENT '审核备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`car_member_id`) REFERENCES `car_member`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  INDEX `idx_car_member_id` (`car_member_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证表';

CREATE TABLE IF NOT EXISTS `logistics` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '物流ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `express_no` VARCHAR(50) COMMENT '快递单号',
  `express_company` VARCHAR(50) COMMENT '快递公司',
  `express_company_code` VARCHAR(20) COMMENT '快递公司编码',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0待发货 1已发货 2运输中 3已到达 4已签收 5异常',
  `sender_name` VARCHAR(50) COMMENT '发货人',
  `sender_phone` VARCHAR(20) COMMENT '发货人电话',
  `sender_address` VARCHAR(255) COMMENT '发货地址',
  `receiver_name` VARCHAR(50) COMMENT '收货人',
  `receiver_phone` VARCHAR(20) COMMENT '收货人电话',
  `receiver_address` VARCHAR(255) COMMENT '收货地址',
  `nodes` TEXT COMMENT '物流节点 JSON数组',
  `tracking_time` DATETIME COMMENT '最新追踪时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`car_id`) REFERENCES `car`(`id`),
  INDEX `idx_car_id` (`car_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流表';

CREATE TABLE IF NOT EXISTS `order` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `car_member_id` INT COMMENT '拼车成员ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `deposit_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '定金金额',
  `balance_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '尾款金额',
  `type` TINYINT DEFAULT 0 COMMENT '类型 0定金 1尾款 2全款',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0待确认 1已确认 2已支付 3已完成 4已取消',
  `settle_status` TINYINT DEFAULT 0 COMMENT '结算状态 0未结算 1已结算',
  `pay_time` DATETIME COMMENT '支付时间',
  `settle_time` DATETIME COMMENT '结算时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`car_id`) REFERENCES `car`(`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  INDEX `idx_car_id` (`car_id`),
  INDEX `idx_order_no` (`order_no`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `review` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `from_user_id` INT NOT NULL COMMENT '评价人ID',
  `to_user_id` INT NOT NULL COMMENT '被评价人ID',
  `type` TINYINT NOT NULL COMMENT '类型 0评价成员 1评价车主',
  `rating` TINYINT NOT NULL COMMENT '评分 1-5',
  `content` VARCHAR(500) COMMENT '评价内容',
  `images` VARCHAR(2000) COMMENT '评价图片 JSON数组',
  `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名 0否 1是',
  `reply_content` VARCHAR(500) COMMENT '回复内容',
  `reply_at` DATETIME COMMENT '回复时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`car_id`) REFERENCES `car`(`id`),
  FOREIGN KEY (`from_user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`to_user_id`) REFERENCES `user`(`id`),
  INDEX `idx_car_id` (`car_id`),
  INDEX `idx_from_user_id` (`from_user_id`),
  INDEX `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

CREATE TABLE IF NOT EXISTS `match_record` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '匹配记录ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `car_id` INT NOT NULL COMMENT '拼车ID',
  `version_match` DECIMAL(5,2) COMMENT '版本匹配度 0-100',
  `card_match` DECIMAL(5,2) COMMENT '小卡匹配度 0-100',
  `artist_match` DECIMAL(5,2) COMMENT '艺人匹配度 0-100',
  `total_score` DECIMAL(5,2) COMMENT '总匹配分数',
  `status` TINYINT DEFAULT 0 COMMENT '状态 0推荐中 1已参与 2已忽略',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`car_id`) REFERENCES `car`(`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_car_id` (`car_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匹配记录表';

CREATE TABLE IF NOT EXISTS `system_config` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `config_type` VARCHAR(20) DEFAULT 'string' COMMENT '配置类型 string/json/number',
  `description` VARCHAR(255) COMMENT '配置描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  `user_id` INT COMMENT '操作用户ID',
  `operation_type` VARCHAR(50) COMMENT '操作类型',
  `module` VARCHAR(50) COMMENT '操作模块',
  `target_id` INT COMMENT '操作目标ID',
  `content` VARCHAR(500) COMMENT '操作内容',
  `ip` VARCHAR(50) COMMENT '操作IP',
  `user_agent` VARCHAR(500) COMMENT '用户代理',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_operation_type` (`operation_type`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

INSERT INTO `goods` (`name`, `type`, `brand`, `versions`, `cards`, `artists`, `market_price`, `description`) VALUES
('2024 冬季特别专辑', '专辑', 'Star Entertainment', '["A版","B版","C版","D版"]', '["成员A小卡","成员B小卡","成员C小卡","成员D小卡","成员E小卡"]', '["成员A","成员B","成员C","成员D","成员E"]', 158.00, '2024年冬季特别专辑，包含主打歌及5首收录曲，附赠随机小卡一张'),
('夏日写真集', '写真', 'Star Entertainment', '["标准版","豪华版"]', '["单人写真卡x5","团体写真卡x3"]', '["成员A","成员B","成员C","成员D","成员E"]', 268.00, '夏日限定写真集，包含成员个人及团体精美照片');

INSERT INTO `user` (`openid`, `unionid`, `nickname`, `phone`, `credit_score`, `credit_level`, `role`, `status`, `total_transactions`, `success_transactions`) VALUES
('wx_openid_001', 'wx_unionid_001', '追星达人', '13800138001', 92, 5, 1, 1, 28, 28),
('wx_openid_002', 'wx_unionid_002', '小确幸', '13800138002', 85, 4, 0, 1, 15, 14),
('wx_openid_003', 'wx_unionid_003', '快乐星球', '13800138003', 78, 4, 0, 1, 8, 7),
('wx_openid_004', 'wx_unionid_004', '管理员', '13800138004', 100, 5, 2, 1, 0, 0);

INSERT INTO `user_address` (`user_id`, `name`, `phone`, `province`, `city`, `district`, `detail`, `is_default`) VALUES
(1, '张三', '13800138001', '北京市', '北京市', '朝阳区', '某某小区1号楼101室', 1),
(2, '李四', '13800138002', '上海市', '上海市', '浦东新区', '某某大厦A座2001室', 1);

INSERT INTO `user_preference` (`user_id`, `preferred_versions`, `preferred_cards`, `preferred_artists`, `auto_match`) VALUES
(2, '["A版","B版"]', '["成员A小卡","成员B小卡"]', '["成员A","成员B"]', 1),
(3, '["C版"]', '["成员C小卡"]', '["成员C"]', 1);

INSERT INTO `car` (`user_id`, `title`, `goods_id`, `goods_name`, `description`, `total_count`, `current_count`, `price_total`, `price_per`, `deposit_amount`, `deadline`, `distribution_type`, `status`, `success_rate`) VALUES
(1, '【包邮】2024冬季专辑拼车', 1, '2024 冬季特别专辑', '全新未拆封，包邮到家，定金20元，到货后补尾款。支持自选版本和小卡偏好。', 5, 3, 790.00, 158.00, 20.00, DATE_ADD(NOW(), INTERVAL 7 DAY), 1, 0, 85.5),
(1, '夏日写真集豪华版拼车', 2, '夏日写真集', '豪华版写真集，限量发售，先到先得。', 4, 2, 1072.00, 268.00, 50.00, DATE_ADD(NOW(), INTERVAL 5 DAY), 0, 0, 72.3);

INSERT INTO `car_member` (`car_id`, `user_id`, `pref_versions`, `pref_cards`, `claim_status`, `amount`, `deposit_paid`, `pay_status`, `is_owner`) VALUES
(1, 1, '["A版"]', '["成员A小卡"]', 2, 158.00, 20.00, 1, 1),
(1, 2, '["B版"]', '["成员B小卡"]', 1, 158.00, 20.00, 1, 0),
(1, 3, '["C版"]', '["成员C小卡"]', 1, 158.00, 0.00, 0, 0),
(2, 1, '["豪华版"]', '["成员A小卡"]', 2, 268.00, 50.00, 1, 1),
(2, 2, '["豪华版"]', '["成员B小卡"]', 1, 268.00, 50.00, 1, 0);

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('credit.default_score', '60', 'number', '新用户默认信用分'),
('credit.max_score', '100', 'number', '最高信用分'),
('car.min_credit', '50', 'number', '参与拼车最低信用分'),
('car.max_member', '50', 'number', '拼车最大人数'),
('deposit.rate', '0.2', 'number', '定金比例'),
('success.rate.formula', '{"history_rate":0.3,"current_rate":0.3,"price_compete":0.2,"credit_score":0.2}', 'json', '成团成功率计算公式'),
('match.formula', '{"version_pref":0.4,"card_pref":0.3,"artist_pref":0.3}', 'json', '匹配度计算公式');

SELECT '数据库初始化完成' as result;
