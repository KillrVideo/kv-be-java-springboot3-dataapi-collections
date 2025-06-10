package com.killrvideo;

import com.killrvideo.service.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class KillrVideoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KillrVideoApplication.class, args);
    }
} 