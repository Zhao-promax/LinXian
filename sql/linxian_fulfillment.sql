CREATE DATABASE IF NOT EXISTS linxian_fulfillment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE linxian_fulfillment;

-- 分拣任务表（按自提点聚合）
CREATE TABLE IF NOT EXISTS `fulfillment_task` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `task_no`         VARCHAR(64) NOT NULL,
    `pickup_point_id` BIGINT      NOT NULL,
    `order_count`     INT         NOT NULL DEFAULT 0,
    `status`          TINYINT     NOT NULL DEFAULT 0 COMMENT '0 待分拣 1 分拣中 2 已完成',
    `created_at`      DATETIME    DEFAULT NULL,
    `updated_at`      DATETIME    DEFAULT NULL,
    `deleted`         TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_no` (`task_no`)
) ENGINE = InnoDB COMMENT = '分拣任务表';

-- 分拣任务明细表
CREATE TABLE IF NOT EXISTS `fulfillment_task_item` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `task_id`    BIGINT      NOT NULL,
    `order_id`   BIGINT      NOT NULL,
    `order_no`   VARCHAR(64) NOT NULL,
    `sku_id`     BIGINT      NOT NULL,
    `quantity`   INT         NOT NULL,
    `status`     TINYINT     NOT NULL DEFAULT 0 COMMENT '0 待分拣 1 已分拣',
    `created_at` DATETIME    DEFAULT NULL,
    `updated_at` DATETIME    DEFAULT NULL,
    `deleted`    TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`)
) ENGINE = InnoDB COMMENT = '分拣任务明细表';

-- 履约异常表（同步 Elasticsearch）
CREATE TABLE IF NOT EXISTS `fulfillment_exception` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `order_id`        BIGINT       DEFAULT NULL,
    `order_no`        VARCHAR(64)  DEFAULT NULL,
    `sku_id`          BIGINT       DEFAULT NULL,
    `leader_id`       BIGINT       DEFAULT NULL,
    `pickup_point_id` BIGINT       DEFAULT NULL,
    `exception_type`  VARCHAR(64)  DEFAULT NULL,
    `description`     VARCHAR(255) DEFAULT NULL,
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '0 待处理 1 已补偿',
    `retry_count`     INT          NOT NULL DEFAULT 0,
    `next_retry_time` DATETIME     DEFAULT NULL,
    `created_at`      DATETIME     DEFAULT NULL,
    `updated_at`      DATETIME     DEFAULT NULL,
    `deleted`         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_leader_id` (`leader_id`),
    KEY `idx_order_no` (`order_no`)
) ENGINE = InnoDB COMMENT = '履约异常表';
