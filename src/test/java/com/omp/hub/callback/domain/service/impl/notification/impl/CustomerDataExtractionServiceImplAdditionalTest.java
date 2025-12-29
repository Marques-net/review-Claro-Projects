package com.omp.hub.callback.domain.service.impl.notification.impl;

import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.ports.client.CustomerContractsSubscribersPort;
import com.omp.hub.callback.domain.ports.client.CustomerMobilePort;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.ports.client.MobileBillingDetailsPort;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.service.impl.notification.util.CustomerContactExtractorUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerDataExtractionServiceImplAdditionalTest {

    @Mock
    private InformationPaymentPort paymentPort;

    @Mock
    private CustomerMobilePort customerMobilePort;

    @Mock
    private MobileBillingDetailsPort mobileBillingDetailsPort;

    @Mock
    private CustomerContractsSubscribersPort customerContractsSubscribersPort;

    @Mock
    private CustomerContactExtractorUtil customerContactExtractorUtil;

    @InjectMocks
    private CustomerDataExtractionServiceImpl serviceUnderTest;

    private UUID testUUID;
    private String testIdentifier;

    @BeforeEach
    void setUp() {
        testUUID = UUID.randomUUID();
        testIdentifier = "PAYMENT_123";
    }

    @Test
    void testExtractCustomerDataWithNullIdentifier() {
        // When & Then
        ExtractedCustomerDataDTO result = serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, null);

        assertThat(result).isNull();
    }

    @Test
    void testExtractCustomerDataWithEmptyPayments() {
        // Given
        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(new ArrayList<>());

        when(paymentPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);

        // When & Then
        assertThatThrownBy(() -> serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void testExtractCustomerDataWithNullPayments() {
        // Given
        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(null);

        when(paymentPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);

        // When & Then
        assertThatThrownBy(() -> serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void testExtractCustomerDataWithNullPaymentData() {
        // Given
        PaymentDTO payment = new PaymentDTO();
        payment.setPixAuto(null);
        payment.setJourney(null);

        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(new ArrayList<>());
        paymentInfo.getPayments().add(payment);

        when(paymentPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);

        // When & Then
        assertThatThrownBy(() -> serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void testEnrichCustomerDataWithNullData() {
        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testEnrichCustomerDataWithIncompleteData() {
        // Given
        ExtractedCustomerDataDTO incompleteData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, incompleteData);

        // Then
        assertThat(result).isEqualTo(incompleteData);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "CLARO_MOVEL", "CLARO_RESIDENCIAL"})
    void testEnrichCustomerDataWithVariousSegments(String segment) {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment(segment)
                .mobileBan("BAN123")
                .contractNumber("CONT123")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testExtractCustomerDataWithInvalidPaymentInfoObject() {
        // Given
        when(paymentPort.sendFindByIdentifier(testIdentifier))
                .thenThrow(new RuntimeException("API Error"));

        // When & Then
        assertThatThrownBy(() -> serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void testEnrichCustomerDataResidentialSegment() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_RESIDENCIAL")
                .mobileBan("BAN123")
                .operatorCode("OP01")
                .cityCode("CITY01")
                .contractNumber("CONT123")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichMobileDataWithoutMobileBan() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichCustomerDataWithUnsupportedSegment() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("UNKNOWN_SEGMENT")
                .mobileBan("BAN123")
                .criteriosAtendidos(false)
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isEqualTo(customerData);
    }

    @Test
    void testEnrichCustomerDataWithCnpj() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("Empresa LTDA")
                .cnpj("12345678000199")
                .segment("CLARO_MOVEL")
                .mobileBan("BAN123")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testExtractCustomerDataWithSourceNullPaymentInfo() {
        // Given
        when(paymentPort.sendFindByIdentifier(testIdentifier)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void testEnrichCustomerDataMobileSegmentWithValidSubscribers() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("BAN123")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichCustomerDataWithBothMobileAndResidentialIndicators() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .mobileBan("BAN123")
                .operatorCode("OP01")
                .cityCode("CITY01")
                .contractNumber("CONT123")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901", "98765432109", "11144477789"})
    void testEnrichCustomerDataWithVariousCPFs(String cpf) {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf(cpf)
                .segment("CLARO_MOVEL")
                .mobileBan("BAN123")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"BAN001", "BAN002", "BAN123", "BAN999"})
    void testEnrichCustomerDataWithVariousMobileBans(String mobileBan) {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan(mobileBan)
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }
}
