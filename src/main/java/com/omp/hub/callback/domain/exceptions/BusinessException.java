package com.omp.hub.callback.domain.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private ErrorResponse error;

    public BusinessException(String message, String errorCode) {
        super(message);
        this.error = ErrorResponse.builder()
                .errorCode(errorCode)
                .build();
    }

    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.error = ErrorResponse.builder()
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }

    public BusinessException(String message, String errorCode, String description) {
        super(message);
        this.error = ErrorResponse.builder()
                .errorCode(errorCode)
                .details(description)
                .timestamp(Instant.now())
                .build();
    }

    public BusinessException(String message, String errorCode, String description, HttpStatus httpStatus) {
        this.error = ErrorResponse.builder()
                .errorCode(errorCode)
                .details(description)
                .status(httpStatus.value())
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public BusinessException(ErrorResponse error) {
        super(error != null && error.getMessage() != null ? error.getMessage() : "Erro de negócio");
        this.error = error;
    }

    public BusinessException(Exception e) {
        super("Ocorreu um erro interno", e);
        this.error = ErrorResponse.builder()
                .message("Ocorreu um erro interno")
                .details(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
                .errorCode("ERROR")
                .status(500)
                .timestamp(Instant.now())
                .build();
    }

    public boolean isUnprocessableEntity() {
        return error != null && error.getStatus() == 422;
    }
}
