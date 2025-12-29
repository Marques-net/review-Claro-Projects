package com.omp.hub.callback.domain.model.dto.journey.single;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class ChangeDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void changeDTO_ShouldSerializeAndDeserialize() throws Exception {
        // Arrange
        ChangeDTO change = ChangeDTO.builder()
                .value("100.50")
                .alterationModality(1)
                .agentModality("CASH")
                .withdrawalServiceProvider("PROVIDER-123")
                .build();

        // Act
        String json = objectMapper.writeValueAsString(change);
        ChangeDTO deserialized = objectMapper.readValue(json, ChangeDTO.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getValue()).isEqualTo("100.50");
        assertThat(deserialized.getAlterationModality()).isEqualTo(1);
        assertThat(deserialized.getAgentModality()).isEqualTo("CASH");
        assertThat(deserialized.getWithdrawalServiceProvider()).isEqualTo("PROVIDER-123");
    }

    @Test
    void changeDTO_AllArgsConstructor_ShouldWork() {
        // Act
        ChangeDTO change = new ChangeDTO("50.00", 2, "DIGITAL", "PROVIDER-XYZ");

        // Assert
        assertThat(change.getValue()).isEqualTo("50.00");
        assertThat(change.getAlterationModality()).isEqualTo(2);
        assertThat(change.getAgentModality()).isEqualTo("DIGITAL");
        assertThat(change.getWithdrawalServiceProvider()).isEqualTo("PROVIDER-XYZ");
    }

    @Test
    void changeDTO_NoArgsConstructor_ShouldWork() {
        // Act
        ChangeDTO change = new ChangeDTO();
        change.setValue("75.25");
        change.setAlterationModality(3);

        // Assert
        assertThat(change.getValue()).isEqualTo("75.25");
        assertThat(change.getAlterationModality()).isEqualTo(3);
    }

    @Test
    void changeDTO_Builder_ShouldWork() {
        // Act
        ChangeDTO change = ChangeDTO.builder()
                .value("200.00")
                .agentModality("HYBRID")
                .build();

        // Assert
        assertThat(change.getValue()).isEqualTo("200.00");
        assertThat(change.getAgentModality()).isEqualTo("HYBRID");
    }
}
