package com.omp.hub.callback.infrastructure.persistence.message.sqs;

import com.google.gson.Gson;
import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;
import software.amazon.awssdk.services.sqs.model.KmsAccessDeniedException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SqsMessageRepository {

    private final SqsClient sqsClient;
    private final Gson gson;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.dlq-url}")
    private String dlqUrl;

    public <T> void sendMessage(CallbackRequest<T> callbackRequest) {
        try {
            log.info("Enviando mensagem para a fila SQS: {}", queueUrl);

            MessageSQS<T> messageSQS = MessageSQS.<T>builder()
                    .callbackRequest(callbackRequest)
                    .timestamp(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            String messageBody = gson.toJson(messageSQS);
            String messageGroupId = extractMessageGroupId(callbackRequest);

            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("timestamp", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(messageSQS.getTimestamp().toString())
                    .build());

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageAttributes(messageAttributes)
                    .messageGroupId(messageGroupId)
                    .messageDeduplicationId(UUID.randomUUID().toString())
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);

            log.info("Mensagem enviada com sucesso. MessageId: {}, MessageGroupId: {}",
                    response.messageId(), messageGroupId);

        } catch (KmsAccessDeniedException e) {
            log.error("Erro de acesso KMS ao enviar mensagem para SQS. RequestId={}, ErrorCode={}",
                    e.requestId(), e.awsErrorDetails().errorCode());
            throw new RuntimeException("Falha de permissao KMS ao enviar mensagem para SQS", e);
        } catch (SqsException e) {
            log.error("Erro ao enviar mensagem para a fila SQS: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar mensagem para SQS", e);
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar mensagem: {}", e.getMessage(), e);
            throw new RuntimeException("Erro inesperado ao enviar mensagem", e);
        }
    }

    public <T> void sendToDLQ(MessageSQS<T> message, Exception error) {
        try {
            log.error("Enviando mensagem para DLQ: {}", dlqUrl);

            message.setErrorMessage(error.getMessage());
            message.setErrorStackTrace(getStackTraceAsString(error));
            message.setFailureTimestamp(LocalDateTime.now());

            String messageBody = gson.toJson(message);

            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("originalTimestamp", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(message.getTimestamp() != null ? message.getTimestamp().toString() : LocalDateTime.now().toString())
                    .build());
            messageAttributes.put("failureTimestamp", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(message.getFailureTimestamp().toString())
                    .build());
            messageAttributes.put("retryCount", MessageAttributeValue.builder()
                    .dataType("Number")
                    .stringValue(String.valueOf(message.getRetryCount() != null ? message.getRetryCount() : 0))
                    .build());
            messageAttributes.put("errorMessage", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(error.getMessage() != null ? error.getMessage() : "Unknown error")
                    .build());

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(dlqUrl)
                    .messageBody(messageBody)
                    .messageAttributes(messageAttributes)
                    .messageGroupId("payment-callback-dlq-group")
                    .messageDeduplicationId((message.getMessageId() != null ? message.getMessageId() : UUID.randomUUID().toString()) + "-" + System.currentTimeMillis())
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);

            log.info("Mensagem enviada para DLQ com sucesso. MessageId: {}", response.messageId());

        } catch (KmsAccessDeniedException e) {
            log.error("Erro de acesso KMS ao enviar mensagem para DLQ. RequestId={}, ErrorCode={}",
                    e.requestId(), e.awsErrorDetails().errorCode());
            throw new RuntimeException("Falha de permissao KMS ao enviar mensagem para DLQ", e);
        } catch (SqsException e) {
            log.error("Erro ao enviar mensagem para DLQ: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar mensagem para DLQ", e);
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar mensagem para DLQ: {}", e.getMessage(), e);
            throw new RuntimeException("Erro inesperado ao enviar mensagem para DLQ", e);
        }
    }

    public <T> void resendForRetry(MessageSQS<T> message) {
        try {
            int currentRetryCount = message.getRetryCount() != null ? message.getRetryCount() : 0;
            int newRetryCount = currentRetryCount + 1;
            
            log.info("Reenviando mensagem para retry. MessageId: {}, RetryCount: {} -> {}", 
                    message.getMessageId(), currentRetryCount, newRetryCount);

            message.setRetryCount(newRetryCount);
            
            String messageBody = gson.toJson(message);
            String messageGroupId = extractMessageGroupIdFromMessage(message);

            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("timestamp", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(message.getTimestamp() != null ? message.getTimestamp().toString() : LocalDateTime.now().toString())
                    .build());
            messageAttributes.put("retryCount", MessageAttributeValue.builder()
                    .dataType("Number")
                    .stringValue(String.valueOf(newRetryCount))
                    .build());

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageAttributes(messageAttributes)
                    .messageGroupId(messageGroupId)
                    .messageDeduplicationId(UUID.randomUUID().toString())
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);

            log.info("Mensagem reenviada para retry com sucesso. NewMessageId: {}, RetryCount: {}", 
                    response.messageId(), newRetryCount);

        } catch (Exception e) {
            log.error("Erro ao reenviar mensagem para retry: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao reenviar mensagem para retry", e);
        }
    }

    private <T> String extractMessageGroupIdFromMessage(MessageSQS<T> message) {
        if (message.getCallbackRequest() != null) {
            return extractMessageGroupId(message.getCallbackRequest());
        }
        return "payment-callback-default";
    }

    private String getStackTraceAsString(Exception error) {
        if (error == null) {
            return "No stack trace available";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(error.getClass().getName()).append(": ").append(error.getMessage()).append("\n");

        for (StackTraceElement element : error.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if (sb.length() > 5000) {
                sb.append("\t... (truncated)");
                break;
            }
        }

        return sb.toString();
    }

    private <T> String extractMessageGroupId(CallbackRequest<T> callbackRequest) {
        try {
            if (callbackRequest == null || callbackRequest.getData() == null) {
                return "payment-callback-default";
            }

            String dataJson = gson.toJson(callbackRequest.getData());

            com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(dataJson).getAsJsonObject();

            if (jsonObject.has("ompTransactionId") && !jsonObject.get("ompTransactionId").isJsonNull()) {
                String ompTransactionId = jsonObject.get("ompTransactionId").getAsString();
                if (ompTransactionId != null && !ompTransactionId.isEmpty()) {
                    return "callback-" + ompTransactionId;
                }
            }

            if (jsonObject.has("identifier") && !jsonObject.get("identifier").isJsonNull()) {
                String identifier = jsonObject.get("identifier").getAsString();
                if (identifier != null && !identifier.isEmpty()) {
                    return "callback-" + identifier;
                }
            }

            if (jsonObject.has("txId") && !jsonObject.get("txId").isJsonNull()) {
                String txId = jsonObject.get("txId").getAsString();
                if (txId != null && !txId.isEmpty()) {
                    return "callback-" + txId;
                }
            }

            return "payment-callback-default";

        } catch (Exception e) {
            log.warn("Nao foi possivel extrair identificador para message group. Usando default. Erro: {}",
                    e.getMessage());
            return "payment-callback-default";
        }
    }
}
