package com.omp.hub.callback.application.utils.apigee;

import com.omp.hub.callback.application.utils.apigee.dto.ApigeeTokenDTO;
import okhttp3.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApigeeHeaderServiceImplTest {

    @Mock
    private AccessTokenPort port;

    @InjectMocks
    private ApigeeHeaderServiceImpl service;

    private UUID uuid;
    private ApigeeTokenDTO tokenDTO;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        tokenDTO = new ApigeeTokenDTO();
        tokenDTO.setAccess_token("test-token-12345");
    }

    @Test
    void generateHeaderApigee_WithValidToken_ShouldReturnHeadersWithBearerToken() {
        // Given
        when(port.getAccessToken(uuid)).thenReturn(tokenDTO);

        // When
        Headers.Builder result = service.generateHeaderApigee(uuid);

        // Then
        assertNotNull(result);
        Headers headers = result.build();
        
        assertEquals("Bearer test-token-12345", headers.get("x-client-auth"));
        assertEquals("application/json", headers.get("Content-Type"));
        
        verify(port).getAccessToken(uuid);
    }

    @Test
    void generateHeaderApigee_WhenPortReturnsToken_ShouldFormatCorrectly() {
        // Given
        ApigeeTokenDTO customToken = new ApigeeTokenDTO();
        customToken.setAccess_token("custom-access-token-xyz");
        when(port.getAccessToken(uuid)).thenReturn(customToken);

        // When
        Headers.Builder result = service.generateHeaderApigee(uuid);

        // Then
        Headers headers = result.build();
        assertEquals("Bearer custom-access-token-xyz", headers.get("x-client-auth"));
        verify(port).getAccessToken(uuid);
    }
}
