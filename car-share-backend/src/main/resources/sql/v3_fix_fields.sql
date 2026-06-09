ALTER TABLE `logistics` ADD COLUMN `express_company_code` varchar(50) DEFAULT NULL COMMENT '快递公司编码' AFTER `express_company`;
ALTER TABLE `logistics` ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `receiver_address`;

ALTER TABLE `car_member` ADD COLUMN `phone` varchar(20) DEFAULT NULL COMMENT '联系电话' AFTER `pref_priority`;
ALTER TABLE `car_member` ADD COLUMN `address` varchar(500) DEFAULT NULL COMMENT '收货地址' AFTER `phone`;

ALTER TABLE `user` ADD COLUMN `session_key` varchar(200) DEFAULT NULL COMMENT '微信session_key' AFTER `unionid`;
