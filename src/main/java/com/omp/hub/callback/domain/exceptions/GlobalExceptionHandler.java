package com.omp.hub.callback.domain.exceptions;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.omp.hub.callback.application.validator.CallbackValidationException;
import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;
import com.omp.hub.callback.infrastructure.persistence.message.sqs.MessageSQS;
import com.omp.hub.callback.infrastructure.persistence.message.sqs.SqsMessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final SqsMessageRepository sqsMessageRepository;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        ErrorResponse errorResponse = ex.getError();
        
        if (errorResponse.getStatus() == null) {
            errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        }
        
        if (errorResponse.getMessage() == null) {
            errorResponse.setMessage(ex.getMessage() != null ? ex.getMessage() : "Erro de negócio");
        }
        
        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(errorResponse.getStatus()));
    }

    @ExceptionHandler(CallbackValidationException.class)
    public ResponseEntity<ErrorResponse> handleCallbackValidationException(
            CallbackValidationException ex, WebRequest request) {
        
        log.error("Erro de validação no callback: {}", ex.getDetails());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .details(ex.getDetails())
                .errorCode("VALIDATION_ERROR")
                .timestamp(Instant.now())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Unprocessable422Exception.class)
    public ResponseEntity<ErrorResponse> handleUnprocessable422Exception(
            Unprocessable422Exception ex, WebRequest request) {
        
        log.error("Erro 422 detectado. Enviando para DLQ sem retry.");
        
        sendToDLQ(ex);
        
        BusinessException businessException = ex.getOriginalException();
        ErrorResponse errorResponse = businessException != null ? 
            businessException.getError() : 
            ErrorResponse.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .message("Erro de validação")
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private void sendToDLQ(Unprocessable422Exception exception) {
        try {
            Object callbackData = exception.getCallbackData();
            if (callbackData == null) {
                log.warn("Nenhum dado de callback para enviar para DLQ");
                return;
            }

            CallbackRequest<Object> callbackRequest = new CallbackRequest<>();
            callbackRequest.setData(callbackData);
            
            MessageSQS<Object> messageSQS = MessageSQS.<Object>builder()
                    .callbackRequest(callbackRequest)
                    .timestamp(LocalDateTime.now())
                    .retryCount(0)
                    .build();
            
            sqsMessageRepository.sendToDLQ(messageSQS, exception);
            log.info("Callback com erro 422 enviado para DLQ");
        } catch (Exception e) {
            log.error("Erro ao enviar callback para DLQ: {}", e.getMessage(), e);
        }
    }
}
