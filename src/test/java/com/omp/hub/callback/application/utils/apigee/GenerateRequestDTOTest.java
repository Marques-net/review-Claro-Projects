package com.omp.hub.callback.application.utils.apigee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateRequestDTOTest {

    @Test
    void testBuilderAllFields() {
        // Given
        String apiUrl = "http://localhost:8080/api/test";
        String httpVerb = "POST";
        Object body = new Object();
        okhttp3.Headers headers = okhttp3.Headers.of("Content-Type", "application/json");

        // When
        GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                .apiUrl(apiUrl)
                .httpVerb(httpVerb)
                .body(body)
                .headers(headers)
                .build();

        // Then
        assertThat(dto.getApiUrl()).isEqualTo(apiUrl);
        assertThat(dto.getHttpVerb()).isEqualTo(httpVerb);
        assertThat(dto.getBody()).isEqualTo(body);
        assertThat(dto.getHeaders()).isEqualTo(headers);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "GET", "PUT", "PATCH", "DELETE"})
    void testHttpVerbVariations(String httpVerb) {
        // Given & When
        GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                .apiUrl("http://localhost:8080/api")
                .httpVerb(httpVerb)
                .body(new Object())
                .build();

        // Then
        assertThat(dto.getHttpVerb()).isEqualTo(httpVerb);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:8080/api/v1",
            "https://api.example.com/v2/endpoint",
            "http://internal-server:9090/test",
            "https://secure.domain.org/api"
    })
    void testVariousApiUrls(String apiUrl) {
        // Given & When
        GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                .apiUrl(apiUrl)
                .httpVerb("GET")
                .build();

        // Then
        assertThat(dto.getApiUrl()).isEqualTo(apiUrl);
    }

    @Test
    void testSetters() {
        // Given
        GenerateRequestDTO<Object> dto = new GenerateRequestDTO<>();

        // When
        dto.setApiUrl("http://example.com");
        dto.setHttpVerb("PUT");
        dto.setBody(new Object());

        // Then
        assertThat(dto.getApiUrl()).isEqualTo("http://example.com");
        assertThat(dto.getHttpVerb()).isEqualTo("PUT");
        assertThat(dto.getBody()).isNotNull();
    }

    @Test
    void testNullHeaders() {
        // Given & When
        GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                .apiUrl("http://localhost:8080/api")
                .httpVerb("POST")
                .headers(null)
                .build();

        // Then
        assertThat(dto.getHeaders()).isNull();
    }

    @Test
    void testNullBody() {
        // Given & When
        GenerateRequestDTO<?> dto = GenerateRequestDTO.builder()
                .apiUrl("http://localhost:8080/api")
                .httpVerb("GET")
                .body(null)
                .build();

        // Then
        assertThat(dto.getBody()).isNull();
    }
}
