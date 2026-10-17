package com.cirida.zhao.linxian.api.leader;

import com.cirida.zhao.linxian.api.leader.dto.PickupPointDTO;
import com.cirida.zhao.linxian.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 团长服务 Feign 客户端（供订单服务校验自提点）。
 */
@FeignClient(name = "linxian-leader-service")
public interface LeaderClient {

    @GetMapping("/leader/pickup-points/{id}")
    Result<PickupPointDTO> getPickupPoint(@PathVariable("id") Long id);
}
