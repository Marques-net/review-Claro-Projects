package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsResponse;
import okhttp3.Headers;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SapPaymentsClientTest {

    @Mock
    private ApigeeUtils apigeeUtils;

    @InjectMocks
    private SapPaymentsClient client;

    private UUID uuid;
    private SapPaymentsRequest request;
    private Headers.Builder headersBuilder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "urlClient", "https://api.sap.com/payments");
        uuid = UUID.randomUUID();
        request = new SapPaymentsRequest();
        headersBuilder = new Headers.Builder();
    }

    @Test
    void send_WithValidRequest_ShouldReturnResponse() {
        // Given
        SapPaymentsResponse expectedResponse = new SapPaymentsResponse();
        Request mockRequest = new Request.Builder()
                .url("https://api.sap.com/payments")
                .build();
        
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(mockRequest), anyString(), eq(SapPaymentsResponse.class)))
                .thenReturn(expectedResponse);

        // When
        SapPaymentsResponse result = client.send(uuid, request, headersBuilder);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeUtils).generateRequest(eq(uuid), any(GenerateRequestDTO.class));
        verify(apigeeUtils).sendRequestToApigee(eq(uuid), eq(mockRequest), 
                eq("https://api.sap.com/payments"), eq(SapPaymentsResponse.class));
    }
}
