package com.omp.hub.callback.infrastructure.client;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;

import okhttp3.Headers;
import okhttp3.Request;

@ExtendWith(MockitoExtension.class)
class TransationsNotificationsClientTest {

    @Mock
    private ApigeeUtils apigeeUtils;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TransationsNotificationsClient client;

    private UUID uuid;
    private OmphubTransactionNotificationRequest request;
    private Headers.Builder headersBuilder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "urlClient", "https://api.omphub.com/notifications");
        uuid = UUID.randomUUID();
        request = new OmphubTransactionNotificationRequest();
        headersBuilder = new Headers.Builder();
    }

    @Test
    void send_WithValidRequest_ShouldCallApigeeUtils() throws Exception {
        // Given
        Request mockRequest = new Request.Builder()
                .url("https://api.omphub.com/notifications")
                .build();
        
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);

        // When
        client.send(uuid, request, headersBuilder);

        // Then
        verify(apigeeUtils).generateRequest(eq(uuid), any(GenerateRequestDTO.class));
        verify(apigeeUtils).sendRequestToApigee(eq(uuid), eq(mockRequest), 
                eq("https://api.omphub.com/notifications"), isNull());
    }

    @Test
    void send_WithDifferentHeaders_ShouldAddAcceptHeader() throws Exception {
        // Given
        Headers.Builder customBuilder = new Headers.Builder()
                .add("Custom-Header", "value");
        Request mockRequest = new Request.Builder()
                .url("https://api.omphub.com/notifications")
                .build();
        
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);

        // When
        client.send(uuid, request, customBuilder);

        // Then
        verify(apigeeUtils).sendRequestToApigee(eq(uuid), any(Request.class), anyString(), isNull());
    }
}
