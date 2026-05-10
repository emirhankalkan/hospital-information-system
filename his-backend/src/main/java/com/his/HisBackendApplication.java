package com.his;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HisBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HisBackendApplication.class, args);
    }

}
