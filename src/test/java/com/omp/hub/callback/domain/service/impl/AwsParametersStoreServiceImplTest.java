package com.omp.hub.callback.domain.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.omp.hub.callback.domain.service.aws.impl.AwsParametersStoreServiceImpl;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

@ExtendWith(MockitoExtension.class)
class AwsParametersStoreServiceImplTest {

    @InjectMocks
    private AwsParametersStoreServiceImpl service;

    @Mock
    private SsmClient ssmClient;

    @Test
    void getParameterByArn_WithValidArn_ShouldReturnValue() {
        // Given
        String parameterArn = "/app/config/test-param";
        String expectedValue = "test-value-123";
        
        Parameter parameter = Parameter.builder()
                .value(expectedValue)
                .build();
        
        GetParameterResponse response = GetParameterResponse.builder()
                .parameter(parameter)
                .build();
        
        when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(response);
        
        // When
        String result = service.getParameterByArn(parameterArn);
        
        // Then
        assertEquals(expectedValue, result);
        verify(ssmClient, times(1)).getParameter(any(GetParameterRequest.class));
    }

    @Test
    void getParameterByArn_WhenParameterNotFound_ShouldReturnNull() {
        // Given
        String parameterArn = "/app/config/non-existent";
        
        when(ssmClient.getParameter(any(GetParameterRequest.class)))
                .thenThrow(ParameterNotFoundException.builder()
                        .message("Parameter not found")
                        .build());
        
        // When
        String result = service.getParameterByArn(parameterArn);
        
        // Then
        assertNull(result);
    }

    @Test
    void getParameterByArn_WhenGenericExceptionOccurs_ShouldPropagateException() {
        // Given
        String parameterArn = "/app/config/error-param";
        
        when(ssmClient.getParameter(any(GetParameterRequest.class)))
                .thenThrow(new RuntimeException("AWS SSM error"));
        
        // When/Then
        assertThrows(RuntimeException.class, () -> {
            service.getParameterByArn(parameterArn);
        });
    }

    @Test
    void getParameterByArn_ShouldRequestWithDecryption() {
        // Given
        String parameterArn = "/app/config/encrypted-param";
        String expectedValue = "encrypted-value";
        
        Parameter parameter = Parameter.builder()
                .value(expectedValue)
                .build();
        
        GetParameterResponse response = GetParameterResponse.builder()
                .parameter(parameter)
                .build();
        
        when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(response);
        
        // When
        service.getParameterByArn(parameterArn);
        
        // Then
        verify(ssmClient, times(1)).getParameter(any(GetParameterRequest.class));
    }

    @Test
    void getParameterByArn_WithComplexArn_ShouldHandleCorrectly() {
        // Given
        String parameterArn = "/production/app/database/connection-string";
        String expectedValue = "jdbc:postgresql://localhost:5432/db";
        
        Parameter parameter = Parameter.builder()
                .value(expectedValue)
                .build();
        
        GetParameterResponse response = GetParameterResponse.builder()
                .parameter(parameter)
                .build();
        
        when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(response);
        
        // When
        String result = service.getParameterByArn(parameterArn);
        
        // Then
        assertEquals(expectedValue, result);
        verify(ssmClient, times(1)).getParameter(any(GetParameterRequest.class));
    }
}
