package org.kruchon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Starter {
    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }
}