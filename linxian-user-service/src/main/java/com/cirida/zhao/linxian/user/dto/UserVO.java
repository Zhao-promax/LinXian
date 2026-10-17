package com.cirida.zhao.linxian.user.dto;

import lombok.Data;

/**
 * 用户视图对象，登录成功时附带 token。
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String phone;

    private String nickname;

    private Integer status;

    /** 仅登录成功时返回 */
    private String token;
}
