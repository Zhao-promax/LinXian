package com.cirida.zhao.linxian.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API 网关。WebFlux 应用，仅扫描自身包，避免注册 MVC 风格全局异常处理器。
 */
@SpringBootApplication(scanBasePackages = "com.cirida.zhao.linxian.gateway")
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
