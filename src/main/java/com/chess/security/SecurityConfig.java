package com.chess.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Configuration;

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
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll();
        })
        .logout(logout -> {
            logout
                .logoutSuccessUrl("/login?logout")
                .permitAll();
        })
        .authorizeHttpRequests(registry -> {
            registry.requestMatchers("/signup", "/css/**", "/js/**", "/images/**", "/error").permitAll();
            registry.requestMatchers("/home").authenticated();
            registry.anyRequest().authenticated();
        })
        .csrf(csrf -> csrf.disable())  // For development only
        .build();
    }
}
