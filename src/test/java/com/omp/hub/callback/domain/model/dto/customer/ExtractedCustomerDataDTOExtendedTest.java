package com.omp.hub.callback.domain.model.dto.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractedCustomerDataDTOExtendedTest {

    @ParameterizedTest
    @ValueSource(strings = {"João", "Maria da Silva", "Dr. José", "Prof. Ana Lima"})
    void testBuilderWithVariousNames(String name) {
        // Given & When
        ExtractedCustomerDataDTO dto = ExtractedCustomerDataDTO.builder()
                .name(name)
                .cpf("12345678901")
                .email("test@email.com")
                .msisdn("11999999999")
                .build();

        // Then
        assertThat(dto.getName()).isEqualTo(name);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901", "98765432109", "11144477789"})
    void testBuilderWithVariousCPFs(String cpf) {
        // Given & When
        ExtractedCustomerDataDTO dto = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf(cpf)
                .email("test@email.com")
                .build();

        // Then
        assertThat(dto.getCpf()).isEqualTo(cpf);
    }

    @ParameterizedTest
    @ValueSource(strings = {"test@email.com", "user@domain.br", "admin@company.org", "info@test.net"})
    void testBuilderWithVariousEmails(String email) {
        // Given & When
        ExtractedCustomerDataDTO dto = ExtractedCustomerDataDTO.builder()
                .name("João")
                .email(email)
                .build();

        // Then
        assertThat(dto.getEmail()).isEqualTo(email);
    }

    @ParameterizedTest
    @ValueSource(strings = {"11999999999", "21987654321", "85988776655", "4733332222"})
    void testBuilderWithVariousMsisdns(String msisdn) {
        // Given & When
        ExtractedCustomerDataDTO dto = ExtractedCustomerDataDTO.builder()
                .name("João")
                .msisdn(msisdn)
                .build();

        // Then
        assertThat(dto.getMsisdn()).isEqualTo(msisdn);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testCriteriosAtendidosVariations(boolean criterios) {
        // Given & When
        ExtractedCustomerDataDTO dto = ExtractedCustomerDataDTO.builder()
                .name("João")
                .criteriosAtendidos(criterios)
                .build();

        // Then
        assertThat(dto.getCriteriosAtendidos()).isEqualTo(criterios);
    }

    @Test
    void testHasValidMobileBanEdgeCases() {
        // Given
        ExtractedCustomerDataDTO dto = new ExtractedCustomerDataDTO();

        // When - null mobileBan
        boolean result1 = dto.hasValidMobileBan();
        assertThat(result1).isFalse();

        // When - empty mobileBan
        dto.setMobileBan("");
        boolean result2 = dto.hasValidMobileBan();
        assertThat(result2).isFalse();

        // When - blank mobileBan
        dto.setMobileBan("   ");
        boolean result3 = dto.hasValidMobileBan();
        assertThat(result3).isFalse();

        // When - valid mobileBan
        dto.setMobileBan("BAN123");
        boolean result4 = dto.hasValidMobileBan();
        assertThat(result4).isTrue();
    }

    @Test
    void testHasValidContractNumberEdgeCases() {
        // Given
        ExtractedCustomerDataDTO dto = new ExtractedCustomerDataDTO();

        // When - null contractNumber
        boolean result1 = dto.hasValidContractNumber();
        assertThat(result1).isFalse();

        // When - empty contractNumber
        dto.setContractNumber("");
        boolean result2 = dto.hasValidContractNumber();
        assertThat(result2).isFalse();

        // When - blank contractNumber
        dto.setContractNumber("   ");
        boolean result3 = dto.hasValidContractNumber();
        assertThat(result3).isFalse();

        // When - valid contractNumber
        dto.setContractNumber("CONT123");
        boolean result4 = dto.hasValidContractNumber();
        assertThat(result4).isTrue();
    }

    @Test
    void testToBuilder() {
        // Given
        ExtractedCustomerDataDTO original = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .email("joao@email.com")
                .build();

        // When
        ExtractedCustomerDataDTO copy = original.toBuilder()
                .msisdn("11999999999")
                .build();

        // Then
        assertThat(copy.getName()).isEqualTo("João");
        assertThat(copy.getCpf()).isEqualTo("12345678901");
        assertThat(copy.getEmail()).isEqualTo("joao@email.com");
        assertThat(copy.getMsisdn()).isEqualTo("11999999999");
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        ExtractedCustomerDataDTO dto1 = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .build();

        ExtractedCustomerDataDTO dto2 = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .build();

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        ExtractedCustomerDataDTO dto = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .build();

        // When
        String result = dto.toString();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("João");
        assertThat(result).contains("12345678901");
    }

    @Test
    void testBuilderWithAllFields() {
        // Given
        String name = "João Silva";
        String cpf = "12345678901";
        String cnpj = "12345678000199";
        String email = "joao@email.com";
        String msisdn = "11999999999";
        String segment = "CLARO_MOVEL";
        String mobileBan = "123456789";
        String contractNumber = "CONTR123";
        String operatorCode = "OP01";
        String cityCode = "CITY01";

        // When
        ExtractedCustomerDataDTO dto = ExtractedCustomerDataDTO.builder()
                .name(name)
                .cpf(cpf)
                .cnpj(cnpj)
                .email(email)
                .msisdn(msisdn)
                .segment(segment)
                .mobileBan(mobileBan)
                .contractNumber(contractNumber)
                .operatorCode(operatorCode)
                .cityCode(cityCode)
                .criteriosAtendidos(true)
                .build();

        // Then
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getCpf()).isEqualTo(cpf);
        assertThat(dto.getCnpj()).isEqualTo(cnpj);
        assertThat(dto.getEmail()).isEqualTo(email);
        assertThat(dto.getMsisdn()).isEqualTo(msisdn);
        assertThat(dto.getSegment()).isEqualTo(segment);
        assertThat(dto.getMobileBan()).isEqualTo(mobileBan);
        assertThat(dto.getContractNumber()).isEqualTo(contractNumber);
        assertThat(dto.getOperatorCode()).isEqualTo(operatorCode);
        assertThat(dto.getCityCode()).isEqualTo(cityCode);
        assertThat(dto.getCriteriosAtendidos()).isTrue();
    }

    @Test
    void testHasCompleteDataAllCombinations() {
        // Test various combinations of completeness
        // hasCompleteData() only validates name and document (CPF or CNPJ)
        
        // Only name - should be false (no document)
        ExtractedCustomerDataDTO dto1 = ExtractedCustomerDataDTO.builder()
                .name("João")
                .build();
        assertThat(dto1.hasCompleteData()).isFalse();

        // Only CPF - should be false (no name)
        ExtractedCustomerDataDTO dto2 = ExtractedCustomerDataDTO.builder()
                .cpf("12345678901")
                .build();
        assertThat(dto2.hasCompleteData()).isFalse();

        // Name and CPF - should be true (all required fields present)
        ExtractedCustomerDataDTO dto3 = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .build();
        assertThat(dto3.hasCompleteData()).isTrue();

        // Name and CNPJ - should be true (document can be CNPJ)
        ExtractedCustomerDataDTO dto4 = ExtractedCustomerDataDTO.builder()
                .name("Empresa LTDA")
                .cnpj("12345678000199")
                .build();
        assertThat(dto4.hasCompleteData()).isTrue();

        // Name with CPF and additional fields - should still be true
        ExtractedCustomerDataDTO dto5 = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .email("joao@email.com")
                .msisdn("11999999999")
                .build();
        assertThat(dto5.hasCompleteData()).isTrue();
    }

    @Test
    void testFieldAssignmentThroughSetters() {
        // Given
        ExtractedCustomerDataDTO dto = new ExtractedCustomerDataDTO();

        // When - assigning multiple fields
        dto.setName("Maria");
        dto.setCpf("98765432109");
        dto.setCnpj("98765432000111");
        dto.setEmail("maria@email.com");
        dto.setMsisdn("21988776655");
        dto.setSegment("CLARO_RESIDENCIAL");
        dto.setMobileBan("987654321");
        dto.setContractNumber("CONTR456");
        dto.setOperatorCode("OP02");
        dto.setCityCode("CITY02");

        // Then
        assertThat(dto.getName()).isEqualTo("Maria");
        assertThat(dto.getCpf()).isEqualTo("98765432109");
        assertThat(dto.getCnpj()).isEqualTo("98765432000111");
        assertThat(dto.getEmail()).isEqualTo("maria@email.com");
        assertThat(dto.getMsisdn()).isEqualTo("21988776655");
        assertThat(dto.getSegment()).isEqualTo("CLARO_RESIDENCIAL");
        assertThat(dto.getMobileBan()).isEqualTo("987654321");
        assertThat(dto.getContractNumber()).isEqualTo("CONTR456");
        assertThat(dto.getOperatorCode()).isEqualTo("OP02");
        assertThat(dto.getCityCode()).isEqualTo("CITY02");
    }

    @Test
    void testNullFieldHandling() {
        // Given
        ExtractedCustomerDataDTO dto = new ExtractedCustomerDataDTO();

        // Then - all fields should be null initially
        assertThat(dto.getName()).isNull();
        assertThat(dto.getCpf()).isNull();
        assertThat(dto.getCnpj()).isNull();
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getMsisdn()).isNull();
        assertThat(dto.getSegment()).isNull();
        assertThat(dto.getMobileBan()).isNull();
        assertThat(dto.getContractNumber()).isNull();
        assertThat(dto.getOperatorCode()).isNull();
        assertThat(dto.getCityCode()).isNull();
    }
}
