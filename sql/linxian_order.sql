CREATE DATABASE IF NOT EXISTS linxian_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE linxian_order;

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `order_no`        VARCHAR(64)   NOT NULL,
    `user_id`         BIGINT        NOT NULL,
    `pickup_point_id` BIGINT        NOT NULL,
    `leader_id`       BIGINT        NOT NULL,
    `total_amount`    DECIMAL(10,2) NOT NULL,
    `status`          TINYINT       NOT NULL DEFAULT 0 COMMENT '0 待支付 1 已支付 2 分拣中 3 待自提 4 已自提 5 已取消',
    `payment_time`    DATETIME      DEFAULT NULL,
    `cancel_time`     DATETIME      DEFAULT NULL,
    `created_at`      DATETIME      DEFAULT NULL,
    `updated_at`      DATETIME      DEFAULT NULL,
    `deleted`         TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB COMMENT = '订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS `order_item` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `order_id`     BIGINT        NOT NULL,
    `sku_id`       BIGINT        NOT NULL,
    `product_name` VARCHAR(128)  DEFAULT NULL,
    `sku_spec`     VARCHAR(128)  DEFAULT NULL,
    `quantity`     INT           NOT NULL,
    `price`        DECIMAL(10,2) NOT NULL,
    `created_at`   DATETIME      DEFAULT NULL,
    `updated_at`   DATETIME      DEFAULT NULL,
    `deleted`      TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB COMMENT = '订单明细表';
