package com.alieninsurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AlienInsuranceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlienInsuranceApplication.class, args);
    }
}
