package com.omp.hub.callback.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;
import com.omp.hub.callback.infrastructure.persistence.message.sqs.MessageSQS;
import com.omp.hub.callback.infrastructure.persistence.message.sqs.SqsMessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryService {

    @Value("${client.retry.max-retries-api:3}")
    private int maxAttempts;

    @Value("${client.retry.retry-delay-api-seconds:60}")
    private long delaySeconds;

    private final SqsMessageRepository sqsMessageRepository;

    public void executeWithRetrySyncVoid(UUID uuid, String operationName, Runnable operation) {
        executeWithRetrySyncVoid(uuid, operationName, operation, null);
    }

    public void executeWithRetrySyncVoid(UUID uuid, String operationName, Runnable operation, Object callbackData) {
        int attempt = 1;
        Exception lastException = null;

        while (attempt <= maxAttempts) {
            try {
                log.info("TxId: {} - Executando operacao: {} - Tentativa: {}/{}", 
                        uuid, operationName, attempt, maxAttempts);
                
                operation.run();
                
                if (attempt > 1) {
                    log.info("TxId: {} - Operacao: {} bem-sucedida apos {} tentativas", 
                            uuid, operationName, attempt);
                }
                
                return;
                
            } catch (Exception e) {
                lastException = e;
                
                if (e instanceof BusinessException) {
                    BusinessException businessException = (BusinessException) e;
                    
                    if (businessException.isUnprocessableEntity()) {
                        log.error("TxId: {} - Operacao: {} retornou erro 422 (Unprocessable Entity). Enviando para DLQ sem retry.", 
                                uuid, operationName);
                        sendToDLQ(callbackData, businessException);
                        throw businessException;
                    }
                }
                
                log.warn("TxId: {} - Falha na operacao: {} - Tentativa: {}/{} - Erro: {}", 
                        uuid, operationName, attempt, maxAttempts, e.getMessage());
                
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delaySeconds * 1000 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrompido", ie);
                    }
                }
                
                attempt++;
            }
        }

        log.error("TxId: {} - Operacao: {} falhou apos {} tentativas", 
                uuid, operationName, maxAttempts);
        throw new RuntimeException("Operacao falhou apos " + maxAttempts + " tentativas", lastException);
    }

    private void sendToDLQ(Object callbackData, BusinessException exception) {
        try {
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
            log.info("Callback com erro 422 enviado para DLQ com sucesso");
        } catch (Exception e) {
            log.error("Erro ao enviar callback para DLQ: {}", e.getMessage(), e);
        }
    }
}

