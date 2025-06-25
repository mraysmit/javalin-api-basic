package dev.mars.service.validation;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for validating request objects using Bean Validation.
 */
@Singleton
public class ValidationService {
    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    
    private final Validator validator;

    public ValidationService() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
        logger.info("Validation service initialized");
    }

    /**
     * Validates an object and throws ValidationException if validation fails.
     * 
     * @param object The object to validate
     * @throws ValidationException if validation fails
     */
    public void validate(Object object) {
        if (object == null) {
            throw new ValidationException("Object cannot be null");
        }
        
        Set<ConstraintViolation<Object>> violations = validator.validate(object);
        
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
            
            logger.debug("Validation failed for {}: {}", object.getClass().getSimpleName(), errorMessage);
            throw new ValidationException("Validation failed: " + errorMessage);
        }
        
        logger.trace("Validation passed for {}", object.getClass().getSimpleName());
    }

    /**
     * Validates an object and returns validation results.
     * 
     * @param object The object to validate
     * @return ValidationResult containing validation status and errors
     */
    public ValidationResult validateWithResult(Object object) {
        if (object == null) {
            return ValidationResult.failure("Object cannot be null");
        }
        
        Set<ConstraintViolation<Object>> violations = validator.validate(object);
        
        if (violations.isEmpty()) {
            return ValidationResult.success();
        }
        
        String errorMessage = violations.stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining(", "));
        
        return ValidationResult.failure(errorMessage);
    }

    /**
     * Validation result holder.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Exception thrown when validation fails.
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
