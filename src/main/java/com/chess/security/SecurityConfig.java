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
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import com.chess.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration//Marks the class as a source of bean definitions for the application context.
@EnableWebSecurity//Enables web security configuration.

/*@Bean: Indicates that a method produces a bean that should be managed by the Spring container.
 In this case, it creates a PasswordEncoder and a SecurityFilterChain. */
public class SecurityConfig {

    private final UserService userService;

    @Autowired
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

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
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/index", true)
                .failureUrl("/login?error=true")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oauth2UserService())
                )
            )
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
                    "/audio/**",  // Allow access to audio files
                    "/error",
                    "/api/games/**",  // Allow access to games API endpoints
                    "/index",
                    "/about",     // Allow access to index page without authentication
                    "/oauth2/**"  // Add OAuth2 endpoints
                ).permitAll();
                registry.anyRequest().authenticated();
            })
            .csrf(csrf -> csrf.disable())
            .build();
    }

    @Bean
    public OAuth2UserService oauth2UserService() {
        return new CustomOAuth2UserService(userService);
    }
}
