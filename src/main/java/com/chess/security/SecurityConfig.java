package com.chess.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration//Marks the class as a source of bean definitions for the application context.
@EnableWebSecurity//Enables web security configuration.

/*@Bean: Indicates that a method produces a bean that should be managed by the Spring container.
 In this case, it creates a PasswordEncoder and a SecurityFilterChain. */
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       return http
        .formLogin(httpForm -> {
            httpForm
                .loginPage("/login")//Tells Spring where to redirect for login
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/index", true)//Tells Spring where to redirect after successful login
                .failureUrl("/login?error=true")
                .permitAll();//Allows all users to access the login page
        })
        .rememberMe(remember -> {
            remember
                .key("uniqueAndSecretKey") // Change this to a secure random key in production
                .tokenValiditySeconds(86400) // 24 hours
                .rememberMeParameter("remember") // matches the checkbox name in your form
                .rememberMeCookieName("remember-me-cookie");
        })
        .logout(logout -> {
            logout
                .logoutSuccessUrl("/") // Redirect to home page with logout parameter
                .deleteCookies("remember-me-cookie") // Delete remember-me cookie on logout
                .permitAll();
        })
        .authorizeHttpRequests(registry -> {
            registry.requestMatchers(
                "/",           // Allow access to root path
                "/signup",
                "/api/users/signup",  // Add this line to allow access to signup API
                "/login", 
                "/game/**",  // Allow access to game URLs
                "/css/**",   // Allow access to CSS files
                "/js/**",    // Allow access to JS files
                "/images/**", 
                "/error",
                "/api/games/**",  // Allow access to games API endpoints
                "/index",
                "/about"     // Allow access to index page without authentication
            ).permitAll();
            registry.anyRequest().authenticated();
        })

        .csrf(csrf -> csrf.disable())  // Configures Cross-Site Request Forgery protection. Disabling CSRF is generally not recommended for production environments.
        .build();
    }
}
