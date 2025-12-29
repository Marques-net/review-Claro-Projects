package com.omp.hub.callback.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.services.sqs.SqsClient;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("sqs-backup")
class SqsConfigurationBackupTest {

    @Test
    void should_CreateSqsClientBackup_When_ValidCredentialsProvided() {
        // Given
        SqsConfigurationBackup sqsConfigurationBackup = new SqsConfigurationBackup();
        ReflectionTestUtils.setField(sqsConfigurationBackup, "awsRegion", "us-east-1");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "accessKeyId", "AKIAIOSFODNN7EXAMPLE");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "secretKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "queueUrl", "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue");

        // When
        SqsClient result = sqsConfigurationBackup.sqsClientBackup();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SqsClient.class);
    }

    @Test
    void should_CreateSqsClientBackup_When_NullCredentialsProvided() {
        // Given
        SqsConfigurationBackup sqsConfigurationBackup = new SqsConfigurationBackup();
        ReflectionTestUtils.setField(sqsConfigurationBackup, "awsRegion", "us-east-1");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "accessKeyId", null);
        ReflectionTestUtils.setField(sqsConfigurationBackup, "secretKey", null);
        ReflectionTestUtils.setField(sqsConfigurationBackup, "queueUrl", "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue");

        // When
        SqsClient result = sqsConfigurationBackup.sqsClientBackup();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SqsClient.class);
    }

    @Test
    void should_CreateSqsClientBackup_When_EmptyCredentialsProvided() {
        // Given
        SqsConfigurationBackup sqsConfigurationBackup = new SqsConfigurationBackup();
        ReflectionTestUtils.setField(sqsConfigurationBackup, "awsRegion", "us-east-1");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "accessKeyId", "");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "secretKey", "");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "queueUrl", "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue");

        // When
        SqsClient result = sqsConfigurationBackup.sqsClientBackup();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SqsClient.class);
    }

    @Test
    void should_CreateSqsClientBackup_When_WhitespaceCredentialsProvided() {
        // Given
        SqsConfigurationBackup sqsConfigurationBackup = new SqsConfigurationBackup();
        ReflectionTestUtils.setField(sqsConfigurationBackup, "awsRegion", "us-east-1");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "accessKeyId", "   ");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "secretKey", "   ");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "queueUrl", "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue");

        // When
        SqsClient result = sqsConfigurationBackup.sqsClientBackup();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SqsClient.class);
    }

    @Test
    void should_CreateSqsClientBackup_When_LocalhostQueueUrlProvided() {
        // Given
        SqsConfigurationBackup sqsConfigurationBackup = new SqsConfigurationBackup();
        ReflectionTestUtils.setField(sqsConfigurationBackup, "awsRegion", "us-east-1");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "accessKeyId", "AKIAIOSFODNN7EXAMPLE");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "secretKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "queueUrl", "http://localhost:5001/test-queue");

        // When
        SqsClient result = sqsConfigurationBackup.sqsClientBackup();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SqsClient.class);
    }

    @Test
    void should_CreateSqsClientBackup_When_DifferentRegionProvided() {
        // Given
        SqsConfigurationBackup sqsConfigurationBackup = new SqsConfigurationBackup();
        ReflectionTestUtils.setField(sqsConfigurationBackup, "awsRegion", "sa-east-1");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "accessKeyId", "AKIAIOSFODNN7EXAMPLE");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "secretKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "queueUrl", "https://sqs.sa-east-1.amazonaws.com/123456789012/test-queue");

        // When
        SqsClient result = sqsConfigurationBackup.sqsClientBackup();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SqsClient.class);
    }

    @Test
    void should_CreateSqsClientBackup_When_ShortAccessKeyProvided() {
        // Given
        SqsConfigurationBackup sqsConfigurationBackup = new SqsConfigurationBackup();
        ReflectionTestUtils.setField(sqsConfigurationBackup, "awsRegion", "us-east-1");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "accessKeyId", "ABC");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "secretKey", "secretValue");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "queueUrl", "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue");

        // When
        SqsClient result = sqsConfigurationBackup.sqsClientBackup();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SqsClient.class);
    }

    @Test
    void should_CreateSqsClientBackup_When_LocalhostWithPortProvided() {
        // Given
        SqsConfigurationBackup sqsConfigurationBackup = new SqsConfigurationBackup();
        ReflectionTestUtils.setField(sqsConfigurationBackup, "awsRegion", "us-east-1");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "accessKeyId", "testKey");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "secretKey", "testSecret");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "queueUrl", "http://localhost:9324/queue/test");

        // When
        SqsClient result = sqsConfigurationBackup.sqsClientBackup();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SqsClient.class);
    }

    @Test
    void should_CreateSqsClientBackup_When_AWSQueueUrlProvided() {
        // Given
        SqsConfigurationBackup sqsConfigurationBackup = new SqsConfigurationBackup();
        ReflectionTestUtils.setField(sqsConfigurationBackup, "awsRegion", "eu-west-1");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "accessKeyId", "AKIAIOSFODNN7EXAMPLE");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "secretKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        ReflectionTestUtils.setField(sqsConfigurationBackup, "queueUrl", "https://sqs.eu-west-1.amazonaws.com/987654321098/production-queue");

        // When
        SqsClient result = sqsConfigurationBackup.sqsClientBackup();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SqsClient.class);
    }
}