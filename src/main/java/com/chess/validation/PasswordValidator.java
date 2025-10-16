package com.chess.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Custom password validator with strong security requirements
 * Enforces password complexity for better security
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    // Minimum 8 characters, at least one uppercase, lowercase, digit, and special character
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    
    // Common weak passwords to block - REMOVED to allow any password that meets complexity requirements
    // private static final String[] COMMON_PASSWORDS = {};
    
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        // Trim whitespace
        password = password.trim();
        
        // Check minimum length
        if (password.length() < 8) {
            return false;
        }
        
        // Check maximum length (reasonable limit)
        if (password.length() > 128) {
            return false;
        }
        
        // Check for common weak passwords - REMOVED to allow any password that meets complexity requirements
        // String lowerPassword = password.toLowerCase();
        // for (String commonPassword : COMMON_PASSWORDS) {
        //     if (lowerPassword.equals(commonPassword)) {
        //         return false;
        //     }
        // }
        
        // Check for repeated characters - REMOVED to allow patterns like "aaa"
        // if (hasRepeatedCharacters(password, 3)) {
        //     return false;
        // }
        
        // Check for sequential characters - REMOVED to allow patterns like "abcd", "1234"
        // if (hasSequentialCharacters(password, 3)) {
        //     return false;
        // }
        
        // Check password pattern (uppercase, lowercase, digit, special char)
        return pattern.matcher(password).matches();
    }
    
    /**
     * Check for repeated characters in password
     */
    private boolean hasRepeatedCharacters(String password, int maxRepeats) {
        for (int i = 0; i < password.length() - maxRepeats; i++) {
            char currentChar = password.charAt(i);
            int repeatCount = 1;
            
            for (int j = i + 1; j < password.length(); j++) {
                if (password.charAt(j) == currentChar) {
                    repeatCount++;
                    if (repeatCount > maxRepeats) {
                        return true;
                    }
                } else {
                    break;
                }
            }
        }
        return false;
    }
    
    /**
     * Check for sequential characters in password
     * Only checks for obvious keyboard sequences, not number sequences
     */
    private boolean hasSequentialCharacters(String password, int maxSequence) {
        // Convert to lowercase for case-insensitive checking
        String lowerPassword = password.toLowerCase();
        
        // Check for letter sequences (abc, def, etc.)
        for (int i = 0; i < lowerPassword.length() - maxSequence; i++) {
            int sequenceCount = 1;
            char currentChar = lowerPassword.charAt(i);
            
            // Only check if current character is a letter
            if (currentChar >= 'a' && currentChar <= 'z') {
                for (int j = i + 1; j < lowerPassword.length(); j++) {
                    char nextChar = lowerPassword.charAt(j);
                    if (nextChar == currentChar + sequenceCount && nextChar >= 'a' && nextChar <= 'z') {
                        sequenceCount++;
                        if (sequenceCount > maxSequence) {
                            return true;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return false;
    }
}
