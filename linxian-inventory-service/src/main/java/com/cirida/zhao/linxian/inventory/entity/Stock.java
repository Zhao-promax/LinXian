package com.cirida.zhao.linxian.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cirida.zhao.linxian.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存表。available/locked 由 Redis 与 DB 双写，version 记录并发修改次数（乐观锁）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock")
public class Stock extends BaseEntity {

    private Long skuId;

    /** 总库存 */
    private Integer total;

    /** 可用库存 */
    private Integer available;

    /** 预扣锁定库存 */
    private Integer locked;

    /** 乐观锁版本 */
    private Long version;
}
