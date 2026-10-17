package com.cirida.zhao.linxian.leader.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 团长创建/更新请求体。
 */
@Data
public class LeaderRequest {

    @NotBlank(message = "团长姓名不能为空")
    private String name;

    private String phone;

    private Integer status;
}
