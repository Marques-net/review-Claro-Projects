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
        "aws.credentials.key=",
        "aws.credentials.secret="
})
class AwsConfigurationWithoutCredentialsTest {

    @Autowired
    private SsmClient ssmClient;

    @Test
    void ssmClient_ShouldBeConfiguredWithoutCredentials_WhenKeysAreEmpty() {
        assertThat(ssmClient).isNotNull();
        assertThat(ssmClient.serviceName()).isEqualTo("ssm");
    }
}
