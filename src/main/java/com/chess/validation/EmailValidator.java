package com.chess.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Custom email validator with comprehensive validation
 * Validates email format, length, and common security issues
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    
    // RFC 5322 compliant email regex (simplified but comprehensive)
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";
    
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    // Common disposable email domains to block
    private static final String[] DISPOSABLE_EMAIL_DOMAINS = {
        "10minutemail.com", "tempmail.org", "guerrillamail.com", 
        "mailinator.com", "temp-mail.org", "throwaway.email"
    };
    
    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Trim whitespace
        email = email.trim();
        
        // Check length (reasonable limits)
        if (email.length() < 5 || email.length() > 254) {
            return false;
        }
        
        // Check for basic format
        if (!pattern.matcher(email).matches()) {
            return false;
        }
        
        // Check for disposable email domains
        String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase();
        for (String disposableDomain : DISPOSABLE_EMAIL_DOMAINS) {
            if (domain.equals(disposableDomain)) {
                return false;
            }
        }
        
        // Check for suspicious patterns
        if (email.contains("..") || email.startsWith(".") || email.endsWith(".")) {
            return false;
        }
        
        // Check for consecutive dots in domain
        if (domain.contains("..")) {
            return false;
        }
        
        return true;
    }
}
