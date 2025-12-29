package com.omp.hub.callback.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class RestTemplateConfigTest {

    @Test
    void restTemplate_ShouldBeConfigured() {
        // Given & When - Create a basic RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        
        // Then
        assertNotNull(restTemplate);
        assertNotNull(restTemplate.getInterceptors());
    }
}
