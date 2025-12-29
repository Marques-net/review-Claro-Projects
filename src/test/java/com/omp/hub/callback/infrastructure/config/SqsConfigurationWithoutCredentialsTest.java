package com.omp.hub.callback.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.services.sqs.SqsClient;

class SqsConfigurationWithoutCredentialsTest {

    private SqsConfiguration sqsConfiguration;

    @BeforeEach
    void setUp() {
        sqsConfiguration = new SqsConfiguration();
        ReflectionTestUtils.setField(sqsConfiguration, "awsRegion", "us-east-1");
        ReflectionTestUtils.setField(sqsConfiguration, "queueUrl", "https://sqs.us-east-1.amazonaws.com/123456789/test-queue.fifo");
    }

    @Test
    void sqsClient_ShouldBeCreatedWithDefaultCredentialsProvider() {
        SqsClient sqsClient = sqsConfiguration.sqsClient();

        assertThat(sqsClient).isNotNull();
        assertThat(sqsClient.serviceName()).isEqualTo("sqs");
    }
}
