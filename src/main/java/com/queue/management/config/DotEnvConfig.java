package com.queue.management.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to load environment variables from .env file.
 * This must load before Spring tries to resolve @Value annotations.
 */
@Configuration
public class DotEnvConfig {
    static {
        Dotenv dotenv = Dotenv.configure()
            .directory(".")
            .ignoreIfMissing()
            .load();
        
        // Load all variables from .env into system properties
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}
