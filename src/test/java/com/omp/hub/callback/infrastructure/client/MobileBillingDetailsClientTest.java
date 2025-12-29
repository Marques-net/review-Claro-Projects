package com.omp.hub.callback.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsResponse;

import okhttp3.Headers;
import okhttp3.Request;

@ExtendWith(MockitoExtension.class)
class MobileBillingDetailsClientTest {

    @Mock
    private ApigeeUtils apigeeUtils;

    @Mock
    private ApigeeHeaderService apigeeHeaderService;

    @InjectMocks
    private MobileBillingDetailsClient mobileBillingDetailsClient;

    private UUID uuid;
    private String mobileBan;
    private String urlClient;
    private String targetValue;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        mobileBan = "5511999999999";
        urlClient = "http://test.com/mobile/billing";
        targetValue = "prod01";
        
        // Configurar os valores das propriedades
        ReflectionTestUtils.setField(mobileBillingDetailsClient, "urlClient", urlClient);
        ReflectionTestUtils.setField(mobileBillingDetailsClient, "targetValue", targetValue);
    }

    @Test
    void getCustomerBillingDetailsByMobileBan_WithValidResponse_ShouldReturnMobileBillingDetailsResponse() {
        // Arrange
        MobileBillingDetailsResponse expectedResponse = new MobileBillingDetailsResponse();
        expectedResponse.setData(new com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsData());
        
        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("x-api-key", "test");
        
        Request mockRequest = new Request.Builder()
                .url("http://test.com")
                .build();

        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headerBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class)))
                .thenReturn(expectedResponse);

        // Act
        MobileBillingDetailsResponse result = mobileBillingDetailsClient.getCustomerBillingDetailsByMobileBan(uuid, mobileBan);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeHeaderService, times(1)).generateHeaderApigee(uuid);
        verify(apigeeUtils, times(1)).generateRequest(eq(uuid), any(GenerateRequestDTO.class));
        verify(apigeeUtils, times(1)).sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class));
    }

    @Test
    void getCustomerBillingDetailsByMobileBan_WithNullResponse_ShouldReturnNull() {
        // Arrange
        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("x-api-key", "test");
        
        Request mockRequest = new Request.Builder()
                .url("http://test.com")
                .build();

        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headerBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class)))
                .thenReturn(null);

        // Act
        MobileBillingDetailsResponse result = mobileBillingDetailsClient.getCustomerBillingDetailsByMobileBan(uuid, mobileBan);

        // Assert
        assertNull(result);
        verify(apigeeHeaderService, times(1)).generateHeaderApigee(uuid);
        verify(apigeeUtils, times(1)).generateRequest(eq(uuid), any(GenerateRequestDTO.class));
        verify(apigeeUtils, times(1)).sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class));
    }

    @Test
    void getCustomerBillingDetailsByMobileBan_WithException_ShouldReturnNull() {
        // Arrange
        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("x-api-key", "test");
        
        Request mockRequest = new Request.Builder()
                .url("http://test.com")
                .build();

        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headerBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class)))
                .thenThrow(new RuntimeException("API Error"));

        // Act
        MobileBillingDetailsResponse result = mobileBillingDetailsClient.getCustomerBillingDetailsByMobileBan(uuid, mobileBan);

        // Assert
        assertNull(result);
        verify(apigeeHeaderService, times(1)).generateHeaderApigee(uuid);
        verify(apigeeUtils, times(1)).generateRequest(eq(uuid), any(GenerateRequestDTO.class));
        verify(apigeeUtils, times(1)).sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class));
    }

    @Test
    void getCustomerBillingDetailsByMobileBan_WithNullTargetValue_ShouldUseDefaultTarget() {
        // Arrange
        ReflectionTestUtils.setField(mobileBillingDetailsClient, "targetValue", null);
        
        MobileBillingDetailsResponse expectedResponse = new MobileBillingDetailsResponse();
        expectedResponse.setData(new com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsData());
        
        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("x-api-key", "test");
        
        Request mockRequest = new Request.Builder()
                .url("http://test.com")
                .build();

        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headerBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class)))
                .thenReturn(expectedResponse);

        // Act
        MobileBillingDetailsResponse result = mobileBillingDetailsClient.getCustomerBillingDetailsByMobileBan(uuid, mobileBan);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeHeaderService, times(1)).generateHeaderApigee(uuid);
        verify(apigeeUtils, times(1)).generateRequest(eq(uuid), any(GenerateRequestDTO.class));
        verify(apigeeUtils, times(1)).sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class));
    }

    @Test
    void getCustomerBillingDetailsByMobileBan_WithResponseButNullData_ShouldReturnResponse() {
        // Arrange
        MobileBillingDetailsResponse expectedResponse = new MobileBillingDetailsResponse();
        expectedResponse.setData(null); // Data é null
        
        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("x-api-key", "test");
        
        Request mockRequest = new Request.Builder()
                .url("http://test.com")
                .build();

        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headerBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class)))
                .thenReturn(expectedResponse);

        // Act
        MobileBillingDetailsResponse result = mobileBillingDetailsClient.getCustomerBillingDetailsByMobileBan(uuid, mobileBan);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeHeaderService, times(1)).generateHeaderApigee(uuid);
        verify(apigeeUtils, times(1)).generateRequest(eq(uuid), any(GenerateRequestDTO.class));
        verify(apigeeUtils, times(1)).sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class));
    }

    @Test
    void getCustomerBillingDetailsByMobileBan_WithEmptyMobileBan_ShouldStillCallApi() {
        // Arrange
        String emptyMobileBan = "";
        MobileBillingDetailsResponse expectedResponse = new MobileBillingDetailsResponse();
        
        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("x-api-key", "test");
        
        Request mockRequest = new Request.Builder()
                .url("http://test.com")
                .build();

        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headerBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class)))
                .thenReturn(expectedResponse);

        // Act
        MobileBillingDetailsResponse result = mobileBillingDetailsClient.getCustomerBillingDetailsByMobileBan(uuid, emptyMobileBan);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeHeaderService, times(1)).generateHeaderApigee(uuid);
        verify(apigeeUtils, times(1)).generateRequest(eq(uuid), any(GenerateRequestDTO.class));
        verify(apigeeUtils, times(1)).sendRequestToApigee(eq(uuid), eq(mockRequest), eq(urlClient), eq(MobileBillingDetailsResponse.class));
    }
}