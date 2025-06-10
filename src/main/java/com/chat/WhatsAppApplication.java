package com.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.chat.model")
@EnableJpaRepositories("com.chat.repository")
public class WhatsAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(WhatsAppApplication.class, args);
    }
} 