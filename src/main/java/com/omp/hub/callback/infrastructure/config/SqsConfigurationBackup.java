package com.omp.hub.callback.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

@Configuration
@Slf4j
@Profile("sqs-backup")
public class SqsConfigurationBackup {

    @Value("${aws.credentials.region}")
    private String awsRegion;

    @Value("${aws.credentials.key}")
    private String accessKeyId;

    @Value("${aws.credentials.secret}")
    private String secretKey;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Bean
    public SqsClient sqsClientBackup() {
        String key = (accessKeyId == null || accessKeyId.trim().isEmpty()) ? "test" : accessKeyId;
        String secret = (secretKey == null || secretKey.trim().isEmpty()) ? "test" : secretKey;
        
        log.info("Configurando SQS Client BACKUP - Region: {}, AccessKey: {}****, QueueUrl: {}", 
                awsRegion, 
                key.length() > 4 ? key.substring(0, 4) : "****",
                queueUrl);
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(key, secret);

        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials));

        if (queueUrl.startsWith("http://localhost")) {
            builder.endpointOverride(java.net.URI.create("http://localhost:5001"));
            log.info("Configurando endpoint local para SQS: http://localhost:5001");
        }

        return builder.build();
    }
}