package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.domain.model.dto.customer.CustomerMobileResponse;
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
class CustomerMobileSubscriberClientTest {

    @Mock
    private ApigeeUtils apigeeUtils;

    @Mock
    private ApigeeHeaderService apigeeHeaderService;

    @InjectMocks
    private CustomerMobileSubscriberClient client;

    private UUID uuid;
    private String document;
    private String status;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        document = "12345678900";
        status = "ACTIVE";
        
        // Inject @Value property
        ReflectionTestUtils.setField(client, "urlClient", "https://api.claro.com/customer/mobile");
    }

    @Test
    void send_WithValidCpfAndStatus_ShouldReturnCustomerMobileResponse() {
        // Given
        CustomerMobileResponse expectedResponse = new CustomerMobileResponse();
        Request request = new Request.Builder().url("https://api.claro.com").build();
        
        Headers.Builder headersBuilder = new Headers.Builder();
        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headersBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any())).thenReturn(request);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerMobileResponse.class)))
                .thenReturn(expectedResponse);

        // When
        CustomerMobileResponse result = client.send(uuid, document, status);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeUtils).generateRequest(eq(uuid), any());
        verify(apigeeUtils).sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerMobileResponse.class));
    }

    @Test
    void send_WithCnpjDocument_ShouldUseCnpjInHeader() {
        // Given
        String cnpj = "12345678000195";
        CustomerMobileResponse expectedResponse = new CustomerMobileResponse();
        Request request = new Request.Builder().url("https://api.claro.com").build();
        
        Headers.Builder headersBuilder = new Headers.Builder();
        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headersBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any())).thenReturn(request);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerMobileResponse.class)))
                .thenReturn(expectedResponse);

        // When
        CustomerMobileResponse result = client.send(uuid, cnpj, status);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeUtils).generateRequest(eq(uuid), any());
        verify(apigeeUtils).sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerMobileResponse.class));
    }
}
