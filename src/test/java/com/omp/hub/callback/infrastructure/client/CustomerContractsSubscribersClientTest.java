package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersResponse;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
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
class CustomerContractsSubscribersClientTest {

    @Mock
    private ApigeeUtils apigeeUtils;

    @Mock
    private ApigeeHeaderService apigeeHeaderService;

    @InjectMocks
    private CustomerContractsSubscribersClient client;

    private UUID uuid;
    private String cpf;
    private String cnpj;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        cpf = "12345678900";
        cnpj = "12345678000195";
        
        // Inject @Value property
        ReflectionTestUtils.setField(client, "urlClient", "https://api.claro.com/customer/contracts/subscribers");
    }

    @Test
    void send_WithValidCpf_ShouldReturnCustomerContractsResponse() {
        // Given
        CustomerContractsSubscribersResponse expectedResponse = new CustomerContractsSubscribersResponse();
        Request request = new Request.Builder().url("https://api.claro.com").build();
        ExtractedCustomerDataDTO dto = ExtractedCustomerDataDTO.builder().build();
        
        Headers.Builder headersBuilder = new Headers.Builder();
        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headersBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any())).thenReturn(request);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerContractsSubscribersResponse.class)))
                .thenReturn(expectedResponse);

        // When
        CustomerContractsSubscribersResponse result = client.send(uuid, cpf, dto);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeUtils).generateRequest(eq(uuid), any());
        verify(apigeeUtils).sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerContractsSubscribersResponse.class));
    }

    @Test
    void send_WithValidCnpj_ShouldReturnCustomerContractsResponse() {
        // Given
        CustomerContractsSubscribersResponse expectedResponse = new CustomerContractsSubscribersResponse();
        Request request = new Request.Builder().url("https://api.claro.com").build();
        ExtractedCustomerDataDTO dto = ExtractedCustomerDataDTO.builder().build();
        
        Headers.Builder headersBuilder = new Headers.Builder();
        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headersBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any())).thenReturn(request);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerContractsSubscribersResponse.class)))
                .thenReturn(expectedResponse);

        // When
        CustomerContractsSubscribersResponse result = client.send(uuid, cnpj, dto);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeUtils).generateRequest(eq(uuid), any());
        verify(apigeeUtils).sendRequestToApigee(eq(uuid), eq(request), anyString(), eq(CustomerContractsSubscribersResponse.class));
    }

    @Test
    void send_WhenExceptionOccurs_ShouldReturnNull() {
        ExtractedCustomerDataDTO dto = ExtractedCustomerDataDTO.builder().build();
        // Given
        Headers.Builder headersBuilder = new Headers.Builder();
        when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headersBuilder);
        when(apigeeUtils.generateRequest(eq(uuid), any())).thenThrow(new RuntimeException("Connection error"));

        // When
        CustomerContractsSubscribersResponse result = client.send(uuid, cpf, dto);

        // Then
        assertNull(result);
        verify(apigeeUtils).generateRequest(eq(uuid), any());
        verify(apigeeUtils, never()).sendRequestToApigee(any(), any(), anyString(), any());
    }
}
