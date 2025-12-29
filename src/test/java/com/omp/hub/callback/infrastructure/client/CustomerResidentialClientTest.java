package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.domain.model.dto.customer.residential.CustomerResidentialResponse;
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
class CustomerResidentialClientTest {

    @Mock
    private ApigeeUtils apigeeUtils;

    @Mock
    private ApigeeHeaderService apigeeHeaderService;

    @InjectMocks
    private CustomerResidentialClient client;

    private UUID uuid;
    private String phoneNumber;
    private String document;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        phoneNumber = "11999999999";
        document = "12345678900";
        
        // Inject @Value property
        ReflectionTestUtils.setField(client, "urlClient", "https://api.claro.com/customer/residential");
    }

    @Test
    void getCustomerContractsByPhoneNumber_WithValidPhoneNumber_ShouldReturnResponse() {
        // Given
        CustomerResidentialResponse expectedResponse = new CustomerResidentialResponse();
        Request request = new Request.Builder().url("https://api.claro.com").build();
        
        Headers.Builder headersBuilder = new Headers.Builder();
        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headersBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any())).thenReturn(request);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerResidentialResponse.class)))
                .thenReturn(expectedResponse);

        // When
        CustomerResidentialResponse result = client.getCustomerContractsByPhoneNumber(uuid, phoneNumber);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeUtils).generateRequest(eq(uuid), any());
        verify(apigeeUtils).sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerResidentialResponse.class));
    }

    @Test
    void getCustomerContractsByDocument_WithValidDocument_ShouldReturnResponse() {
        // Given
        CustomerResidentialResponse expectedResponse = new CustomerResidentialResponse();
        Request request = new Request.Builder().url("https://api.claro.com").build();
        
        Headers.Builder headersBuilder = new Headers.Builder();
        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headersBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any())).thenReturn(request);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerResidentialResponse.class)))
                .thenReturn(expectedResponse);

        // When
        CustomerResidentialResponse result = client.getCustomerContractsByDocument(uuid, document);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeUtils).generateRequest(eq(uuid), any());
        verify(apigeeUtils).sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerResidentialResponse.class));
    }

    @Test
    void getCustomerContractsByPhoneNumber_WhenExceptionOccurs_ShouldPropagateException() {
        // Given
        Headers.Builder headersBuilder = new Headers.Builder();
        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headersBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any())).thenThrow(new RuntimeException("Connection error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            client.getCustomerContractsByPhoneNumber(uuid, phoneNumber);
        });

        verify(apigeeUtils).generateRequest(eq(uuid), any());
    }

    @Test
    void getCustomerContractsByDocument_WhenExceptionOccurs_ShouldPropagateException() {
        // Given
        Headers.Builder headersBuilder = new Headers.Builder();
        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headersBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any())).thenThrow(new RuntimeException("Connection error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            client.getCustomerContractsByDocument(uuid, document);
        });

        verify(apigeeUtils).generateRequest(eq(uuid), any());
    }
}
