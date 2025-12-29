package com.omp.hub.callback.domain.model.dto.omphub.transaction.notification;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OmphubTransactionNotificationRequestTest {

    @Test
    void testOmphubTransactionNotificationRequestBuilder() {
        // Given
        DataDTO data = DataDTO.builder().build();

        // When
        OmphubTransactionNotificationRequest request = OmphubTransactionNotificationRequest.builder()
                .data(data)
                .build();

        // Then
        assertNotNull(request);
        assertEquals(data, request.getData());
    }

    @Test
    void testOmphubTransactionNotificationRequestSettersAndGetters() {
        // Given
        OmphubTransactionNotificationRequest request = new OmphubTransactionNotificationRequest();
        DataDTO data = DataDTO.builder().build();

        // When
        request.setData(data);

        // Then
        assertEquals(data, request.getData());
    }

    @Test
    void testOmphubTransactionNotificationRequestDefaultConstructor() {
        // When
        OmphubTransactionNotificationRequest request = new OmphubTransactionNotificationRequest();

        // Then
        assertNotNull(request);
        assertNull(request.getData());
    }
}