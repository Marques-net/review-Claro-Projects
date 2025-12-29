package com.omp.hub.callback.domain.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class AddressDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void addressDTO_ShouldSerializeAndDeserialize() throws Exception {
        // Arrange
        AddressDTO address = AddressDTO.builder()
                .streetName("Rua das Flores")
                .streetNr("123")
                .informationStreetNr("Apartamento 45")
                .complement("Bloco B")
                .informationComplement("Próximo ao mercado")
                .city("São Paulo")
                .stateOrProvince("SP")
                .postCode("01234-567")
                .country("Brasil")
                .build();

        // Act
        String json = objectMapper.writeValueAsString(address);
        AddressDTO deserialized = objectMapper.readValue(json, AddressDTO.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getStreetName()).isEqualTo("Rua das Flores");
        assertThat(deserialized.getStreetNr()).isEqualTo("123");
        assertThat(deserialized.getInformationStreetNr()).isEqualTo("Apartamento 45");
        assertThat(deserialized.getComplement()).isEqualTo("Bloco B");
        assertThat(deserialized.getInformationComplement()).isEqualTo("Próximo ao mercado");
        assertThat(deserialized.getCity()).isEqualTo("São Paulo");
        assertThat(deserialized.getStateOrProvince()).isEqualTo("SP");
        assertThat(deserialized.getPostCode()).isEqualTo("01234-567");
        assertThat(deserialized.getCountry()).isEqualTo("Brasil");
    }

    @Test
    void addressDTO_AllArgsConstructor_ShouldWork() {
        // Act
        AddressDTO address = new AddressDTO(
                "Rua das Flores", "123", "Apt 45", "Bloco B", 
                "Próximo ao mercado", "São Paulo", "SP", "01234-567", "Brasil");

        // Assert
        assertThat(address.getStreetName()).isEqualTo("Rua das Flores");
        assertThat(address.getStreetNr()).isEqualTo("123");
        assertThat(address.getCity()).isEqualTo("São Paulo");
        assertThat(address.getCountry()).isEqualTo("Brasil");
    }

    @Test
    void addressDTO_NoArgsConstructor_ShouldWork() {
        // Act
        AddressDTO address = new AddressDTO();
        address.setStreetName("Av. Paulista");
        address.setStreetNr("1000");
        address.setCity("São Paulo");
        address.setStateOrProvince("SP");

        // Assert
        assertThat(address.getStreetName()).isEqualTo("Av. Paulista");
        assertThat(address.getStreetNr()).isEqualTo("1000");
        assertThat(address.getCity()).isEqualTo("São Paulo");
        assertThat(address.getStateOrProvince()).isEqualTo("SP");
    }

    @Test
    void addressDTO_Builder_ShouldWork() {
        // Act
        AddressDTO address = AddressDTO.builder()
                .streetName("Rua Teste")
                .city("Rio de Janeiro")
                .stateOrProvince("RJ")
                .build();

        // Assert
        assertThat(address.getStreetName()).isEqualTo("Rua Teste");
        assertThat(address.getCity()).isEqualTo("Rio de Janeiro");
        assertThat(address.getStateOrProvince()).isEqualTo("RJ");
    }
}
