package com.omp.hub.callback.domain.model.dto.callback.creditcard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class AntifraudDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void antifraudDTO_ShouldSerializeAndDeserialize() throws Exception {
        // Arrange
        AntifraudDTO antifraud = AntifraudDTO.builder()
                .statusCode("APPROVED")
                .decision("ACCEPT")
                .timeChangeStatus("2024-10-04T14:30:00")
                .build();

        // Act
        String json = objectMapper.writeValueAsString(antifraud);
        AntifraudDTO deserialized = objectMapper.readValue(json, AntifraudDTO.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getStatusCode()).isEqualTo("APPROVED");
        assertThat(deserialized.getDecision()).isEqualTo("ACCEPT");
        assertThat(deserialized.getTimeChangeStatus()).isEqualTo("2024-10-04T14:30:00");
    }

    @Test
    void antifraudDTO_AllArgsConstructor_ShouldWork() {
        // Act
        AntifraudDTO antifraud = new AntifraudDTO("REJECTED", "DENY", "2024-10-04T15:00:00");

        // Assert
        assertThat(antifraud.getStatusCode()).isEqualTo("REJECTED");
        assertThat(antifraud.getDecision()).isEqualTo("DENY");
        assertThat(antifraud.getTimeChangeStatus()).isEqualTo("2024-10-04T15:00:00");
    }

    @Test
    void antifraudDTO_NoArgsConstructor_ShouldWork() {
        // Act
        AntifraudDTO antifraud = new AntifraudDTO();
        antifraud.setStatusCode("PENDING");
        antifraud.setDecision("REVIEW");

        // Assert
        assertThat(antifraud.getStatusCode()).isEqualTo("PENDING");
        assertThat(antifraud.getDecision()).isEqualTo("REVIEW");
    }

    @Test
    void antifraudDTO_Builder_ShouldWork() {
        // Act
        AntifraudDTO antifraud = AntifraudDTO.builder()
                .statusCode("VERIFIED")
                .build();

        // Assert
        assertThat(antifraud.getStatusCode()).isEqualTo("VERIFIED");
    }
}
