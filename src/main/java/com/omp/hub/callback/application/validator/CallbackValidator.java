package com.omp.hub.callback.application.validator;

import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CallbackValidator {

    private final Validator validator;

    public <T> void validate(T object, String objectType) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Erro de validação");
            
            throw new CallbackValidationException(
                    "Erro de validação no callback " + objectType,
                    errors
            );
        }
    }
}
