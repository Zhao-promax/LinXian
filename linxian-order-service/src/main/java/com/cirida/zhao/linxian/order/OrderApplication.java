package com.cirida.zhao.linxian.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.cirida.zhao.linxian")
@MapperScan("com.cirida.zhao.linxian.order.mapper")
@EnableFeignClients(basePackages = "com.cirida.zhao.linxian.api")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
