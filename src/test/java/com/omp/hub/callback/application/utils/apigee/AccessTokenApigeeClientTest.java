package com.omp.hub.callback.application.utils.apigee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.utils.apigee.dto.ApigeeTokenDTO;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessTokenApigeeClientTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ApigeeUtils apigeeUtils;

    @InjectMocks
    private AccessTokenApigeeClient accessTokenApigeeClient;

    private static final String APIGEE_URL = "https://api.example.com";
    private static final String APIGEE_BASIC = "Basic ABC123";
    private static final String URL_CLIENT = "/oauth/token";
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        ReflectionTestUtils.setField(accessTokenApigeeClient, "apiUrl", APIGEE_URL);
        ReflectionTestUtils.setField(accessTokenApigeeClient, "apigeeBasic", APIGEE_BASIC);
        ReflectionTestUtils.setField(accessTokenApigeeClient, "urlClient", URL_CLIENT);
    }

    @Test
    void getAccessToken_ShouldReturnToken() {
        // Arrange
        Request mockRequest = mock(Request.class);
        when(apigeeUtils.generateRequest(any(UUID.class), any(GenerateRequestDTO.class))).thenReturn(mockRequest);

        ApigeeTokenDTO expectedToken = ApigeeTokenDTO.builder()
                .access_token("test-token-123")
                .token_type("Bearer")
                .expires_in("3600")
                .build();

        when(apigeeUtils.sendRequestToApigee(any(UUID.class), eq(mockRequest), eq(URL_CLIENT), eq(ApigeeTokenDTO.class)))
                .thenReturn(expectedToken);

        // Act
        ApigeeTokenDTO result = accessTokenApigeeClient.getAccessToken(testUuid);

        // Assert
        assertNotNull(result);
        assertEquals("test-token-123", result.getAccess_token());
        assertEquals("Bearer", result.getToken_type());
        assertEquals("3600", result.getExpires_in());

        verify(apigeeUtils).generateRequest(any(UUID.class), any(GenerateRequestDTO.class));
        verify(apigeeUtils).sendRequestToApigee(any(UUID.class), eq(mockRequest), eq(URL_CLIENT), eq(ApigeeTokenDTO.class));
    }

    @Test
    void getAccessToken_ShouldUseCorrectParameters() {
        // Arrange
        Request mockRequest = mock(Request.class);
        when(apigeeUtils.generateRequest(any(UUID.class), any(GenerateRequestDTO.class))).thenReturn(mockRequest);

        ApigeeTokenDTO expectedToken = ApigeeTokenDTO.builder()
                .access_token("another-token")
                .build();
        when(apigeeUtils.sendRequestToApigee(any(UUID.class), any(Request.class), any(String.class), any()))
                .thenReturn(expectedToken);

        // Act
        ApigeeTokenDTO result = accessTokenApigeeClient.getAccessToken(testUuid);

        // Assert
        assertNotNull(result);
        verify(apigeeUtils).generateRequest(any(UUID.class), argThat(dto ->
                dto.getApiUrl().equals(URL_CLIENT) &&
                dto.getHttpVerb().equals("POST") &&
                dto.getHeaders() != null &&
                dto.getBody() != null
        ));
    }

    @Test
    void implementsAccessTokenPort() {
        // Assert
        assertTrue(accessTokenApigeeClient instanceof AccessTokenPort);
    }
}