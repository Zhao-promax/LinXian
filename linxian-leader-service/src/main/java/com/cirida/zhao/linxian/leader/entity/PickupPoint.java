package com.cirida.zhao.linxian.leader.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cirida.zhao.linxian.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自提点。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pickup_point")
public class PickupPoint extends BaseEntity {

    private Long leaderId;

    private String name;

    private String address;

    private String phone;

    /** 1 启用，0 禁用 */
    private Integer status;
}
