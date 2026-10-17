package com.cirida.zhao.linxian.fulfillment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.cirida.zhao.linxian")
@MapperScan("com.cirida.zhao.linxian.fulfillment.mapper")
@EnableFeignClients(basePackages = "com.cirida.zhao.linxian.api")
@EnableScheduling
public class FulfillmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(FulfillmentApplication.class, args);
    }
}
