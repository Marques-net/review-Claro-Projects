package com.omp.hub.callback.domain.exceptions;

public class Unprocessable422Exception extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final BusinessException originalException;
    private final Object callbackData;

    public Unprocessable422Exception(String message, BusinessException originalException, Object callbackData) {
        super(message, originalException);
        this.originalException = originalException;
        this.callbackData = callbackData;
    }

    public BusinessException getOriginalException() {
        return originalException;
    }

    public Object getCallbackData() {
        return callbackData;
    }
}