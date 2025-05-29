package com.chess.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;

import com.chess.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;
    private final String baseUrl;

    @Autowired
    public SecurityConfig(UserService userService, @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.userService = userService;
        this.baseUrl = baseUrl;
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
    public CustomAuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .formLogin(httpForm -> {
                httpForm
                    .loginPage("/index")
                    .loginProcessingUrl("/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .defaultSuccessUrl(baseUrl + "/index?login=true", true)
                    .failureHandler(authenticationFailureHandler())
                    .permitAll();
            })
            .oauth2Login(oauth2 -> {
                oauth2
                    .loginPage("/index")
                    .defaultSuccessUrl(baseUrl + "/index?login=true", true)
                    .failureUrl(baseUrl + "/index?error=true")
                    .userInfoEndpoint(userInfo -> {
                        userInfo.userService(oauth2UserService());
                    });
            })
            .rememberMe(remember -> {
                remember
                    .key("uniqueAndSecretKey")
                    .tokenValiditySeconds(86400)
                    .rememberMeParameter("remember")
                    .rememberMeCookieName("remember-me-cookie");
            })
            .logout(logout -> {
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl(baseUrl + "/?logout=true")
                    .deleteCookies("remember-me-cookie", "JSESSIONID")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .permitAll();
            })
            .exceptionHandling(exception -> {
                exception.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/index"));
            })
            .authorizeHttpRequests(registry -> {
                registry.requestMatchers(
                    "/",
                    "/api/users/signup",
                    "/api/users/login",
                    "/login",
                    "/logout",
                    "/game/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/audio/**",
                    "/error",
                    "/api/games/**",
                    "/index",
                    "/about",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/chess-websocket/**",
                    "/topic/**",
                    "/app/**"
                ).permitAll();
                registry.anyRequest().authenticated();
            })
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/api/users/signup",
                    "/api/users/login",
                    "/login",
                    "/logout",
                    "/api/games/**",
                    "/chess-websocket/**",
                    "/topic/**",
                    "/app/**"
                )
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self' https: 'unsafe-inline' 'unsafe-eval'; connect-src 'self' https: wss: ws: http://localhost:*; img-src 'self' https: data:; form-action 'self' https: http://localhost:*;")
                )
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            )
            .requiresChannel(channel -> channel
                .anyRequest().requiresSecure()
            )
            .build();
    }

    @Bean
    public OAuth2UserService oauth2UserService() {
        return new CustomOAuth2UserService(userService, passwordEncoder());
    }
}
