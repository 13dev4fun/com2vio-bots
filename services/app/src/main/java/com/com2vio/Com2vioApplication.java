package com.com2vio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableJpaRepositories(basePackages = {
    "com.com2vio.repositories"
})
@EntityScan(basePackages = {
    "com.com2vio.entities"
})
@EnableKafka
public class Com2vioApplication {

    public static void main(String[] args) {
        SpringApplication.run(Com2vioApplication.class, args);
    }

}
