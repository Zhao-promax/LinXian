package com.cirida.zhao.linxian.api.leader.dto;

import lombok.Data;

/**
 * 自提点信息（供订单服务校验下单自提点）。
 */
@Data
public class PickupPointDTO {

    private Long id;
    private Long leaderId;
    private String name;
    private String address;
    private String phone;
}
