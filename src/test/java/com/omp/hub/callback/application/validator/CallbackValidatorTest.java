package com.omp.hub.callback.application.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallbackValidatorTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private CallbackValidator callbackValidator;

    @BeforeEach
    void setUp() {
    }

    @Test
    void validate_WithValidObject_ShouldNotThrowException() {
        // Given
        Object validObject = new Object();
        when(validator.validate(any())).thenReturn(Collections.emptySet());

        // When & Then
        assertDoesNotThrow(() -> callbackValidator.validate(validObject, "TestObject"));
        verify(validator).validate(validObject);
    }

    @Test
    void validate_WithSingleViolation_ShouldThrowCallbackValidationException() {
        // Given
        TestObject invalidObject = new TestObject();
        
        @SuppressWarnings("unchecked")
        ConstraintViolation<TestObject> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("field1");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be null");

        Set<ConstraintViolation<TestObject>> violations = new HashSet<>();
        violations.add(violation);
        
        when(validator.validate(invalidObject)).thenReturn(violations);

        // When & Then
        CallbackValidationException exception = assertThrows(CallbackValidationException.class, 
            () -> callbackValidator.validate(invalidObject, "TestObject"));
        
        assertTrue(exception.getMessage().contains("Erro de validação no callback TestObject"));
        assertTrue(exception.getDetails().contains("field1: must not be null"));
    }

    @Test
    void validate_WithMultipleViolations_ShouldIncludeAllInDetails() {
        // Given
        TestObject invalidObject = new TestObject();
        
        @SuppressWarnings("unchecked")
        ConstraintViolation<TestObject> violation1 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        when(path1.toString()).thenReturn("field1");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("must not be null");

        @SuppressWarnings("unchecked")
        ConstraintViolation<TestObject> violation2 = mock(ConstraintViolation.class);
        Path path2 = mock(Path.class);
        when(path2.toString()).thenReturn("field2");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("must not be empty");

        Set<ConstraintViolation<TestObject>> violations = new HashSet<>();
        violations.add(violation1);
        violations.add(violation2);
        
        when(validator.validate(invalidObject)).thenReturn(violations);

        // When & Then
        CallbackValidationException exception = assertThrows(CallbackValidationException.class, 
            () -> callbackValidator.validate(invalidObject, "TestObject"));
        
        assertTrue(exception.getMessage().contains("Erro de validação no callback TestObject"));
        String details = exception.getDetails();
        assertTrue(details.contains("field1: must not be null") || details.contains("field2: must not be empty"));
    }

    @Test
    void validate_WithDifferentObjectTypes_ShouldIncludeTypeInMessage() {
        // Given
        TestObject invalidObject = new TestObject();
        
        @SuppressWarnings("unchecked")
        ConstraintViolation<TestObject> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("orderId");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("is required");

        Set<ConstraintViolation<TestObject>> violations = new HashSet<>();
        violations.add(violation);
        
        when(validator.validate(invalidObject)).thenReturn(violations);

        // When & Then - Test with different object type names
        CallbackValidationException exception1 = assertThrows(CallbackValidationException.class, 
            () -> callbackValidator.validate(invalidObject, "TefWebCallback"));
        assertTrue(exception1.getMessage().contains("TefWebCallback"));

        CallbackValidationException exception2 = assertThrows(CallbackValidationException.class, 
            () -> callbackValidator.validate(invalidObject, "CreditCardCallback"));
        assertTrue(exception2.getMessage().contains("CreditCardCallback"));

        CallbackValidationException exception3 = assertThrows(CallbackValidationException.class, 
            () -> callbackValidator.validate(invalidObject, "PixCallback"));
        assertTrue(exception3.getMessage().contains("PixCallback"));
    }

    @Test
    void validate_WithNullObject_ShouldDelegateToValidator() {
        // Given
        when(validator.validate(null)).thenReturn(Collections.emptySet());

        // When & Then
        assertDoesNotThrow(() -> callbackValidator.validate(null, "NullObject"));
        verify(validator).validate(null);
    }

    // Helper class for testing
    private static class TestObject {
        private String field1;
        private String field2;
    }
}
