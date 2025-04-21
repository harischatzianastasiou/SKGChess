package com.chess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.chess"})
public class ChessWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChessWebApplication.class, args);
    }
} 