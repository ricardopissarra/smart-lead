package com.rpissarra.smartleadqualification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@SpringBootApplication
public class SmartLeadQualificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartLeadQualificationApplication.class, args);
    }

}
