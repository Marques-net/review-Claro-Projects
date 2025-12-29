package com.omp.hub.callback.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

@Configuration
public class AwsConfiguration {
    @Value("${aws.credentials.region}")
    private String awsRegion;

    @Value("${aws.credentials.key}")
    private String accessKeyId;

    @Value("${aws.credentials.secret}")
    private String secretKey;

    @Bean
    public SsmClient ssmClient() {
        if (!accessKeyId.isEmpty() && !secretKey.isEmpty()) {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretKey);

            return SsmClient.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build();
        }

        return SsmClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

}
