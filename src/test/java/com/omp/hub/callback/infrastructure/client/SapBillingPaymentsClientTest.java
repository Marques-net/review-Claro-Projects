package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsResponse;
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
class SapBillingPaymentsClientTest {

    @Mock
    private ApigeeUtils apigeeUtils;

    @InjectMocks
    private SapBillingPaymentsClient client;

    private UUID uuid;
    private SapBillingPaymentsRequest request;
    private Headers.Builder headersBuilder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "urlClient", "https://api.sap.com/billing/payments");
        uuid = UUID.randomUUID();
        request = new SapBillingPaymentsRequest();
        headersBuilder = new Headers.Builder();
    }

    @Test
    void send_WithValidRequest_ShouldReturnResponse() {
        // Given
        SapBillingPaymentsResponse expectedResponse = new SapBillingPaymentsResponse();
        Request mockRequest = new Request.Builder()
                .url("https://api.sap.com/billing/payments")
                .build();
        
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(mockRequest), anyString(), eq(SapBillingPaymentsResponse.class)))
                .thenReturn(expectedResponse);

        // When
        SapBillingPaymentsResponse result = client.send(uuid, request, headersBuilder);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeUtils).generateRequest(eq(uuid), any(GenerateRequestDTO.class));
        verify(apigeeUtils).sendRequestToApigee(eq(uuid), eq(mockRequest), 
                eq("https://api.sap.com/billing/payments"), eq(SapBillingPaymentsResponse.class));
    }
}
