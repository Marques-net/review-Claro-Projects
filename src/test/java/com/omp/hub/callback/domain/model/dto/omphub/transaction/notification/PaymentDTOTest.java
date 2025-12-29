package com.omp.hub.callback.domain.model.dto.omphub.transaction.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PaymentDTOTest {

    @Test
    void paymentDTO_ShouldBeInstantiated() {
        // When
        PaymentDTO paymentDTO = new PaymentDTO();

        // Then
        assertThat(paymentDTO).isNotNull();
    }
}