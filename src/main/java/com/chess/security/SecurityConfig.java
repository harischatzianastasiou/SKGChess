package com.chess.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       return http
        .formLogin(httpForm -> {
            httpForm
                .loginPage("/req/login")
                .defaultSuccessUrl("/index", true)
                .permitAll();
        })
        .logout(logout -> {
            logout
                .logoutSuccessUrl("/req/login?logout")
                .permitAll();
        })
        .authorizeHttpRequests(registry -> {
            registry.requestMatchers("/req/signup", "/req/login", "/css/**", "/js/**", "/images/**", "/error").permitAll();
            registry.requestMatchers("/index").authenticated();
            registry.anyRequest().authenticated();
        })
        .csrf(csrf -> csrf.disable())  // For development only
        .build();
    }
}
