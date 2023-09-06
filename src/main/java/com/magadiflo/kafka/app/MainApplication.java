package com.magadiflo.kafka.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    /**
     * Usamos inyección de dependencia vía parámetro del método.
     * Enviamos nuestro mensaje al Topic: magadiflo
     */
    @Bean
    public CommandLineRunner commandLineRunner(KafkaTemplate<String, String> kafkaTemplate) {
        return args -> {
            for (int i = 0; i < 10; i++) {
                kafkaTemplate.send("magadiflo", i + ", hola!");
            }
        };
    }
}
