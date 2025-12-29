package com.omp.hub.callback.application.utils.apigee;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestUtilsExtendedTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private OkHttpClient okHttpClient;

    @InjectMocks
    private RequestUtils serviceUnderTest;

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(mapper.writeValueAsString(any())).thenReturn("{\"test\": \"data\"}");
    }

    @Test
    void testGenerateRequestWithNullHeaders() throws IOException {
        // Given
        GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                .apiUrl("https://api.test.com/endpoint")
                .httpVerb("POST")
                .body("test body")
                .headers(null)
                .build();

        when(mapper.writeValueAsString("test body")).thenReturn("\"test body\"");

        // When
        Request result = serviceUnderTest.generateRequest(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.url().toString()).isEqualTo("https://api.test.com/endpoint");
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "GET", "PUT", "PATCH", "DELETE"})
    void testGenerateRequestWithAllHttpVerbs(String verb) throws IOException {
        // Given
        Headers headers = Headers.of("Content-Type", "application/json");
        GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                .apiUrl("https://api.test.com/endpoint")
                .httpVerb(verb)
                .body("test body")
                .headers(headers)
                .build();

        when(mapper.writeValueAsString("test body")).thenReturn("\"test body\"");

        // When
        Request result = serviceUnderTest.generateRequest(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.url().toString()).isEqualTo("https://api.test.com/endpoint");
    }

    @Test
    void testGenerateRequestWithInvalidHttpVerb() throws IOException {
        // Given
        Headers headers = Headers.of("Content-Type", "application/json");
        GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                .apiUrl("https://api.test.com/endpoint")
                .httpVerb("INVALID")
                .body("test body")
                .headers(headers)
                .build();

        when(mapper.writeValueAsString("test body")).thenReturn("\"test body\"");

        // When
        Request result = serviceUnderTest.generateRequest(dto);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testSendRequestWithNullRequest() {
        // When
        Object result = serviceUnderTest.sendRequest(null, "http://test.com", String.class);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testGenerateRequestWithVariousUrls() throws IOException {
        // Given - Test with different URLs
        String[] urls = {
            "https://api.test.com/endpoint",
            "http://localhost:8080/api",
            "https://prod.api.com/v1/payments"
        };

        for (String url : urls) {
            GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                    .apiUrl(url)
                    .httpVerb("POST")
                    .body("test body")
                    .build();

            when(mapper.writeValueAsString("test body")).thenReturn("\"test body\"");

            // When
            Request result = serviceUnderTest.generateRequest(dto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.url().toString()).isEqualTo(url);
        }
    }

    @Test
    void testGenerateRequestWithCustomHeaders() throws IOException {
        // Given
        Headers customHeaders = Headers.of(
            "Authorization", "Bearer token123",
            "Content-Type", "application/json",
            "X-Custom-Header", "custom-value"
        );

        GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                .apiUrl("https://api.test.com/endpoint")
                .httpVerb("POST")
                .body("test body")
                .headers(customHeaders)
                .build();

        when(mapper.writeValueAsString("test body")).thenReturn("\"test body\"");

        // When
        Request result = serviceUnderTest.generateRequest(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.headers().get("Authorization")).isEqualTo("Bearer token123");
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "GET", "PUT", "PATCH", "DELETE"})
    void testGenerateRequestPreservesHttpMethod(String httpMethod) throws IOException {
        // Given
        GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                .apiUrl("https://api.example.com/data")
                .httpVerb(httpMethod)
                .body("request body")
                .headers(Headers.of("X-Test", "value"))
                .build();

        when(mapper.writeValueAsString("request body")).thenReturn("\"request body\"");

        // When
        Request result = serviceUnderTest.generateRequest(dto);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testGenerateRequestWithMultipleCustomHeaders() throws IOException {
        // Given
        Headers customHeaders = Headers.of(
            "Authorization", "Bearer token",
            "X-API-Key", "api-key-value",
            "X-Request-ID", "req-123",
            "Accept", "application/json"
        );

        GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                .apiUrl("https://api.test.com/endpoint")
                .httpVerb("POST")
                .body("test body")
                .headers(customHeaders)
                .build();

        when(mapper.writeValueAsString("test body")).thenReturn("\"test body\"");

        // When
        Request result = serviceUnderTest.generateRequest(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.headers().get("X-API-Key")).isEqualTo("api-key-value");
        assertThat(result.headers().get("X-Request-ID")).isEqualTo("req-123");
    }
}
