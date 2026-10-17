CREATE DATABASE IF NOT EXISTS linxian_inventory DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE linxian_inventory;

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(128)  NOT NULL,
    `description` VARCHAR(255)  DEFAULT NULL,
    `price`       DECIMAL(10,2) NOT NULL,
    `status`      TINYINT       NOT NULL DEFAULT 1,
    `created_at`  DATETIME      DEFAULT NULL,
    `updated_at`  DATETIME      DEFAULT NULL,
    `deleted`     TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '商品表';

-- SKU 表
CREATE TABLE IF NOT EXISTS `sku` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT,
    `product_id` BIGINT        NOT NULL,
    `spec`       VARCHAR(128)  DEFAULT NULL COMMENT '规格描述',
    `price`      DECIMAL(10,2) NOT NULL,
    `status`     TINYINT       NOT NULL DEFAULT 1,
    `created_at` DATETIME      DEFAULT NULL,
    `updated_at` DATETIME      DEFAULT NULL,
    `deleted`    TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE = InnoDB COMMENT = 'SKU 表';

-- 库存表
CREATE TABLE IF NOT EXISTS `stock` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `sku_id`     BIGINT   NOT NULL,
    `total`      INT      NOT NULL DEFAULT 0 COMMENT '总库存',
    `available`  INT      NOT NULL DEFAULT 0 COMMENT '可用库存',
    `locked`     INT      NOT NULL DEFAULT 0 COMMENT '预扣锁定库存',
    `version`    BIGINT   NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `created_at` DATETIME DEFAULT NULL,
    `updated_at` DATETIME DEFAULT NULL,
    `deleted`    TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_id` (`sku_id`)
) ENGINE = InnoDB COMMENT = '库存表';

-- 库存预扣流水表：唯一索引兜底防重复扣减
CREATE TABLE IF NOT EXISTS `stock_deduct_log` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `order_id`   BIGINT      NOT NULL,
    `order_no`   VARCHAR(64) NOT NULL,
    `sku_id`     BIGINT      NOT NULL,
    `quantity`   INT         NOT NULL,
    `type`       TINYINT     NOT NULL COMMENT '1 预扣 2 回补 3 确认',
    `created_at` DATETIME    DEFAULT NULL,
    `updated_at` DATETIME    DEFAULT NULL,
    `deleted`    TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_sku_type` (`order_id`, `sku_id`, `type`)
) ENGINE = InnoDB COMMENT = '库存预扣流水表';

-- 演示数据
INSERT INTO `product` (`id`, `name`, `description`, `price`, `status`) VALUES
    (1, '新疆阿克苏苹果', '5 斤装', 29.90, 1),
    (2, '山东红富士苹果', '10 斤装', 49.90, 1);

INSERT INTO `sku` (`id`, `product_id`, `spec`, `price`, `status`) VALUES
    (1, 1, '5斤/箱', 29.90, 1),
    (2, 2, '10斤/箱', 49.90, 1);

INSERT INTO `stock` (`id`, `sku_id`, `total`, `available`, `locked`, `version`) VALUES
    (1, 1, 1000, 1000, 0, 0),
    (2, 2, 1000, 1000, 0, 0);
