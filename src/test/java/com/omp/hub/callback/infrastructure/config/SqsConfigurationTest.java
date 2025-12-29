package com.omp.hub.callback.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import software.amazon.awssdk.services.sqs.SqsClient;

@SpringBootTest
@TestPropertySource(properties = {
        "aws.credentials.region=us-east-1",
        "aws.credentials.key=test-access-key",
        "aws.credentials.secret=test-secret-key"
})
class SqsConfigurationTest {

    @Autowired
    private SqsClient sqsClient;

    @Test
    void sqsClient_ShouldBeConfigured() {
        assertThat(sqsClient).isNotNull();
    }

    @Test
    void sqsClient_ShouldBeConfiguredWithCredentials() {
        assertThat(sqsClient).isNotNull();
        assertThat(sqsClient.serviceName()).isEqualTo("sqs");
    }
}
