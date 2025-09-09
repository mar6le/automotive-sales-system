package com.automotive.sales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AutomotiveSalesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutomotiveSalesApplication.class, args);
    }
}
