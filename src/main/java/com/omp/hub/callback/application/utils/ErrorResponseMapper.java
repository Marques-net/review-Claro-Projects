package com.omp.hub.callback.application.utils;

import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ErrorResponseMapper {
    
    public ErrorResponse mapExceptionToErrorResponse(Exception ex, String path) {
        return ErrorResponse.builder()
            .status(422)
            .timestamp(Instant.now())
            .message("Não foi possível processar o callback do pagamento devido a um erro interno")
            .path(path)
            .errorCode("CALLBACK_PROCESSING_ERROR")
            .details(String.format("Falha no processamento: %s", 
                ex.getMessage() != null ? ex.getMessage() : "Erro desconhecido"))
            .build();
    }

    public ErrorResponse mapMaxRetriesExceededError(String identifier, int maxRetries, Exception lastException) {
        String userFriendlyMessage = "O processamento do callback do pagamento falhou após múltiplas tentativas. " +
            "Por favor, verifique o status do pagamento e entre em contato com o suporte se necessário.";
        
        String technicalDetails = String.format(
            "Transação %s falhou após %d tentativas. Último erro: %s", 
            identifier, 
            maxRetries, 
            lastException != null && lastException.getMessage() != null 
                ? lastException.getMessage() 
                : "Erro não especificado durante o processamento do callback"
        );

        return ErrorResponse.builder()
            .status(424)
            .timestamp(Instant.now())
            .message(userFriendlyMessage)
            .path("/callback")
            .errorCode("CALLBACK_MAX_RETRIES_EXCEEDED")
            .details(technicalDetails)
            .build();
    }
}