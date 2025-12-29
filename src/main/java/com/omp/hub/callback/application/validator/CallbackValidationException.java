package com.omp.hub.callback.application.validator;

import lombok.Getter;

@Getter
public class CallbackValidationException extends RuntimeException {
    
    private final String details;
    
    public CallbackValidationException(String message, String details) {
        super(message);
        this.details = details;
    }
}
