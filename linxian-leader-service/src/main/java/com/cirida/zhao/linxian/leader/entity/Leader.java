package com.cirida.zhao.linxian.leader.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cirida.zhao.linxian.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团长。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("leader")
public class Leader extends BaseEntity {

    private String name;

    private String phone;

    /** 1 启用，0 禁用 */
    private Integer status;
}
