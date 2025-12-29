package com.omp.hub.callback.application.utils.apigee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestUtilsTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private OkHttpClient okHttpClient;

    @InjectMocks
    private RequestUtils requestUtils;

    private GenerateRequestDTO<?> generateRequestDTO;

    @BeforeEach
    void setUp() {
        generateRequestDTO = GenerateRequestDTO.builder()
            .apiUrl("http://localhost:8080/api/test")
            .body(new Object())
            .headers(Headers.of("Content-Type", "application/json"))
            .httpVerb("POST")
            .build();
    }

    @Test
    void generateRequest_WithPOST_ShouldReturnPostRequest() throws IOException {
        // Given
        generateRequestDTO.setHttpVerb("POST");
        when(mapper.writeValueAsString(generateRequestDTO.getBody())).thenReturn("{}");

        // When
        Request request = requestUtils.generateRequest(generateRequestDTO);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.url().toString()).isEqualTo("http://localhost:8080/api/test");
        assertThat(request.method()).isEqualTo("POST");
    }

    @Test
    void generateRequest_WithGET_ShouldReturnGetRequest() throws IOException {
        // Given
        generateRequestDTO.setHttpVerb("GET");
        when(mapper.writeValueAsString(generateRequestDTO.getBody())).thenReturn("{}");

        // When
        Request request = requestUtils.generateRequest(generateRequestDTO);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.url().toString()).isEqualTo("http://localhost:8080/api/test");
        assertThat(request.method()).isEqualTo("GET");
    }

    @Test
    void generateRequest_WithPUT_ShouldReturnPutRequest() throws IOException {
        // Given
        generateRequestDTO.setHttpVerb("PUT");
        when(mapper.writeValueAsString(generateRequestDTO.getBody())).thenReturn("{}");

        // When
        Request request = requestUtils.generateRequest(generateRequestDTO);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.url().toString()).isEqualTo("http://localhost:8080/api/test");
        assertThat(request.method()).isEqualTo("PUT");
    }

    @Test
    void generateRequest_WithPATCH_ShouldReturnPatchRequest() throws IOException {
        // Given
        generateRequestDTO.setHttpVerb("PATCH");
        when(mapper.writeValueAsString(generateRequestDTO.getBody())).thenReturn("{}");

        // When
        Request request = requestUtils.generateRequest(generateRequestDTO);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.url().toString()).isEqualTo("http://localhost:8080/api/test");
        assertThat(request.method()).isEqualTo("PATCH");
    }

    @Test
    void generateRequest_WithDELETE_ShouldReturnDeleteRequest() throws IOException {
        // Given
        generateRequestDTO.setHttpVerb("DELETE");
        when(mapper.writeValueAsString(generateRequestDTO.getBody())).thenReturn("{}");

        // When
        Request request = requestUtils.generateRequest(generateRequestDTO);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.url().toString()).isEqualTo("http://localhost:8080/api/test");
        assertThat(request.method()).isEqualTo("DELETE");
    }

    @Test
    void generateRequest_WithUnsupportedVerb_ShouldReturnNull() throws IOException {
        // Given
        generateRequestDTO.setHttpVerb("UNSUPPORTED");
        when(mapper.writeValueAsString(generateRequestDTO.getBody())).thenReturn("{}");

        // When
        Request request = requestUtils.generateRequest(generateRequestDTO);

        // Then
        assertThat(request).isNull();
    }

    @Test
    void generateRequest_WithNullHeaders_ShouldSetDefaultHeaders() throws IOException {
        // Given
        generateRequestDTO.setHeaders(null);
        when(mapper.writeValueAsString(generateRequestDTO.getBody())).thenReturn("{}");

        // When
        Request request = requestUtils.generateRequest(generateRequestDTO);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.header("Content-Type")).isEqualTo("application/json");
    }

    @Test
    void sendRequest_WithNullRequest_ShouldReturnNull() {
        // When
        String result = requestUtils.sendRequest(null, "http://test.com", String.class);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void generateRequest_WithVariousUrls() throws IOException {
        // Given
        String[] urls = {
            "https://api.example.com/v1/endpoint",
            "http://localhost:9090/test",
            "https://internal-server/api/v2/resource"
        };

        for (String url : urls) {
            generateRequestDTO.setApiUrl(url);
            generateRequestDTO.setHttpVerb("GET");
            when(mapper.writeValueAsString(generateRequestDTO.getBody())).thenReturn("{}");

            // When
            Request request = requestUtils.generateRequest(generateRequestDTO);

            // Then
            assertThat(request).isNotNull();
            assertThat(request.url().toString()).isEqualTo(url);
        }
    }

    @Test
    void generateRequest_WithDifferentHeaderCombinations() throws IOException {
        // Given
        Headers headers1 = Headers.of("X-Custom-Header", "value1");
        Headers headers2 = Headers.of("Authorization", "Bearer token123");

        generateRequestDTO.setHttpVerb("POST");
        when(mapper.writeValueAsString(generateRequestDTO.getBody())).thenReturn("{}");

        // When
        generateRequestDTO.setHeaders(headers1);
        Request request1 = requestUtils.generateRequest(generateRequestDTO);

        generateRequestDTO.setHeaders(headers2);
        Request request2 = requestUtils.generateRequest(generateRequestDTO);

        // Then
        assertThat(request1).isNotNull();
        assertThat(request2).isNotNull();
        assertThat(request1.header("X-Custom-Header")).isEqualTo("value1");
        assertThat(request2.header("Authorization")).isEqualTo("Bearer token123");
    }

    @Test
    void generateRequest_WithMultipleHttpVerbs() throws IOException {
        // Given
        String[] verbs = {"POST", "GET", "PUT", "PATCH", "DELETE"};
        String[] expectedMethods = {"POST", "GET", "PUT", "PATCH", "DELETE"};

        for (int i = 0; i < verbs.length; i++) {
            generateRequestDTO.setHttpVerb(verbs[i]);
            when(mapper.writeValueAsString(generateRequestDTO.getBody())).thenReturn("{}");

            // When
            Request request = requestUtils.generateRequest(generateRequestDTO);

            // Then
            assertThat(request).isNotNull();
            assertThat(request.method()).isEqualTo(expectedMethods[i]);
        }
    }

    @Test
    void sendRequest_WithSuccessfulResponse_ShouldReturnParsedObject() throws IOException {
        // Given
        Request request = new Request.Builder()
            .url("http://test.com")
            .build();
        
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        Call call = mock(Call.class);
        
        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("{\"message\":\"success\"}");
        when(mapper.readValue(anyString(), eq(TestResponse.class))).thenReturn(new TestResponse("success"));

        // When
        TestResponse result = requestUtils.sendRequest(request, "http://test.com", TestResponse.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("success");
    }

    @Test
    void sendRequest_WithEmptyResponse_ShouldReturnNull() throws IOException {
        // Given
        Request request = new Request.Builder()
            .url("http://test.com")
            .build();
        
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        Call call = mock(Call.class);
        
        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("");

        // When
        TestResponse result = requestUtils.sendRequest(request, "http://test.com", TestResponse.class);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void sendRequest_WithUnsuccessfulResponse_ShouldThrowBusinessException() throws IOException {
        // Given
        Request request = new Request.Builder()
            .url("http://test.com")
            .build();
        
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);
        Call call = mock(Call.class);
        ErrorResponse errorResponse = ErrorResponse.builder()
            .message("Error occurred")
            .status(400)
            .build();
        
        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.isSuccessful()).thenReturn(false);
        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenReturn("{\"message\":\"Error occurred\",\"status\":400}");
        when(mapper.readValue(anyString(), eq(ErrorResponse.class))).thenReturn(errorResponse);

        // When/Then
        assertThatThrownBy(() -> requestUtils.sendRequest(request, "http://test.com", TestResponse.class))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void sendRequest_WithConnectException_ShouldThrowBusinessExceptionWithBadGateway() throws IOException {
        // Given
        Request request = new Request.Builder()
            .url("http://test.com")
            .build();
        
        Call call = mock(Call.class);
        
        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenThrow(new ConnectException("Connection refused"));

        // When/Then
        assertThatThrownBy(() -> requestUtils.sendRequest(request, "http://test.com", TestResponse.class))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Erro de conexão");
    }

    @Test
    void sendRequest_WithSocketTimeoutException_ShouldThrowBusinessExceptionWithTimeout() throws IOException {
        // Given
        Request request = new Request.Builder()
            .url("http://test.com")
            .build();
        
        Call call = mock(Call.class);
        
        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenThrow(new SocketTimeoutException("Connection timeout"));

        // When/Then
        assertThatThrownBy(() -> requestUtils.sendRequest(request, "http://test.com", TestResponse.class))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Erro de conexão");
    }

    @Test
    void sendRequest_WithGenericException_ShouldThrowBusinessException() throws IOException {
        // Given
        Request request = new Request.Builder()
            .url("http://test.com")
            .build();
        
        Call call = mock(Call.class);
        
        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenThrow(new RuntimeException("Unexpected error"));

        // When/Then
        assertThatThrownBy(() -> requestUtils.sendRequest(request, "http://test.com", TestResponse.class))
            .isInstanceOf(BusinessException.class)
            .hasCauseInstanceOf(RuntimeException.class);
    }

    // Classe auxiliar para testes
    private static class TestResponse {
        private String message;

        public TestResponse() {}

        public TestResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
