package com.chess.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.chess.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // Spring Boot's auto-configuration will handle the DataSource and EntityManagerFactory
    // based on the application.properties/yml configuration
}
