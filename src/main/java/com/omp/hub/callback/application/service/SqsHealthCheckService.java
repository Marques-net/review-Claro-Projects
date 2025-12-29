package com.omp.hub.callback.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsHealthCheckService {

    private final SqsClient sqsClient;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Value("${aws.sqs.dlq-url}")
    private String dlqUrl;

    public Map<String, Object> checkSqsHealth() {
        Map<String, Object> sqsStatus = new HashMap<>();
        
        try {
            log.debug("Verificando conectividade com SQS: {}", queueUrl);
            
            if (sqsClient == null) {
                throw new IllegalStateException("SQS client não foi configurado");
            }
            
            String serviceName = sqsClient.serviceName();
            if (serviceName == null || !serviceName.equals("sqs")) {
                throw new IllegalStateException("Cliente SQS não está configurado corretamente");
            }
            
            sqsStatus.put("status", "UP");
            sqsStatus.put("queueUrl", queueUrl);
            sqsStatus.put("service", serviceName);
            sqsStatus.put("message", "SQS client configured successfully");
            
            log.debug("SQS está funcionando corretamente");
            
        } catch (SqsException e) {
            log.error("Erro ao verificar status do SQS: {}", e.getMessage(), e);
            sqsStatus.put("status", "DOWN");
            sqsStatus.put("queueUrl", queueUrl);
            sqsStatus.put("error", e.getMessage());
            sqsStatus.put("errorCode", e.awsErrorDetails().errorCode());
            
        } catch (Exception e) {
            log.error("Erro inesperado ao verificar status do SQS: {}", e.getMessage(), e);
            sqsStatus.put("status", "DOWN");
            sqsStatus.put("queueUrl", queueUrl);
            sqsStatus.put("error", e.getMessage());
        }
        
        return sqsStatus;
    }

    public boolean isSqsHealthy() {
        try {
            Map<String, Object> status = checkSqsHealth();
            return "UP".equals(status.get("status"));
        } catch (Exception e) {
            log.error("Erro ao verificar se SQS está saudável: {}", e.getMessage(), e);
            return false;
        }
    }

    public Map<String, Object> checkDlqHealth() {
        Map<String, Object> dlqStatus = new HashMap<>();
        
        try {
            log.debug("Verificando status da DLQ: {}", dlqUrl);
            
            var getQueueAttributesRequest = software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest.builder()
                    .queueUrl(dlqUrl)
                    .attributeNames(
                        software.amazon.awssdk.services.sqs.model.QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
                        software.amazon.awssdk.services.sqs.model.QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE
                    )
                    .build();
            
            var response = sqsClient.getQueueAttributes(getQueueAttributesRequest);
            
            String messagesCount = response.attributes().get(
                software.amazon.awssdk.services.sqs.model.QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES
            );
            String messagesNotVisibleCount = response.attributes().get(
                software.amazon.awssdk.services.sqs.model.QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE
            );
            
            int totalMessages = Integer.parseInt(messagesCount != null ? messagesCount : "0");
            int notVisibleMessages = Integer.parseInt(messagesNotVisibleCount != null ? messagesNotVisibleCount : "0");
            
            String status = totalMessages > 0 ? "WARNING" : "UP";
            
            dlqStatus.put("status", status);
            dlqStatus.put("dlqUrl", dlqUrl);
            dlqStatus.put("approximateNumberOfMessages", totalMessages);
            dlqStatus.put("approximateNumberOfMessagesNotVisible", notVisibleMessages);
            dlqStatus.put("message", totalMessages > 0 
                ? "DLQ contém " + totalMessages + " mensagem(ns) que requer(em) atenção" 
                : "DLQ está vazia");
            
            log.debug("DLQ status verificado: {} mensagens", totalMessages);
            
        } catch (SqsException e) {
            log.error("Erro ao verificar status da DLQ: {}", e.getMessage(), e);
            dlqStatus.put("status", "DOWN");
            dlqStatus.put("dlqUrl", dlqUrl);
            dlqStatus.put("error", e.getMessage());
            dlqStatus.put("errorCode", e.awsErrorDetails().errorCode());
            
        } catch (Exception e) {
            log.error("Erro inesperado ao verificar status da DLQ: {}", e.getMessage(), e);
            dlqStatus.put("status", "DOWN");
            dlqStatus.put("dlqUrl", dlqUrl);
            dlqStatus.put("error", e.getMessage());
        }
        
        return dlqStatus;
    }
}