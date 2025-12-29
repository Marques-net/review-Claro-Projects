package com.omp.hub.callback.application.utils.apigee;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApigeeUtilsTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private OkHttpClient okHttpClient;

    @InjectMocks
    private ApigeeUtils apigeeUtils;

    private UUID uuid;
    private String apigeeUrl;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        apigeeUrl = "https://api.apigee.com";
        ReflectionTestUtils.setField(apigeeUtils, "apigeeUrl", apigeeUrl);
    }

    @Test
    void generateRequest_WithPostMethod_ShouldCreatePostRequest() throws IOException {
        // Given
        TestRequestBody testBody = new TestRequestBody("test data");
        GenerateRequestDTO<Object> dto = GenerateRequestDTO.builder()
                .apiUrl("/test/endpoint")
                .httpVerb("POST")
                .body(testBody)
                .headers(Headers.of("Content-Type", "application/json"))
                .build();

        // O mapper é chamado duas vezes: uma vez para criar o corpo da requisição e outra para fins de log
        when(mapper.writeValueAsString(testBody)).thenReturn("{\"data\":\"test data\"}");

        // When
        Request result = apigeeUtils.generateRequest(uuid, dto);

        // Then
        assertNotNull(result);
        assertEquals("POST", result.method());
        assertEquals(apigeeUrl + "/test/endpoint", result.url().toString());
        verify(mapper, times(2)).writeValueAsString(testBody); // Chamado duas vezes: body + log
        }

        @Test
        void generateRequest_WithGetMethod_ShouldCreateGetRequest() throws IOException {
        // Given
        GenerateRequestDTO<Object> dto = GenerateRequestDTO.builder()
            .apiUrl("/test/endpoint")
            .httpVerb("GET")
            .headers(Headers.of("Accept", "application/json"))
            .build();

        when(mapper.writeValueAsString(null)).thenReturn("");

        // When
        Request result = apigeeUtils.generateRequest(uuid, dto);

        // Then
        assertNotNull(result);
        assertEquals("GET", result.method());
        assertEquals(apigeeUrl + "/test/endpoint", result.url().toString());
        }

        @Test
        void generateRequest_WithPutMethod_ShouldCreatePutRequest() throws IOException {
        // Given
        TestRequestBody testBody = new TestRequestBody("updated data");
        GenerateRequestDTO<Object> dto = GenerateRequestDTO.builder()
            .apiUrl("/test/endpoint")
            .httpVerb("PUT")
            .body(testBody)
            .headers(Headers.of("Content-Type", "application/json"))
            .build();

        when(mapper.writeValueAsString(testBody)).thenReturn("{\"data\":\"updated data\"}");

        // When
        Request result = apigeeUtils.generateRequest(uuid, dto);

        // Then
        assertNotNull(result);
        assertEquals("PUT", result.method());
        }

        @Test
        void generateRequest_WithPatchMethod_ShouldCreatePatchRequest() throws IOException {
        // Given
        TestRequestBody testBody = new TestRequestBody("patched data");
        GenerateRequestDTO<Object> dto = GenerateRequestDTO.builder()
            .apiUrl("/test/endpoint")
            .httpVerb("PATCH")
            .body(testBody)
            .headers(Headers.of("Content-Type", "application/json"))
            .build();

        when(mapper.writeValueAsString(testBody)).thenReturn("{\"data\":\"patched data\"}");

        // When
        Request result = apigeeUtils.generateRequest(uuid, dto);

        // Then
        assertNotNull(result);
        assertEquals("PATCH", result.method());
        }

        @Test
        void generateRequest_WithDeleteMethod_ShouldCreateGetRequest() throws IOException {
        // Given - DELETE nesta implementação na verdade cria uma requisição GET
        GenerateRequestDTO<Object> dto = GenerateRequestDTO.builder()
            .apiUrl("/test/endpoint")
            .httpVerb("DELETE")
            .headers(Headers.of("Accept", "application/json"))
            .build();

        when(mapper.writeValueAsString(null)).thenReturn("");

        // When
        Request result = apigeeUtils.generateRequest(uuid, dto);

        // Then
        assertNotNull(result);
        assertEquals("GET", result.method());
        }

        @Test
        void generateRequest_WhenMapperThrowsRuntimeException_ShouldPropagateException() throws IOException {
        // Given
        TestRequestBody testBody = new TestRequestBody("test");
        GenerateRequestDTO<Object> dto = GenerateRequestDTO.builder()
            .apiUrl("/test/endpoint")
            .httpVerb("POST")
            .body(testBody)
            .headers(Headers.of("Content-Type", "application/json"))
            .build();

        when(mapper.writeValueAsString(any())).thenThrow(new RuntimeException("Erro de serialização"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            apigeeUtils.generateRequest(uuid, dto);
        });
        }

        @Test
        void sendRequestToApigee_WithSuccessfulResponse_ShouldReturnMappedObject() throws IOException {
        // Given
        Request request = new Request.Builder().url("http://test.com").build();
        Response response = createMockResponse(200, "{\"status\":\"success\"}");
        Call call = mock(Call.class);

        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(mapper.readValue(anyString(), eq(TestResponse.class)))
            .thenReturn(new TestResponse("success"));

        // When
        TestResponse result = apigeeUtils.sendRequestToApigee(uuid, request, "http://test.com", TestResponse.class);

        // Then
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        }

        @Test
        void sendRequestToApigee_WithEmptyResponse_ShouldReturnNull() throws IOException {
        // Given
        Request request = new Request.Builder().url("http://test.com").build();
        Response response = createMockResponse(200, "");
        Call call = mock(Call.class);

        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenReturn(response);

        // When
        TestResponse result = apigeeUtils.sendRequestToApigee(uuid, request, "http://test.com", TestResponse.class);

        // Then
        assertNull(result);
        }

        @Test
        void sendRequestToApigee_WithNullRequest_ShouldReturnNull() {
        // When
        TestResponse result = apigeeUtils.sendRequestToApigee(uuid, null, "http://test.com", TestResponse.class);

        // Then
        assertNull(result);
        verifyNoInteractions(okHttpClient);
        }

        @Test
        void sendRequestToApigee_WithConnectException_ShouldThrowBusinessException() throws IOException {
        // Given
        Request request = new Request.Builder().url("http://test.com").build();
        Call call = mock(Call.class);

        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenThrow(new ConnectException("Conexão recusada"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            apigeeUtils.sendRequestToApigee(uuid, request, "http://test.com", TestResponse.class);
        });

        assertEquals("Erro de conexão", exception.getError().getMessage());
        assertEquals("ERROR_BAD_GATEWAY", exception.getError().getErrorCode());
        }

        @Test
        void sendRequestToApigee_WithSocketTimeoutException_ShouldThrowBusinessException() throws IOException {
        // Given
        Request request = new Request.Builder().url("http://test.com").build();
        Call call = mock(Call.class);

        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenThrow(new SocketTimeoutException("Tempo de leitura esgotado"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            apigeeUtils.sendRequestToApigee(uuid, request, "http://test.com", TestResponse.class);
        });

        assertEquals("Erro de conexão", exception.getError().getMessage());
        assertEquals("ERROR_CONNECTION_TIMEOUT", exception.getError().getErrorCode());
        }

        @Test
        void sendRequestToApigee_WithGenericException_ShouldThrowBusinessException() throws IOException {
        // Given
        Request request = new Request.Builder().url("http://test.com").build();
        Call call = mock(Call.class);

        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenThrow(new RuntimeException("Erro inesperado"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            apigeeUtils.sendRequestToApigee(uuid, request, "http://test.com", TestResponse.class);
        });

        assertEquals("Erro inesperado", exception.getCause().getMessage());
        }

        @Test
        void convertApigeeErrorToBusinessError_WithUnsuccessfulResponseAndValidBody_ShouldThrowBusinessException() throws IOException {
        // Given
        Response response = createMockResponse(400, "{\"error\":{\"message\":\"Bad Request\",\"detailedMessage\":\"Invalid input\",\"errorCode\":\"VALIDATION_ERROR\",\"httpCode\":400}}");
        String responseBody = "{\"error\":{\"message\":\"Bad Request\",\"detailedMessage\":\"Invalid input\",\"errorCode\":\"VALIDATION_ERROR\",\"httpCode\":400}}";

        ApigeeResponse<Object> apigeeResponse = new ApigeeResponse<>();
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage("Bad Request");
        errorDTO.setDetailedMessageNode(new ObjectMapper().createObjectNode().textNode("Invalid input"));
        errorDTO.setErrorCode("VALIDATION_ERROR");
        errorDTO.setHttpCode(400);
        apigeeResponse.setError(errorDTO);

        when(mapper.readValue(responseBody, ApigeeResponse.class)).thenReturn(apigeeResponse);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            apigeeUtils.convertApigeeErrorToBusinessError(response, responseBody, "http://test.com");
        });

        assertEquals("Bad Request", exception.getError().getMessage());
        assertEquals("Invalid input", exception.getError().getDetails());
        assertEquals("VALIDATION_ERROR", exception.getError().getErrorCode());
        assertEquals(400, exception.getError().getStatus());
        }

        @Test
        void convertApigeeErrorToBusinessError_WithUnsuccessfulResponseAndNullBody_ShouldThrowBusinessException() {
        // Given
        Response response = createMockResponse(500, null);
        String responseBody = null;

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            apigeeUtils.convertApigeeErrorToBusinessError(response, responseBody, "http://test.com");
        });

        assertEquals("Erro ao executar chamada: http://test.com", exception.getError().getMessage());
        assertEquals("Recebemos um erro 500 porém não recebemos nenhum body no response", exception.getError().getDetails());
        assertEquals("ERROR_NO_BODY_RESPONSE", exception.getError().getErrorCode());
        assertEquals(500, exception.getError().getStatus());
        }

        @Test
        void convertApigeeErrorToBusinessError_WithUnsuccessfulResponseAndEmptyBody_ShouldThrowBusinessException() {
        // Given
        Response response = createMockResponse(404, "");
        String responseBody = "";

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            apigeeUtils.convertApigeeErrorToBusinessError(response, responseBody, "http://test.com");
        });

        assertEquals("Erro ao executar chamada: http://test.com", exception.getError().getMessage());
        assertEquals("Recebemos um erro 404 porém não recebemos nenhum body no response", exception.getError().getDetails());
        assertEquals("ERROR_NO_BODY_RESPONSE", exception.getError().getErrorCode());
        assertEquals(404, exception.getError().getStatus());
        }

        @Test
        void generateRequest_WithRequestBodyInstance_ShouldUseExistingRequestBody() throws IOException {
        // Given
        RequestBody existingBody = RequestBody.create("existing body", MediaType.parse("application/json"));
        GenerateRequestDTO<RequestBody> dto = GenerateRequestDTO.<RequestBody>builder()
            .apiUrl("/test/endpoint")
            .httpVerb("POST")
            .body(existingBody)
            .headers(Headers.of("Content-Type", "application/json"))
            .build();

        // O mapper é chamado para fins de log, mesmo quando o body já é um RequestBody
        when(mapper.writeValueAsString(existingBody)).thenReturn("existing body");

        // When
        Request result = apigeeUtils.generateRequest(uuid, dto);

        // Then
        assertNotNull(result);
        assertEquals("POST", result.method());
        // O mapper é chamado uma vez para fins de log
        verify(mapper, times(1)).writeValueAsString(existingBody);
    }

    @Test
    void generateRequest_WithNullHeaders_ShouldSetDefaultHeaders() throws IOException {
        // Given - Versão corrigida: deve definir cabeçalhos padrão quando nulo
        GenerateRequestDTO<Object> dto = GenerateRequestDTO.builder()
                .apiUrl("/test/endpoint")
                .httpVerb("GET")
                .headers(null)  // Cabeçalhos são nulos
                .build();

        when(mapper.writeValueAsString(null)).thenReturn("");

        // When
        Request result = apigeeUtils.generateRequest(uuid, dto);

        // Then
        assertNotNull(result);
        assertEquals("GET", result.method());
        // Deve ter o cabeçalho Content-Type padrão definido
        assertEquals("application/json", result.headers().get("Content-Type"));
    }

    @Test
    void sendRequestToApigee_WithNullNameClass_ShouldReturnNull() throws IOException {
        // Given
        Request request = new Request.Builder().url("http://test.com").build();
        Response response = createMockResponse(200, "{\"status\":\"success\"}");
        Call call = mock(Call.class);

        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenReturn(response);

        // When
        Object result = apigeeUtils.sendRequestToApigee(uuid, request, "http://test.com", null);

        // Then
        assertNull(result);
        // Verifica que mapper.readValue não é chamado quando nameClass é nulo
        verify(mapper, never()).readValue(anyString(), any(Class.class));
    }

    @Test
    void generateRequest_WithUnsupportedHttpVerb_ShouldReturnNull() throws IOException {
        // Given
        GenerateRequestDTO<Object> dto = GenerateRequestDTO.builder()
                .apiUrl("/test/endpoint")
                .httpVerb("UNSUPPORTED")  // Verbo não suportado
                .headers(Headers.of("Content-Type", "application/json"))
                .build();

        when(mapper.writeValueAsString(null)).thenReturn("");

        // When
        Request result = apigeeUtils.generateRequest(uuid, dto);

        // Then
        assertNull(result);
    }

    @Test
    void sendRequestToApigee_WithNullResponseBody_ShouldHandleNullBody() throws IOException {
        // Given
        Request request = new Request.Builder().url("http://test.com").build();
        Response response = new Response.Builder()
                .request(new Request.Builder().url("http://test.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(null)  // Corpo nulo
                .build();
        Call call = mock(Call.class);

        when(okHttpClient.newCall(request)).thenReturn(call);
        when(call.execute()).thenReturn(response);

        // When
        TestResponse result = apigeeUtils.sendRequestToApigee(uuid, request, "http://test.com", TestResponse.class);

        // Then
        assertNull(result);
    }

    // Métodos e classes auxiliares
    private Response createMockResponse(int code, String body) {
        ResponseBody responseBody = body != null ? ResponseBody.create(body, MediaType.parse("application/json")) : null;
        return new Response.Builder()
                .request(new Request.Builder().url("http://test.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("OK")
                .body(responseBody)
                .build();
    }

    static class TestRequestBody {
        private String data;

        public TestRequestBody(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    static class TestResponse {
        private String status;

        public TestResponse() {}

        public TestResponse(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
