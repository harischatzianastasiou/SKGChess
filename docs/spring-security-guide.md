# Spring Security and Thymeleaf Security Guide

## Table of Contents
1. [Spring Security Configuration](#spring-security-configuration)
2. [Thymeleaf Security Attributes](#thymeleaf-security-attributes)
3. [URL Authorization Rules](#url-authorization-rules)
4. [Authentication Flow](#authentication-flow)

## Spring Security Configuration

### Basic Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .formLogin(httpForm -> {
                httpForm
                    .loginPage("/login")
                    .defaultSuccessUrl("/index", true)
                    .permitAll();
            })
            .authorizeHttpRequests(registry -> {
                registry.requestMatchers(
                    "/signup",
                    "/login", 
                    "/game/**",
                    "/css/**",   
                    "/js/**",    
                    "/images/**", 
                    "/error",
                    "/api/games/**",
                    "/index"
                ).permitAll();
                registry.anyRequest().authenticated();
            })
            .build();
    }
}
```

### Key Components
- `@Configuration`: Marks the class as a source of bean definitions
- `@EnableWebSecurity`: Enables web security configuration
- `SecurityFilterChain`: Defines the security filter chain for the application

## Thymeleaf Security Attributes

### Basic Usage
```html
<!-- For authenticated users -->
<div sec:authorize="isAuthenticated()">
    <!-- Content for logged-in users -->
</div>

<!-- For non-authenticated users -->
<div sec:authorize="!isAuthenticated()">
    <!-- Content for non-logged-in users -->
</div>
```

### Common Attributes
- `sec:authorize="isAuthenticated()"`: Shows content only to logged-in users
- `sec:authorize="!isAuthenticated()"`: Shows content only to non-logged-in users
- `sec:authentication="name"`: Gets the username of the authenticated user
- `sec:authorize="hasRole('ROLE_ADMIN')"`: Checks for specific roles
- `sec:authorize="hasAuthority('READ')"`: Checks for specific authorities

### Setup Requirements
```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

## URL Authorization Rules

### Understanding `anyRequest().authenticated()`
```java
registry.requestMatchers("/public/**").permitAll();
registry.anyRequest().authenticated();
```

#### How it Works:
1. Spring Security evaluates rules in order
2. First checks if URL matches any `permitAll()` patterns
3. If matched, access is granted without authentication
4. If not matched, falls through to `anyRequest().authenticated()`
5. `anyRequest().authenticated()` requires authentication for any unmatched URLs

### Example Flow
```
Request for "/index"
↓
Checks permitAll list
↓
Matches "/index" in permitAll
↓
Access granted without authentication

Request for "/admin"
↓
Checks permitAll list
↓
No match found
↓
Falls through to anyRequest().authenticated()
↓
Requires authentication
```

## Authentication Flow

### Login Configuration
```java
.formLogin(httpForm -> {
    httpForm
        .loginPage("/login")        // Custom login page URL
        .defaultSuccessUrl("/index", true)  // Redirect after login
        .permitAll();               // Allow access to login page
})
```

### Remember Me Configuration
```java
.rememberMe(remember -> {
    remember
        .key("uniqueAndSecretKey")
        .tokenValiditySeconds(86400)  // 24 hours
        .rememberMeParameter("remember")
        .rememberMeCookieName("remember-me-cookie");
})
```

### Logout Configuration
```java
.logout(logout -> {
    logout
        .logoutSuccessUrl("/login?logout")
        .deleteCookies("remember-me-cookie")
        .permitAll();
})
```

## Best Practices
1. Always use HTTPS in production
2. Keep security keys secure and unique
3. Implement proper CSRF protection
4. Use strong password encoding
5. Implement proper session management
6. Regular security audits and updates 