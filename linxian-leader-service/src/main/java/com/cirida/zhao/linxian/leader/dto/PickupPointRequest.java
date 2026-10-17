package com.cirida.zhao.linxian.leader.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 自提点创建/更新请求体。
 */
@Data
public class PickupPointRequest {

    @NotNull(message = "团长ID不能为空")
    private Long leaderId;

    @NotBlank(message = "自提点名称不能为空")
    private String name;

    private String address;

    private String phone;

    private Integer status;
}
