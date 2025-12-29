package com.omp.hub.callback.domain.model.dto.callback.creditcard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class RetryProcessorDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void retryProcessorDTO_ShouldSerializeAndDeserialize() throws Exception {
        // Arrange
        RetryProcessorDTO retry = RetryProcessorDTO.builder()
                .nsu("123456789")
                .authorizationCode("AUTH-001")
                .acquiratorCode("ACQ-123")
                .transactionId("TXN-456")
                .responseCode("00")
                .responseDescription("Transaction approved")
                .merchantAdviceCode("01")
                .build();

        // Act
        String json = objectMapper.writeValueAsString(retry);
        RetryProcessorDTO deserialized = objectMapper.readValue(json, RetryProcessorDTO.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getNsu()).isEqualTo("123456789");
        assertThat(deserialized.getAuthorizationCode()).isEqualTo("AUTH-001");
        assertThat(deserialized.getAcquiratorCode()).isEqualTo("ACQ-123");
        assertThat(deserialized.getTransactionId()).isEqualTo("TXN-456");
        assertThat(deserialized.getResponseCode()).isEqualTo("00");
        assertThat(deserialized.getResponseDescription()).isEqualTo("Transaction approved");
        assertThat(deserialized.getMerchantAdviceCode()).isEqualTo("01");
    }

    @Test
    void retryProcessorDTO_AllArgsConstructor_ShouldWork() {
        // Act
        RetryProcessorDTO retry = new RetryProcessorDTO(
                "987654321", "AUTH-999", "ACQ-999", "TXN-999",
                "99", "Transaction denied", "02");

        // Assert
        assertThat(retry.getNsu()).isEqualTo("987654321");
        assertThat(retry.getAuthorizationCode()).isEqualTo("AUTH-999");
        assertThat(retry.getResponseCode()).isEqualTo("99");
        assertThat(retry.getMerchantAdviceCode()).isEqualTo("02");
    }

    @Test
    void retryProcessorDTO_NoArgsConstructor_ShouldWork() {
        // Act
        RetryProcessorDTO retry = new RetryProcessorDTO();
        retry.setNsu("555555555");
        retry.setTransactionId("TXN-555");
        retry.setResponseCode("05");

        // Assert
        assertThat(retry.getNsu()).isEqualTo("555555555");
        assertThat(retry.getTransactionId()).isEqualTo("TXN-555");
        assertThat(retry.getResponseCode()).isEqualTo("05");
    }

    @Test
    void retryProcessorDTO_Builder_ShouldWork() {
        // Act
        RetryProcessorDTO retry = RetryProcessorDTO.builder()
                .nsu("111222333")
                .responseCode("00")
                .build();

        // Assert
        assertThat(retry.getNsu()).isEqualTo("111222333");
        assertThat(retry.getResponseCode()).isEqualTo("00");
    }
}
