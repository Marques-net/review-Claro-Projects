package com.omp.hub.callback.domain.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class InvoiceDataDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void invoiceDataDTO_ShouldSerializeAndDeserialize() throws Exception {
        // Arrange
        AddressDTO address = AddressDTO.builder()
                .streetName("Rua das Flores")
                .streetNr("123")
                .city("São Paulo")
                .stateOrProvince("SP")
                .postCode("01234-567")
                .country("Brasil")
                .build();

        InvoiceDataDTO invoice = InvoiceDataDTO.builder()
                .name("João Silva")
                .phoneNumber("11999999999")
                .email("joao@example.com")
                .address(address)
                .build();

        // Act
        String json = objectMapper.writeValueAsString(invoice);
        InvoiceDataDTO deserialized = objectMapper.readValue(json, InvoiceDataDTO.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getName()).isEqualTo("João Silva");
        assertThat(deserialized.getPhoneNumber()).isEqualTo("11999999999");
        assertThat(deserialized.getEmail()).isEqualTo("joao@example.com");
        assertThat(deserialized.getAddress()).isNotNull();
        assertThat(deserialized.getAddress().getStreetName()).isEqualTo("Rua das Flores");
        assertThat(deserialized.getAddress().getCity()).isEqualTo("São Paulo");
    }

    @Test
    void invoiceDataDTO_AllArgsConstructor_ShouldWork() {
        // Arrange
        AddressDTO address = AddressDTO.builder()
                .city("Rio de Janeiro")
                .stateOrProvince("RJ")
                .build();

        // Act
        InvoiceDataDTO invoice = new InvoiceDataDTO("Maria Santos", "21988888888", "maria@example.com", address);

        // Assert
        assertThat(invoice.getName()).isEqualTo("Maria Santos");
        assertThat(invoice.getPhoneNumber()).isEqualTo("21988888888");
        assertThat(invoice.getEmail()).isEqualTo("maria@example.com");
        assertThat(invoice.getAddress()).isNotNull();
        assertThat(invoice.getAddress().getCity()).isEqualTo("Rio de Janeiro");
    }

    @Test
    void invoiceDataDTO_NoArgsConstructor_ShouldWork() {
        // Act
        InvoiceDataDTO invoice = new InvoiceDataDTO();
        invoice.setName("Pedro Costa");
        invoice.setPhoneNumber("11977777777");
        invoice.setEmail("pedro@example.com");

        // Assert
        assertThat(invoice.getName()).isEqualTo("Pedro Costa");
        assertThat(invoice.getPhoneNumber()).isEqualTo("11977777777");
        assertThat(invoice.getEmail()).isEqualTo("pedro@example.com");
    }

    @Test
    void invoiceDataDTO_Builder_ShouldWork() {
        // Act
        InvoiceDataDTO invoice = InvoiceDataDTO.builder()
                .name("Ana Lima")
                .email("ana@example.com")
                .build();

        // Assert
        assertThat(invoice.getName()).isEqualTo("Ana Lima");
        assertThat(invoice.getEmail()).isEqualTo("ana@example.com");
    }

    @Test
    void invoiceDataDTO_WithNullAddress_ShouldWork() throws Exception {
        // Arrange
        InvoiceDataDTO invoice = InvoiceDataDTO.builder()
                .name("Carlos Souza")
                .phoneNumber("11966666666")
                .email("carlos@example.com")
                .address(null)
                .build();

        // Act
        String json = objectMapper.writeValueAsString(invoice);
        InvoiceDataDTO deserialized = objectMapper.readValue(json, InvoiceDataDTO.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getName()).isEqualTo("Carlos Souza");
        assertThat(deserialized.getAddress()).isNull();
    }
}
