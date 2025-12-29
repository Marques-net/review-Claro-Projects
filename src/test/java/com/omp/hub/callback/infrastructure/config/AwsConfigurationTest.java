package com.omp.hub.callback.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import software.amazon.awssdk.services.ssm.SsmClient;

@SpringBootTest
@TestPropertySource(properties = {
        "aws.credentials.region=us-east-1",
        "aws.credentials.key=test-access-key",
        "aws.credentials.secret=test-secret-key"
})
class AwsConfigurationTest {

    @Autowired
    private SsmClient ssmClient;

    @Test
    void ssmClient_ShouldBeConfigured() {
        assertThat(ssmClient).isNotNull();
    }

    @Test
    void ssmClient_ShouldBeConfiguredWithCredentials() {
        assertThat(ssmClient).isNotNull();
        assertThat(ssmClient.serviceName()).isEqualTo("ssm");
    }
}
