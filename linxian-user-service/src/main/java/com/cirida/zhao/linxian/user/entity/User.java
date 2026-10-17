package com.cirida.zhao.linxian.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.cirida.zhao.linxian.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消费者用户。密码为 BCrypt 密文，不参与任何接口序列化返回。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private String phone;

    private String nickname;

    /** 1 启用，0 禁用 */
    private Integer status;
}
