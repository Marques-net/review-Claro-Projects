package com.omp.hub.callback.application.consumer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.service.CallbackErrorNotificationService;
import com.omp.hub.callback.domain.service.impl.callback.CallbackService;
import com.omp.hub.callback.infrastructure.persistence.message.sqs.MessageSQS;
import com.omp.hub.callback.infrastructure.persistence.message.sqs.SqsMessageRepository;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Component
@ConditionalOnProperty(
    name = "aws.sqs.consumer.enabled",
    havingValue = "true",
    matchIfMissing = false
)
@Slf4j
public class SqsCallbackListener {

    private final SqsClient sqsClient;
    private final CallbackService callbackService;
    private final ObjectMapper objectMapper;
    private final CallbackErrorNotificationService callbackErrorNotificationService;
    private final SqsMessageRepository sqsMessageRepository;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.consumer.max-retries-sqs:3}")
    private int maxRetries;

    @Value("${aws.sqs.consumer.max-number-of-messages:10}")
    private int maxNumberOfMessages;

    @Value("${aws.sqs.consumer.wait-time-seconds:20}")
    private int waitTimeSeconds;

    public SqsCallbackListener(SqsClient sqsClient, CallbackService callbackService, ObjectMapper objectMapper,
            CallbackErrorNotificationService callbackErrorNotificationService,
            SqsMessageRepository sqsMessageRepository) {
        this.sqsClient = sqsClient;
        this.callbackService = callbackService;
        this.objectMapper = objectMapper;
        this.callbackErrorNotificationService = callbackErrorNotificationService;
        this.sqsMessageRepository = sqsMessageRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Async
    public void startPolling() {
        if (running.compareAndSet(false, true)) {
            log.info("Iniciando polling do SQS. Queue URL: {}", queueUrl);
            pollMessages();
        }
    }

    @PreDestroy
    public void stopPolling() {
        log.info("Parando polling do SQS");
        running.set(false);
    }

    private void pollMessages() {
        while (running.get()) {
            try {
                ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(maxNumberOfMessages)
                        .waitTimeSeconds(waitTimeSeconds)
                        .build();

                ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
                List<Message> messages = response.messages();

                if (!messages.isEmpty()) {
                    log.info("Recebidas {} mensagens do SQS", messages.size());
                    for (Message message : messages) {
                        processMessage(message);
                    }
                }

            } catch (Exception e) {
                log.error("Erro durante polling do SQS: {}", e.getMessage(), e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.info("Polling do SQS finalizado");
    }

    private void processMessage(Message message) {
        String messageId = message.messageId();
        String txId = null;
        MessageSQS<Object> messageSQS = null;

        try {
            log.info("Mensagem recebida do SQS. MessageId: {}", messageId);

            messageSQS = parseMessage(message.body());
            txId = extractTxId(messageSQS);
            messageId = messageSQS.getMessageId() != null ? messageSQS.getMessageId() : messageId;
            
            int retryCount = getRetryCount(messageSQS);

            log.info("Processando callback. MessageId: {}, TxId: {}, RetryCount: {}/{}",
                    messageId, txId, retryCount, maxRetries);

            String jsonPayload = extractDataAsJson(messageSQS);
            callbackService.processCallback(jsonPayload);

            deleteMessage(message.receiptHandle());
            log.info("Callback processado com sucesso. MessageId: {}, TxId: {}", messageId, txId);

        } catch (Exception e) {
            int retryCount = getRetryCount(messageSQS);
            log.error("Erro ao processar callback. MessageId: {}, TxId: {}, RetryCount: {}/{}, Erro: {}",
                    messageId, txId, retryCount, maxRetries, e.getMessage(), e);

            handleProcessingError(messageSQS, messageId, txId, e, message.receiptHandle(), retryCount);
        }
    }

    private void deleteMessage(String receiptHandle) {
        try {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build();
            sqsClient.deleteMessage(deleteRequest);
        } catch (Exception e) {
            log.error("Erro ao deletar mensagem do SQS: {}", e.getMessage(), e);
        }
    }

    private int getRetryCount(MessageSQS<Object> messageSQS) {
        if (messageSQS != null && messageSQS.getRetryCount() != null) {
            return messageSQS.getRetryCount();
        }
        return 1;
    }

    private void handleProcessingError(MessageSQS<Object> messageSQS, String messageId, 
            String txId, Exception error, String receiptHandle, int retryCount) {
        try {
            if (retryCount >= maxRetries) {
                log.error("Limite de retentativas atingido ({}/{}). MessageId: {}, TxId: {}. Enviando para DLQ.",
                        retryCount, maxRetries, messageId, txId);

                if (txId != null) {
                    callbackErrorNotificationService.notifyJourneyAboutCallbackFailure(txId, maxRetries, error);
                }
                
                if (messageSQS != null) {
                    sqsMessageRepository.sendToDLQ(messageSQS, error);
                }
                
                deleteMessage(receiptHandle);
                log.info("Mensagem removida da fila e enviada para DLQ. MessageId: {}, TxId: {}", messageId, txId);
                
            } else {
                if (messageSQS != null) {
                    sqsMessageRepository.resendForRetry(messageSQS);
                    log.info("Mensagem reenviada para retry {}/{}. MessageId: {}, TxId: {}",
                            retryCount + 1, maxRetries, messageId, txId);
                }
                
                deleteMessage(receiptHandle);
            }

        } catch (Exception e) {
            log.error("Erro ao tratar falha de processamento. MessageId: {}, Erro: {}",
                    messageId, e.getMessage(), e);
            try {
                deleteMessage(receiptHandle);
            } catch (Exception delError) {
                log.error("Erro ao deletar mensagem: {}", delError.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private MessageSQS<Object> parseMessage(String messageBody) {
        try {
            return objectMapper.readValue(messageBody, MessageSQS.class);
        } catch (Exception e) {
            log.error("Erro ao parsear mensagem: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao parsear mensagem", e);
        }
    }

    private String extractDataAsJson(MessageSQS<Object> messageSQS) {
        try {
            if (messageSQS.getCallbackRequest() != null && messageSQS.getCallbackRequest().getData() != null) {
                return objectMapper.writeValueAsString(messageSQS.getCallbackRequest().getData());
            }
            throw new IllegalStateException("CallbackRequest ou Data esta nulo");
        } catch (Exception e) {
            log.error("Erro ao extrair data como JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao extrair payload do callback", e);
        }
    }

    private String extractTxId(MessageSQS<Object> messageSQS) {
        try {
            if (messageSQS.getCallbackRequest() != null && messageSQS.getCallbackRequest().getData() != null) {
                Object data = messageSQS.getCallbackRequest().getData();
                String dataJson = objectMapper.writeValueAsString(data);

                com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(dataJson).getAsJsonObject();

                if (jsonObject.has("txId") && !jsonObject.get("txId").isJsonNull()) {
                    String txId = jsonObject.get("txId").getAsString();
                    if (txId != null && !txId.isEmpty()) {
                        return txId;
                    }
                }

                if (jsonObject.has("identifier") && !jsonObject.get("identifier").isJsonNull()) {
                    String identifier = jsonObject.get("identifier").getAsString();
                    if (identifier != null && !identifier.isEmpty()) {
                        return identifier;
                    }
                }

                if (jsonObject.has("ompTransactionId") && !jsonObject.get("ompTransactionId").isJsonNull()) {
                    String ompTransactionId = jsonObject.get("ompTransactionId").getAsString();
                    if (ompTransactionId != null && !ompTransactionId.isEmpty()) {
                        return ompTransactionId;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Nao foi possivel extrair txId: {}", e.getMessage());
        }
        return null;
    }

    // Métodos para teste
    protected boolean isRunning() {
        return running.get();
    }

    protected void setRunning(boolean value) {
        running.set(value);
    }
}
