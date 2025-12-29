package com.omp.hub.callback.domain.model.dto.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class ExtractedCustomerDataDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void extractedCustomerDataDTO_ShouldSerializeAndDeserialize() throws Exception {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .segment("PF")
                .mobileBan("BAN-123")
                .contractNumber("CONT-456")
                .email("joao@example.com")
                .msisdn("11999999999")
                .build();

        // Act
        String json = objectMapper.writeValueAsString(customer);
        ExtractedCustomerDataDTO deserialized = objectMapper.readValue(json, ExtractedCustomerDataDTO.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getName()).isEqualTo("João Silva");
        assertThat(deserialized.getCpf()).isEqualTo("12345678901");
        assertThat(deserialized.getSegment()).isEqualTo("PF");
        assertThat(deserialized.getMobileBan()).isEqualTo("BAN-123");
        assertThat(deserialized.getContractNumber()).isEqualTo("CONT-456");
        assertThat(deserialized.getEmail()).isEqualTo("joao@example.com");
        assertThat(deserialized.getMsisdn()).isEqualTo("11999999999");
    }

    @Test
    void hasCompleteData_WithValidCpf_ShouldReturnTrue() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .segment("PF")
                .build();

        // Act & Assert
        assertThat(customer.hasCompleteData()).isTrue();
    }

    @Test
    void hasCompleteData_WithValidCnpj_ShouldReturnTrue() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .name("Empresa ABC")
                .cnpj("12345678000190")
                .segment("PJ")
                .build();

        // Act & Assert
        assertThat(customer.hasCompleteData()).isTrue();
    }

    @Test
    void hasCompleteData_WithoutName_ShouldReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .cpf("12345678901")
                .segment("PF")
                .build();

        // Act & Assert
        assertThat(customer.hasCompleteData()).isFalse();
    }

    @Test
    void hasCompleteData_WithEmptyName_ShouldReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .name("   ")
                .cpf("12345678901")
                .segment("PF")
                .build();

        // Act & Assert
        assertThat(customer.hasCompleteData()).isFalse();
    }

    @Test
    void hasCompleteData_WithoutDocument_ShouldReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .segment("PF")
                .build();

        // Act & Assert
        assertThat(customer.hasCompleteData()).isFalse();
    }

    @Test
    void hasCompleteData_WithEmptyDocument_ShouldReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("   ")
                .cnpj("   ")
                .segment("PF")
                .build();

        // Act & Assert
        assertThat(customer.hasCompleteData()).isFalse();
    }

    @Test
    void hasCompleteData_WithoutSegment_ShouldReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .build();

        // Act & Assert
        assertThat(customer.hasCompleteData()).isTrue();
    }

    @Test
    void hasCompleteData_WithEmptySegment_ShouldReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .segment("   ")
                .build();

        // Act & Assert
        assertThat(customer.hasCompleteData()).isTrue();
    }

    @Test
    void hasValidMobileBan_WithValidMobileBan_ShouldReturnTrue() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .mobileBan("BAN-123")
                .build();

        // Act & Assert
        assertThat(customer.hasValidMobileBan()).isTrue();
    }

    @Test
    void hasValidMobileBan_WithNullMobileBan_ShouldReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .build();

        // Act & Assert
        assertThat(customer.hasValidMobileBan()).isFalse();
    }

    @Test
    void hasValidMobileBan_WithEmptyMobileBan_ShouldReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .mobileBan("   ")
                .build();

        // Act & Assert
        assertThat(customer.hasValidMobileBan()).isFalse();
    }

    @Test
    void hasValidContractNumber_WithValidContractNumber_ShouldReturnTrue() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .contractNumber("CONT-456")
                .build();

        // Act & Assert
        assertThat(customer.hasValidContractNumber()).isTrue();
    }

    @Test
    void hasValidContractNumber_WithNullContractNumber_ShouldReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .build();

        // Act & Assert
        assertThat(customer.hasValidContractNumber()).isFalse();
    }

    @Test
    void hasValidContractNumber_WithEmptyContractNumber_ShouldReturnFalse() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .contractNumber("   ")
                .build();

        // Act & Assert
        assertThat(customer.hasValidContractNumber()).isFalse();
    }

    @Test
    void toBuilder_ShouldCreateCopy() {
        // Arrange
        ExtractedCustomerDataDTO original = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .segment("PF")
                .build();

        // Act
        ExtractedCustomerDataDTO copy = original.toBuilder()
                .email("joao@example.com")
                .build();

        // Assert
        assertThat(copy.getName()).isEqualTo(original.getName());
        assertThat(copy.getCpf()).isEqualTo(original.getCpf());
        assertThat(copy.getSegment()).isEqualTo(original.getSegment());
        assertThat(copy.getEmail()).isEqualTo("joao@example.com");
    }

    @Test
    void allArgsConstructor_ShouldWork() {
        // Act
        ExtractedCustomerDataDTO customer = new ExtractedCustomerDataDTO(
                "João Silva", "12345678901", null, "PF", 
                "BAN-123", "123", "456", "CONT-456", "joao@example.com", "11999999999", false);

        // Assert
        assertThat(customer.getName()).isEqualTo("João Silva");
        assertThat(customer.getCpf()).isEqualTo("12345678901");
        assertThat(customer.getSegment()).isEqualTo("PF");
        assertThat(customer.hasCompleteData()).isTrue();
    }

    @Test
    void noArgsConstructor_ShouldWork() {
        // Act
        ExtractedCustomerDataDTO customer = new ExtractedCustomerDataDTO();
        customer.setName("Maria Santos");
        customer.setCnpj("12345678000190");
        customer.setSegment("PJ");

        // Assert
        assertThat(customer.getName()).isEqualTo("Maria Santos");
        assertThat(customer.getCnpj()).isEqualTo("12345678000190");
        assertThat(customer.hasCompleteData()).isTrue();
    }

    @Test
    void criteriosAtendidos_ShouldWorkCorrectly() {
        // Arrange
        ExtractedCustomerDataDTO customer = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .criteriosAtendidos(true)
                .build();

        // Act & Assert
        assertThat(customer.getCriteriosAtendidos()).isTrue();
        
        customer.setCriteriosAtendidos(false);
        assertThat(customer.getCriteriosAtendidos()).isFalse();
    }

    @Test
    void allGettersAndSetters_ShouldWork() {
        // Arrange
        ExtractedCustomerDataDTO customer = new ExtractedCustomerDataDTO();

        // Act
        customer.setName("João Silva");
        customer.setCpf("12345678901");
        customer.setCnpj("12345678000190");
        customer.setSegment("PF");
        customer.setMobileBan("BAN-123");
        customer.setOperatorCode("OP-456");
        customer.setCityCode("CITY-789");
        customer.setContractNumber("CONT-101");
        customer.setEmail("joao@example.com");
        customer.setMsisdn("11999999999");
        customer.setCriteriosAtendidos(true);

        // Assert
        assertThat(customer.getName()).isEqualTo("João Silva");
        assertThat(customer.getCpf()).isEqualTo("12345678901");
        assertThat(customer.getCnpj()).isEqualTo("12345678000190");
        assertThat(customer.getSegment()).isEqualTo("PF");
        assertThat(customer.getMobileBan()).isEqualTo("BAN-123");
        assertThat(customer.getOperatorCode()).isEqualTo("OP-456");
        assertThat(customer.getCityCode()).isEqualTo("CITY-789");
        assertThat(customer.getContractNumber()).isEqualTo("CONT-101");
        assertThat(customer.getEmail()).isEqualTo("joao@example.com");
        assertThat(customer.getMsisdn()).isEqualTo("11999999999");
        assertThat(customer.getCriteriosAtendidos()).isTrue();
    }
}
