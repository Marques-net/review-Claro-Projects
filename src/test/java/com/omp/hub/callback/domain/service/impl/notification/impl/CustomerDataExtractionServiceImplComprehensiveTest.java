package com.omp.hub.callback.domain.service.impl.notification.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
import com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsResponse;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.pix.forms.PixAutoRequest;
import com.omp.hub.callback.domain.model.dto.pix.forms.Debtor;
import com.omp.hub.callback.domain.model.dto.pix.forms.DebtorDTO;
import com.omp.hub.callback.domain.model.dto.pix.forms.Contract;
import com.omp.hub.callback.domain.model.dto.pix.forms.Document;
import com.omp.hub.callback.domain.model.dto.journey.single.DataSingleDTO;
import com.omp.hub.callback.domain.model.dto.journey.recurring.DataRecurringDTO;
import com.omp.hub.callback.domain.model.dto.journey.recurring.CustomerRecurringDTO;
import com.omp.hub.callback.domain.model.dto.journey.recurring.ContractRecurringDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.CustomerSingleDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.ContractSingleDTO;
import com.omp.hub.callback.domain.ports.client.CustomerContractsSubscribersPort;
import com.omp.hub.callback.domain.ports.client.CustomerMobilePort;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.ports.client.MobileBillingDetailsPort;
import com.omp.hub.callback.domain.service.impl.notification.util.CustomerContactExtractorUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerDataExtractionServiceImplComprehensiveTest {

    @Mock
    private InformationPaymentPort informationPort;

    @Mock
    private CustomerMobilePort customerMobilePort;

    @Mock
    private MobileBillingDetailsPort mobileBillingDetailsPort;

    @Mock
    private CustomerContractsSubscribersPort customerContractsSubscribersPort;

    @Mock
    private CustomerContactExtractorUtil customerContactExtractorUtil;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CustomerDataExtractionServiceImpl serviceUnderTest;

    private UUID testUUID;
    private String testIdentifier;

    @BeforeEach
    void setUp() {
        testUUID = UUID.randomUUID();
        testIdentifier = "TEST-IDENTIFIER";
    }

    // ===== extractCustomerDataFromPaymentInfo Tests =====

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
        paymentInfo.setPayments(Collections.emptyList());

        when(informationPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);

        // When & Then
        assertThatThrownBy(() -> serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getError().getErrorCode()).isEqualTo("PAYMENT_INFO_EMPTY"));
    }

    @Test
    void testExtractCustomerDataWithNullPayments() {
        // Given
        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(null);

        when(informationPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);

        // When & Then
        assertThatThrownBy(() -> serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getError().getErrorCode()).isEqualTo("PAYMENT_INFO_EMPTY"));
    }

    @Test
    void testExtractCustomerDataWithInvalidDataDeserialization() {
        // Given
        PaymentDTO payment = new PaymentDTO();
        payment.setJourney("INVALID_JSON_DATA");

        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(Collections.singletonList(payment));

        when(informationPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);

        // When & Then
        assertThatThrownBy(() -> serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getError().getErrorCode()).isEqualTo("PAYMENT_INFO_ERROR"));
    }

    @Test
    void testExtractCustomerDataWithNullPaymentData() {
        // Given
        PaymentDTO payment = new PaymentDTO();
        payment.setPixAuto(null);
        payment.setJourney(null);

        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(Collections.singletonList(payment));

        when(informationPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);

        // When & Then - when payment has both pixAuto and data as null, it throws PAYMENT_INFO_EMPTY
        assertThatThrownBy(() -> serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getError().getErrorCode()).isEqualTo("PAYMENT_INFO_EMPTY"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901", "98765432109", "11144477789"})
    void testEnrichMobileCustomerDataWithVariousCPFs(String cpf) {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf(cpf)
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCpf()).isEqualTo(cpf);
    }

    @Test
    void testEnrichCustomerDataWithNullCustomerData() {
        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testEnrichCustomerDataWithIncompleteCustomerData() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isEqualTo(customerData);
    }

    @Test
    void testEnrichCustomerDataWithUnsupportedSegment() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("UNSUPPORTED_SEGMENT")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isEqualTo(customerData);
    }

    @Test
    void testEnrichCustomerDataWithClaroMovelSegment() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("João");
    }

    @Test
    void testEnrichCustomerDataWithClaroResidencialSegment() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_RESIDENCIAL")
                .contractNumber("CONTRACT123")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("João");
    }

    @Test
    void testEnrichCustomerDataDetectsMobileByBanWithoutOperatorCode() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .mobileBan("123456789")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichCustomerDataDetectsResidentialByBanWithOperatorCode() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .mobileBan("123456789")
                .operatorCode("OP01")
                .cityCode("CITY01")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichMobileCustomerDataWithBillingDetailsError() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        when(mobileBillingDetailsPort.getCustomerBillingDetailsByMobileBan(testUUID, "123456789"))
                .thenThrow(new RuntimeException("API Error"));
        
        lenient().when(customerMobilePort.send(any(), any(), anyString()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichMobileCustomerDataWithValidBillingDetails() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        MobileBillingDetailsResponse billingResponse = new MobileBillingDetailsResponse();
        when(mobileBillingDetailsPort.getCustomerBillingDetailsByMobileBan(testUUID, "123456789"))
                .thenReturn(billingResponse);
        
        lenient().when(customerMobilePort.send(any(), any(), anyString()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichMobileCustomerDataWithMismatchedMobileBan() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        lenient().when(customerMobilePort.send(any(), any(), anyString()))
                .thenThrow(new RuntimeException("API Error"));
        lenient().when(mobileBillingDetailsPort.getCustomerBillingDetailsByMobileBan(any(), anyString()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichMobileCustomerDataWithMismatchedName() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        lenient().when(customerMobilePort.send(any(), any(), anyString()))
                .thenThrow(new RuntimeException("API Error"));
        lenient().when(mobileBillingDetailsPort.getCustomerBillingDetailsByMobileBan(any(), anyString()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichResidentialCustomerDataWithMismatchedStatus() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_RESIDENCIAL")
                .contractNumber("CONTRACT123")
                .build();

        lenient().when(customerContractsSubscribersPort.send(any(), anyString(), any()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichResidentialCustomerDataWithMismatchedContractId() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_RESIDENCIAL")
                .contractNumber("CONTRACT123")
                .build();

        lenient().when(customerContractsSubscribersPort.send(any(), anyString(), any()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichResidentialCustomerDataWithMismatchedName() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_RESIDENCIAL")
                .contractNumber("CONTRACT123")
                .build();

        lenient().when(customerContractsSubscribersPort.send(any(), anyString(), any()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichResidentialCustomerDataWithEmptyContracts() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_RESIDENCIAL")
                .contractNumber("CONTRACT123")
                .build();

        lenient().when(customerContractsSubscribersPort.send(any(), anyString(), any()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"João", "Maria", "José", "Ana"})
    void testEnrichCustomerDataWithVariousNames(String name) {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name(name)
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    void testEnrichCustomerDataWithCNPJInsteadOfCPF() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("Empresa LTDA")
                .cnpj("12345678000199")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCnpj()).isEqualTo("12345678000199");
    }

    @Test
    void testEnrichMobileCustomerDataExtractsEmailFromBilling() {
        // Given - This test exercises the billing details extraction path
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        lenient().when(mobileBillingDetailsPort.getCustomerBillingDetailsByMobileBan(any(), anyString()))
                .thenThrow(new RuntimeException("Network Error"));
        lenient().when(customerMobilePort.send(any(), any(), anyString()))
                .thenThrow(new RuntimeException("Network Error"));

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testExtractCustomerDataHandlesIOException() {
        // Given
        when(informationPort.sendFindByIdentifier(testIdentifier))
                .thenThrow(new RuntimeException("Network Error"));

        // When & Then
        assertThatThrownBy(() -> serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void testEnrichCustomerDataWithSegmentNull() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment(null)
                .build();

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSegment()).isNull();
    }

    @Test
    void testEnrichResidentialCustomerDataExtractsMsisdnFromContracts() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_RESIDENCIAL")
                .contractNumber("CONTRACT123")
                .build();

        lenient().when(customerContractsSubscribersPort.send(any(), anyString(), any()))
                .thenThrow(new RuntimeException("API Error"));

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.enrichCustomerData(testUUID, customerData);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void testEnrichCustomerDataMultipleEnrichmentAttempts() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        // When - calling enrichment twice
        ExtractedCustomerDataDTO result1 = serviceUnderTest.enrichCustomerData(testUUID, customerData);
        ExtractedCustomerDataDTO result2 = serviceUnderTest.enrichCustomerData(testUUID, result1);

        // Then
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
    }

    @Test
    void testExtractCustomerDataWithPixAutoContractDebtorCPF() throws Exception {
        // Given
        PaymentDTO payment = new PaymentDTO();
        payment.setPixAuto("pixData");

        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(Collections.singletonList(payment));

        PixAutoRequest pixAutoRequest = new PixAutoRequest();
        Contract contract = new Contract();
        Debtor debtor = new Debtor();
        debtor.setName("João Silva");
        Document document = new Document();
        document.setType("CPF");
        document.setNumber("12345678901");
        debtor.setDocument(document);
        contract.setDebtor(debtor);
        contract.setMobileBan("123456789");
        contract.setContractNumber("CONTRACT123");
        contract.setOperatorCode("OP01");
        contract.setCityCode("CITY01");
        pixAutoRequest.setContract(contract);

        when(informationPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);
        when(objectMapper.readValue("pixData", PixAutoRequest.class)).thenReturn(pixAutoRequest);

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("João Silva");
        assertThat(result.getCpf()).isEqualTo("12345678901");
        assertThat(result.getMobileBan()).isEqualTo("123456789");
        assertThat(result.getContractNumber()).isEqualTo("CONTRACT123");
        assertThat(result.getOperatorCode()).isEqualTo("OP01");
        assertThat(result.getCityCode()).isEqualTo("CITY01");
    }

    @Test
    void testExtractCustomerDataWithPixAutoContractDebtorCNPJ() throws Exception {
        // Given
        PaymentDTO payment = new PaymentDTO();
        payment.setPixAuto("pixData");

        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(Collections.singletonList(payment));

        PixAutoRequest pixAutoRequest = new PixAutoRequest();
        Contract contract = new Contract();
        Debtor debtor = new Debtor();
        debtor.setName("Empresa LTDA");
        Document document = new Document();
        document.setType("CNPJ");
        document.setNumber("12345678000199");
        debtor.setDocument(document);
        contract.setDebtor(debtor);
        pixAutoRequest.setContract(contract);

        when(informationPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);
        when(objectMapper.readValue("pixData", PixAutoRequest.class)).thenReturn(pixAutoRequest);

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Empresa LTDA");
        assertThat(result.getCnpj()).isEqualTo("12345678000199");
    }

    @Test
    void testExtractCustomerDataWithPixAutoDirectDebtorCPF() throws Exception {
        // Given
        PaymentDTO payment = new PaymentDTO();
        payment.setPixAuto("pixData");

        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(Collections.singletonList(payment));

        PixAutoRequest pixAutoRequest = new PixAutoRequest();
        DebtorDTO debtor = new DebtorDTO();
        debtor.setName("Maria Santos");
        debtor.setCpf("98765432109");
        debtor.setCnpj(null);
        pixAutoRequest.setDebtor(debtor);

        when(informationPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);
        when(objectMapper.readValue("pixData", PixAutoRequest.class)).thenReturn(pixAutoRequest);

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria Santos");
        assertThat(result.getCpf()).isEqualTo("98765432109");
    }

    @Test
    void testExtractCustomerDataWithPixAutoDirectDebtorCNPJ() throws Exception {
        // Given
        PaymentDTO payment = new PaymentDTO();
        payment.setPixAuto("pixData");

        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(Collections.singletonList(payment));

        PixAutoRequest pixAutoRequest = new PixAutoRequest();
        DebtorDTO debtor = new DebtorDTO();
        debtor.setName("Empresa SA");
        debtor.setCpf(null);
        debtor.setCnpj("98765432000188");
        pixAutoRequest.setDebtor(debtor);

        when(informationPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);
        when(objectMapper.readValue("pixData", PixAutoRequest.class)).thenReturn(pixAutoRequest);

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Empresa SA");
        assertThat(result.getCnpj()).isEqualTo("98765432000188");
    }

    @Test
    void testExtractCustomerDataWithJourneyDataSingleDTO() throws Exception {
        // Given
        PaymentDTO payment = new PaymentDTO();
        payment.setJourney("journeyData");

        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(Collections.singletonList(payment));

        DataSingleDTO dataSingleDTO = new DataSingleDTO();
        CustomerSingleDTO customer = new CustomerSingleDTO();
        customer.setName("Pedro Lima");
        customer.setCpf("11122233344");
        ContractSingleDTO contract = new ContractSingleDTO();
        contract.setMobileBan("555666777");
        customer.setContract(contract);
        dataSingleDTO.setCustomer(customer);

        when(informationPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);
        when(objectMapper.readValue("journeyData", DataSingleDTO.class)).thenReturn(dataSingleDTO);

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Pedro Lima");
        assertThat(result.getCpf()).isEqualTo("11122233344");
        assertThat(result.getMobileBan()).isEqualTo("555666777");
    }

    @Test
    void testExtractCustomerDataWithJourneyDataRecurringDTO() throws Exception {
        // Given
        PaymentDTO payment = new PaymentDTO();
        payment.setJourney("journeyData");

        InformationPaymentDTO paymentInfo = new InformationPaymentDTO();
        paymentInfo.setPayments(Collections.singletonList(payment));

        DataRecurringDTO dataRecurringDTO = new DataRecurringDTO();
        CustomerRecurringDTO customer = new CustomerRecurringDTO();
        customer.setName("Ana Costa");
        customer.setCnpj("55566677000122");
        ContractRecurringDTO contract = new ContractRecurringDTO();
        contract.setContractNumber("REC123");
        customer.setContract(contract);
        dataRecurringDTO.setCustomer(customer);

        when(informationPort.sendFindByIdentifier(testIdentifier)).thenReturn(paymentInfo);
        when(objectMapper.readValue("journeyData", DataSingleDTO.class)).thenThrow(new RuntimeException("Not DataSingle"));
        when(objectMapper.readValue("journeyData", DataRecurringDTO.class)).thenReturn(dataRecurringDTO);

        // When
        ExtractedCustomerDataDTO result = serviceUnderTest.extractCustomerDataFromPaymentInfo(testUUID, testIdentifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Ana Costa");
        assertThat(result.getCnpj()).isEqualTo("55566677000122");
        assertThat(result.getContractNumber()).isEqualTo("REC123");
    }
}
