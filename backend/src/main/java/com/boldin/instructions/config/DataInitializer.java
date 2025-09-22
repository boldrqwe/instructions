package com.boldin.instructions.config;

import com.boldin.instructions.service.InstructionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);


    @Bean
    CommandLineRunner logInstructionCount(InstructionService service) {
        return args -> log.info("Application started with {} instruction(s) in storage", service.findAll().size());
    }
}
