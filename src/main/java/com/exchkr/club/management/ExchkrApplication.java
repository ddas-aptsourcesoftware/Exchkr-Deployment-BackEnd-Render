package com.exchkr.club.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // <--- Critical for the cleanup service
public class ExchkrApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchkrApplication.class, args);
    }

}