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
    CommandLineRunner seedInstructions(InstructionService service) {
        return args -> {
            if (service.findAll().isEmpty()) {
                log.info("Seeding initial instructions");
                service.create("Как запустить сервис", "Подключитесь к API и выполните запросы для управления инструкциями.");
                service.create("Проверка состояния", "Вызовите /actuator/health чтобы убедиться, что сервис жив.");
            } else {
                log.info("Instructions already present, skipping seed");
            }
        };
    }
}
