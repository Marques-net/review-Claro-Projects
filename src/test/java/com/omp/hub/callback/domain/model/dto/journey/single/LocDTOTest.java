package com.omp.hub.callback.domain.model.dto.journey.single;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class LocDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void locDTO_ShouldSerializeAndDeserialize() throws Exception {
        // Arrange
        LocDTO loc = LocDTO.builder()
                .id(12345)
                .build();

        // Act
        String json = objectMapper.writeValueAsString(loc);
        LocDTO deserialized = objectMapper.readValue(json, LocDTO.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getId()).isEqualTo(12345);
    }

    @Test
    void locDTO_AllArgsConstructor_ShouldWork() {
        // Act
        LocDTO loc = new LocDTO(999);

        // Assert
        assertThat(loc.getId()).isEqualTo(999);
    }

    @Test
    void locDTO_NoArgsConstructor_ShouldWork() {
        // Act
        LocDTO loc = new LocDTO();
        loc.setId(777);

        // Assert
        assertThat(loc.getId()).isEqualTo(777);
    }

    @Test
    void locDTO_Builder_ShouldWork() {
        // Act
        LocDTO loc = LocDTO.builder().id(555).build();

        // Assert
        assertThat(loc.getId()).isEqualTo(555);
    }

    @Test
    void locDTO_WithNullId_ShouldWork() throws Exception {
        // Arrange
        LocDTO loc = new LocDTO();

        // Act
        String json = objectMapper.writeValueAsString(loc);
        LocDTO deserialized = objectMapper.readValue(json, LocDTO.class);

        // Assert
        assertThat(deserialized.getId()).isNull();
    }
}
