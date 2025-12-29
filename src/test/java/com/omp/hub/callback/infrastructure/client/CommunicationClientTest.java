package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationDataDTO;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageRequest;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageResponse;
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
class CommunicationClientTest {

    @Mock
    private ApigeeUtils apigeeUtils;

    @InjectMocks
    private CommunicationClient client;

    private UUID uuid;
    private CommunicationMessageRequest request;
    private Headers.Builder headersBuilder;
    private CommunicationMessageResponse expectedResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "urlClient", "https://api.example.com/communication");
        
        uuid = UUID.randomUUID();
        
        request = CommunicationMessageRequest.builder()
                .data(CommunicationDataDTO.builder()
                        .channel("SMS")
                        .build())
                .build();
        
        headersBuilder = new Headers.Builder()
                .add("Content-Type", "application/json");
        
        expectedResponse = CommunicationMessageResponse.builder().build();
    }

    @Test
    void sendMessage_WithValidRequest_ShouldReturnResponse() {
        // Given
        Request mockRequest = new Request.Builder()
                .url("https://api.example.com/communication")
                .build();
        
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);
        when(apigeeUtils.sendRequestToApigee(eq(uuid), eq(mockRequest), anyString(), eq(CommunicationMessageResponse.class)))
                .thenReturn(expectedResponse);

        // When
        CommunicationMessageResponse result = client.sendMessage(uuid, request, headersBuilder);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(apigeeUtils).generateRequest(eq(uuid), any(GenerateRequestDTO.class));
        verify(apigeeUtils).sendRequestToApigee(eq(uuid), eq(mockRequest), 
                eq("https://api.example.com/communication"), eq(CommunicationMessageResponse.class));
    }

    @Test
    void sendMessage_WhenExceptionOccurs_ShouldReturnErrorResponse() {
        // Given
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        // When
        CommunicationMessageResponse result = client.sendMessage(uuid, request, headersBuilder);

        // Then
        assertNotNull(result);
        assertNotNull(result.getError());
        assertTrue(result.getError().getMessage().contains("Connection failed"));
    }

    @Test
    void sendMessage_WithNullChannel_ShouldHandleGracefully() {
        // Given
        request.setData(CommunicationDataDTO.builder().channel(null).build());
        Request mockRequest = new Request.Builder()
                .url("https://api.example.com/communication")
                .build();
        
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);
        when(apigeeUtils.sendRequestToApigee(any(), any(), anyString(), eq(CommunicationMessageResponse.class)))
                .thenReturn(expectedResponse);

        // When
        CommunicationMessageResponse result = client.sendMessage(uuid, request, headersBuilder);

        // Then
        assertNotNull(result);
        verify(apigeeUtils).sendRequestToApigee(any(), any(), anyString(), eq(CommunicationMessageResponse.class));
    }

    @Test
    void sendMessage_WithNullData_ShouldHandleGracefully() {
        // Given
        request.setData(null);
        Request mockRequest = new Request.Builder()
                .url("https://api.example.com/communication")
                .build();
        
        when(apigeeUtils.generateRequest(eq(uuid), any(GenerateRequestDTO.class))).thenReturn(mockRequest);
        when(apigeeUtils.sendRequestToApigee(any(), any(), anyString(), eq(CommunicationMessageResponse.class)))
                .thenReturn(expectedResponse);

        // When
        CommunicationMessageResponse result = client.sendMessage(uuid, request, headersBuilder);

        // Then
        assertNotNull(result);
    }
}
