CREATE DATABASE IF NOT EXISTS linxian_leader DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE linxian_leader;

-- 团长表
CREATE TABLE IF NOT EXISTS `leader` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(64) NOT NULL COMMENT '团长姓名',
    `phone`      VARCHAR(20) DEFAULT NULL,
    `status`     TINYINT     NOT NULL DEFAULT 1 COMMENT '1 启用 0 禁用',
    `created_at` DATETIME    DEFAULT NULL,
    `updated_at` DATETIME    DEFAULT NULL,
    `deleted`    TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '团长表';

-- 自提点表
CREATE TABLE IF NOT EXISTS `pickup_point` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `leader_id`  BIGINT       NOT NULL COMMENT '所属团长',
    `name`       VARCHAR(128) NOT NULL COMMENT '自提点名称',
    `address`    VARCHAR(255) DEFAULT NULL,
    `phone`      VARCHAR(20)  DEFAULT NULL,
    `status`     TINYINT      NOT NULL DEFAULT 1,
    `created_at` DATETIME     DEFAULT NULL,
    `updated_at` DATETIME     DEFAULT NULL,
    `deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_leader_id` (`leader_id`)
) ENGINE = InnoDB COMMENT = '自提点表';

-- 演示数据
INSERT INTO `leader` (`id`, `name`, `phone`, `status`) VALUES
    (1, '王团长', '13800000001', 1),
    (2, '李团长', '13800000002', 1);

INSERT INTO `pickup_point` (`id`, `leader_id`, `name`, `address`, `phone`, `status`) VALUES
    (1, 1, '幸福小区自提点', '幸福小区 1 栋 101', '13800000001', 1),
    (2, 2, '阳光花园自提点', '阳光花园北门便利店', '13800000002', 1);
