package com.parkflow360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParkFlow360Application {

    public static void main(String[] args) {
        SpringApplication.run(ParkFlow360Application.class, args);
    }
}
