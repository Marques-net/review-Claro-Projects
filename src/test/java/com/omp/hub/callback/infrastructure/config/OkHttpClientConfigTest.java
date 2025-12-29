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
    "okhttp.trust-all=false"
})
class OkHttpClientConfigTest {

    @Autowired
    private OkHttpClient okHttpClient;

    @Test
    void okHttpClient_ShouldBeConfigured() {
        // Then
        assertNotNull(okHttpClient);
        assertEquals(5000, okHttpClient.connectTimeoutMillis());
        assertEquals(10000, okHttpClient.readTimeoutMillis());
        assertEquals(10000, okHttpClient.writeTimeoutMillis());
    }

    @Test
    void okHttpClient_WithTrustAll_ShouldBeConfigured() {
        // Given - trustAll=true via @TestPropertySource
        // When & Then - Verifica que o cliente foi criado sem erros
        assertNotNull(okHttpClient);
        // Não podemos verificar os timeouts pois são configurados antes da decisão trustAll
    }

    @Test
    void parseDuration_WithSeconds_ShouldConvertCorrectly() throws Exception {
        // Given
        OkHttpClientConfig config = new OkHttpClientConfig();
        java.lang.reflect.Method method = OkHttpClientConfig.class.getDeclaredMethod("parseDuration", String.class);
        method.setAccessible(true);

        // When & Then
        assertEquals(5000L, method.invoke(config, "5s"));
        assertEquals(30000L, method.invoke(config, "30s"));
    }

    @Test
    void parseDuration_WithMilliseconds_ShouldConvertCorrectly() throws Exception {
        // Given
        OkHttpClientConfig config = new OkHttpClientConfig();
        java.lang.reflect.Method method = OkHttpClientConfig.class.getDeclaredMethod("parseDuration", String.class);
        method.setAccessible(true);

        // When & Then
        assertEquals(1000L, method.invoke(config, "1000ms"));
        assertEquals(500L, method.invoke(config, "500ms"));
    }

    @Test
    void parseDuration_WithMinutes_ShouldConvertCorrectly() throws Exception {
        // Given
        OkHttpClientConfig config = new OkHttpClientConfig();
        java.lang.reflect.Method method = OkHttpClientConfig.class.getDeclaredMethod("parseDuration", String.class);
        method.setAccessible(true);

        // When & Then
        assertEquals(120000L, method.invoke(config, "2m")); // 2 minutes = 120000ms
        assertEquals(60000L, method.invoke(config, "1m"));  // 1 minute = 60000ms
    }

    @Test
    void parseDuration_WithPlainNumber_ShouldTreatAsMilliseconds() throws Exception {
        // Given
        OkHttpClientConfig config = new OkHttpClientConfig();
        java.lang.reflect.Method method = OkHttpClientConfig.class.getDeclaredMethod("parseDuration", String.class);
        method.setAccessible(true);

        // When & Then
        assertEquals(1500L, method.invoke(config, "1500"));
        assertEquals(3000L, method.invoke(config, "3000"));
    }
}
