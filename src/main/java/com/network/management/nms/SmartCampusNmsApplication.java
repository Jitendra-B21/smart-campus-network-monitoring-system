package com.network.management.nms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling
@SpringBootApplication
public class SmartCampusNmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCampusNmsApplication.class, args);
    }

}
