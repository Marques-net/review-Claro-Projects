package com.omp.hub.callback.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

@Configuration
@Slf4j
public class SqsConfiguration {

    @Value("${aws.credentials.region}")
    private String awsRegion;

    @Value("${aws.credentials.key:}")
    private String accessKeyId;

    @Value("${aws.credentials.secret:}")
    private String secretKey;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Bean
    public SqsClient sqsClient() {
        log.info("Configurando SQS Client - Region: {}, QueueUrl: {}", awsRegion, queueUrl);
        
        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.of(awsRegion));

        if (queueUrl.startsWith("http://localhost")) {
            String key = (accessKeyId == null || accessKeyId.trim().isEmpty()) ? "test" : accessKeyId;
            String secret = (secretKey == null || secretKey.trim().isEmpty()) ? "test" : secretKey;
            
            log.info("Ambiente LOCAL detectado - usando endpoint local e credenciais estáticas");
            builder.endpointOverride(java.net.URI.create("http://localhost:5001"))
                   .credentialsProvider(StaticCredentialsProvider.create(
                       AwsBasicCredentials.create(key, secret)));
        } else {
            log.info("Ambiente AWS detectado - usando DefaultCredentialsProvider (IAM role)");
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        SqsClient client = builder.build();
        
        try {
            log.info("Testando conectividade SQS...");
            client.getQueueUrl(req -> req.queueName(extractQueueName(queueUrl)));
            log.info("Conectividade SQS OK!");
        } catch (Exception e) {
            log.warn("Falha no teste de conectividade SQS: {}", e.getMessage());
        }
        
        return client;
    }

    private String extractQueueName(String queueUrl) {
        return queueUrl.substring(queueUrl.lastIndexOf('/') + 1);
    }
}
