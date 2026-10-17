CREATE DATABASE IF NOT EXISTS linxian_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE linxian_user;

-- 消费者用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`   VARCHAR(64)  NOT NULL COMMENT '用户名',
    `password`   VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码',
    `phone`      VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `nickname`   VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 启用 0 禁用',
    `created_at` DATETIME     DEFAULT NULL,
    `updated_at` DATETIME     DEFAULT NULL,
    `deleted`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB COMMENT = '消费者用户表';
