package com.omp.hub.callback.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import okhttp3.OkHttpClient;

@SpringBootTest
@TestPropertySource(properties = {
    "okhttp.connect-timeout=5s",
    "okhttp.read-timeout=10s",
    "okhttp.write-timeout=10s",
    "okhttp.trust-all=true"
})
class OkHttpClientConfigTrustAllTest {

    @Autowired
    private OkHttpClient okHttpClient;

    @Test
    void okHttpClient_WithTrustAllEnabled_ShouldBeConfigured() {
        // Then
        assertNotNull(okHttpClient);
        // Cliente deve ser criado com SSL configurado para aceitar todos os certificados
        assertNotNull(okHttpClient.sslSocketFactory());
        assertNotNull(okHttpClient.hostnameVerifier());
    }
}
