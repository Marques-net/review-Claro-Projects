package com.omp.hub.callback.domain.service.notification.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.service.impl.notification.impl.CustomerDataExtractionServiceImpl;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
import com.omp.hub.callback.domain.model.dto.customer.CustomerMobileResponse;
import com.omp.hub.callback.domain.model.dto.customer.SubscriberDTO;
import com.omp.hub.callback.domain.model.dto.customer.CustomerMobileDataDTO;
import com.omp.hub.callback.domain.model.dto.customer.AccountDTO;
import com.omp.hub.callback.domain.model.dto.customer.MobileSubscriptionDataDTO;
import com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsResponse;
import com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsData;
import com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsCustomer;
import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersContract;
import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersData;
import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersResponse;
import com.omp.hub.callback.domain.ports.client.CustomerMobilePort;
import com.omp.hub.callback.domain.ports.client.CustomerContractsSubscribersPort;
import com.omp.hub.callback.domain.ports.client.MobileBillingDetailsPort;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.service.impl.notification.util.CustomerContactExtractorUtil;

@ExtendWith(MockitoExtension.class)
class CustomerDataExtractionServiceImplTest {

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

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CustomerDataExtractionServiceImpl service;

    private UUID uuid;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
    }

    @Test
    void enrichCustomerData_WithResidentialSegmentSuccess_ShouldEnrichWithNewAPI() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("Maria")
                .cnpj("12345678000199")
                .segment("CLARO_RESIDENCIAL")
                .contractNumber("CONTR123")
                .build();

        // Mock new API responses
        var contractsResponse = mock(CustomerContractsSubscribersResponse.class);
        var contractsData = mock(CustomerContractsSubscribersData.class);
        var contract = mock(CustomerContractsSubscribersContract.class);

        when(customerContractsSubscribersPort.send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class))).thenReturn(contractsResponse);
        when(contractsResponse.getData()).thenReturn(contractsData);
        when(contractsData.getContracts()).thenReturn(List.of(contract));
        when(contract.getStatus()).thenReturn("CONECTADO");
        when(contract.getContractId()).thenReturn("CONTR123");
        when(contract.getFirstName()).thenReturn("Maria");
        when(contract.getEmailAddress()).thenReturn("maria@example.com");
        when(customerContactExtractorUtil.extractMobilePhoneFromContracts(contractsResponse)).thenReturn("999999999");

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria");
        assertThat(result.getEmail()).isEqualTo("maria@example.com");
        assertThat(result.getMsisdn()).isEqualTo("999999999");
        assertThat(result.getCriteriosAtendidos()).isTrue();

        verify(customerContractsSubscribersPort).send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class));
        verify(customerContactExtractorUtil).extractMobilePhoneFromContracts(contractsResponse);
    }

    @Test
    void enrichCustomerData_WithResidentialSegmentStatusNotConnected_ShouldNotEnrich() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("Maria Santos Silva")
                .cnpj("12345678000199")
                .segment("CLARO_RESIDENCIAL")
                .contractNumber("CONTR123")
                .build();

        // Mock - status not connected
        var contractsResponse = mock(CustomerContractsSubscribersResponse.class);
        var contractsData = mock(CustomerContractsSubscribersData.class);
        var contract = mock(CustomerContractsSubscribersContract.class);

        when(customerContractsSubscribersPort.send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class))).thenReturn(contractsResponse);
        when(contractsResponse.getData()).thenReturn(contractsData);
        when(contractsData.getContracts()).thenReturn(List.of(contract));
        when(contract.getStatus()).thenReturn("DESCONECTADO"); // Status diferente de CONECTADO
        when(contract.getContractId()).thenReturn("CONTR123");
        when(contract.getFirstName()).thenReturn("Maria");

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria Santos Silva");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getMsisdn()).isNull();
        assertThat(result.getCriteriosAtendidos()).isFalse();

        verify(customerContractsSubscribersPort).send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class));
    }

    @Test
    void enrichCustomerData_WithResidentialSegmentContractIdMismatch_ShouldNotEnrich() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("Maria Santos Silva")
                .cnpj("12345678000199")
                .segment("CLARO_RESIDENCIAL")
                .contractNumber("CONTR123")
                .build();

        // Mock - contract id mismatch
        var contractsResponse = mock(CustomerContractsSubscribersResponse.class);
        var contractsData = mock(CustomerContractsSubscribersData.class);
        var contract = mock(CustomerContractsSubscribersContract.class);

        when(customerContractsSubscribersPort.send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class))).thenReturn(contractsResponse);
        when(contractsResponse.getData()).thenReturn(contractsData);
        when(contractsData.getContracts()).thenReturn(List.of(contract));
        when(contract.getStatus()).thenReturn("CONECTADO");
        when(contract.getContractId()).thenReturn("DIFFERENT_CONTRACT"); // Contract ID diferente
        when(contract.getFirstName()).thenReturn("Maria");

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria Santos Silva");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getMsisdn()).isNull();
        assertThat(result.getCriteriosAtendidos()).isFalse();

        verify(customerContractsSubscribersPort).send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class));
    }

    @Test
    void enrichCustomerData_WithResidentialSegmentNameMismatch_ShouldNotEnrich() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("Maria Santos Silva")
                .cnpj("12345678000199")
                .segment("CLARO_RESIDENCIAL")
                .contractNumber("CONTR123")
                .build();

        // Mock - name mismatch
        var contractsResponse = mock(CustomerContractsSubscribersResponse.class);
        var contractsData = mock(CustomerContractsSubscribersData.class);
        var contract = mock(CustomerContractsSubscribersContract.class);

        when(customerContractsSubscribersPort.send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class))).thenReturn(contractsResponse);
        when(contractsResponse.getData()).thenReturn(contractsData);
        when(contractsData.getContracts()).thenReturn(List.of(contract));
        when(contract.getStatus()).thenReturn("CONECTADO");
        when(contract.getContractId()).thenReturn("CONTR123");
        when(contract.getFirstName()).thenReturn("JOAO"); // Nome diferente - mismatch

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria Santos Silva");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getMsisdn()).isNull();
        assertThat(result.getCriteriosAtendidos()).isFalse();

        verify(customerContractsSubscribersPort).send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class));
    }

    @Test
    void enrichCustomerData_WithResidentialSegmentError_ShouldReturnUnchanged() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("Maria Santos Silva")
                .cnpj("12345678000199")
                .segment("CLARO_RESIDENCIAL")
                .contractNumber("CONTR123")
                .build();

        when(customerContractsSubscribersPort.send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria Santos Silva");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getMsisdn()).isNull();
        assertThat(result.getCriteriosAtendidos()).isFalse();

        verify(customerContractsSubscribersPort).send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class));
    }

    @Test
    void enrichCustomerData_WithMobileSegment_ShouldEnrichSuccessfully() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        // Mock billing details
        MobileBillingDetailsResponse billingResponse = mock(MobileBillingDetailsResponse.class);
        MobileBillingDetailsData billingData = mock(MobileBillingDetailsData.class);
        MobileBillingDetailsCustomer billingCustomer = mock(MobileBillingDetailsCustomer.class);
        
        when(mobileBillingDetailsPort.getCustomerBillingDetailsByMobileBan(any(UUID.class), eq("123456789")))
                .thenReturn(billingResponse);
        when(billingResponse.getData()).thenReturn(billingData);
        when(billingData.getCustomer()).thenReturn(billingCustomer);
        when(billingCustomer.getContactEmail()).thenReturn("joao@example.com");

        // Mock mobile subscribers
        CustomerMobileResponse mobileResponse = mock(CustomerMobileResponse.class);
        MobileSubscriptionDataDTO mobileData = mock(MobileSubscriptionDataDTO.class);
        SubscriberDTO subscriber = mock(SubscriberDTO.class);
        CustomerMobileDataDTO mobileCustomer = mock(CustomerMobileDataDTO.class);
        AccountDTO account = mock(AccountDTO.class);
        
        when(customerMobilePort.send(any(UUID.class), eq("12345678901"), eq("ATIVO")))
                .thenReturn(mobileResponse);
        when(mobileResponse.getData()).thenReturn(mobileData);
        when(mobileData.getSubscribers()).thenReturn(List.of(subscriber));
        when(subscriber.getCustomer()).thenReturn(mobileCustomer);
        when(mobileCustomer.getAccount()).thenReturn(account);
        when(account.getMobileBan()).thenReturn("123456789");
        when(subscriber.getName()).thenReturn("João Silva");
        when(subscriber.getMsisdn()).thenReturn("11999999999");

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("joao@example.com");
        assertThat(result.getMsisdn()).isEqualTo("11999999999");
        assertThat(result.getCriteriosAtendidos()).isTrue();

        verify(mobileBillingDetailsPort).getCustomerBillingDetailsByMobileBan(any(UUID.class), eq("123456789"));
        verify(customerMobilePort).send(any(UUID.class), eq("12345678901"), eq("ATIVO"));
    }

    @Test
    void enrichCustomerData_WithMobileSegmentMobileBanMismatch_ShouldNotSetCriteria() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        // Mock mobile subscribers with different mobileBan
        CustomerMobileResponse mobileResponse = mock(CustomerMobileResponse.class);
        MobileSubscriptionDataDTO mobileData = mock(MobileSubscriptionDataDTO.class);
        SubscriberDTO subscriber = mock(SubscriberDTO.class);
        CustomerMobileDataDTO mobileCustomer = mock(CustomerMobileDataDTO.class);
        AccountDTO account = mock(AccountDTO.class);
        
        when(customerMobilePort.send(any(UUID.class), eq("12345678901"), eq("ATIVO")))
                .thenReturn(mobileResponse);
        when(mobileResponse.getData()).thenReturn(mobileData);
        when(mobileData.getSubscribers()).thenReturn(List.of(subscriber));
        when(subscriber.getCustomer()).thenReturn(mobileCustomer);
        when(mobileCustomer.getAccount()).thenReturn(account);
        when(account.getMobileBan()).thenReturn("987654321"); // Different mobileBan
        when(subscriber.getName()).thenReturn("João Silva");

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMsisdn()).isNull();
        assertThat(result.getCriteriosAtendidos()).isFalse();

        verify(customerMobilePort).send(any(UUID.class), eq("12345678901"), eq("ATIVO"));
    }

    @Test
    void enrichCustomerData_WithMobileSegmentNameMismatch_ShouldNotSetCriteria() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        // Mock mobile subscribers with different name
        CustomerMobileResponse mobileResponse = mock(CustomerMobileResponse.class);
        MobileSubscriptionDataDTO mobileData = mock(MobileSubscriptionDataDTO.class);
        SubscriberDTO subscriber = mock(SubscriberDTO.class);
        CustomerMobileDataDTO mobileCustomer = mock(CustomerMobileDataDTO.class);
        AccountDTO account = mock(AccountDTO.class);
        
        when(customerMobilePort.send(any(UUID.class), eq("12345678901"), eq("ATIVO")))
                .thenReturn(mobileResponse);
        when(mobileResponse.getData()).thenReturn(mobileData);
        when(mobileData.getSubscribers()).thenReturn(List.of(subscriber));
        when(subscriber.getCustomer()).thenReturn(mobileCustomer);
        when(mobileCustomer.getAccount()).thenReturn(account);
        when(account.getMobileBan()).thenReturn("123456789");
        when(subscriber.getName()).thenReturn("Maria Santos"); // Different name

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMsisdn()).isNull();
        assertThat(result.getCriteriosAtendidos()).isFalse();

        verify(customerMobilePort).send(any(UUID.class), eq("12345678901"), eq("ATIVO"));
    }

    @Test
    void enrichCustomerData_WithMobileSegmentBillingError_ShouldContinueWithoutEmail() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        when(mobileBillingDetailsPort.getCustomerBillingDetailsByMobileBan(any(UUID.class), eq("123456789")))
                .thenThrow(new RuntimeException("Billing service unavailable"));

        // Mock mobile subscribers
        CustomerMobileResponse mobileResponse = mock(CustomerMobileResponse.class);
        MobileSubscriptionDataDTO mobileData = mock(MobileSubscriptionDataDTO.class);
        SubscriberDTO subscriber = mock(SubscriberDTO.class);
        CustomerMobileDataDTO mobileCustomer = mock(CustomerMobileDataDTO.class);
        AccountDTO account = mock(AccountDTO.class);
        
        when(customerMobilePort.send(any(UUID.class), eq("12345678901"), eq("ATIVO")))
                .thenReturn(mobileResponse);
        when(mobileResponse.getData()).thenReturn(mobileData);
        when(mobileData.getSubscribers()).thenReturn(List.of(subscriber));
        when(subscriber.getCustomer()).thenReturn(mobileCustomer);
        when(mobileCustomer.getAccount()).thenReturn(account);
        when(account.getMobileBan()).thenReturn("123456789");
        when(subscriber.getName()).thenReturn("João Silva");
        when(subscriber.getMsisdn()).thenReturn("11999999999");

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isNull();
        assertThat(result.getMsisdn()).isEqualTo("11999999999");
        assertThat(result.getCriteriosAtendidos()).isTrue();

        verify(mobileBillingDetailsPort).getCustomerBillingDetailsByMobileBan(any(UUID.class), eq("123456789"));
        verify(customerMobilePort).send(any(UUID.class), eq("12345678901"), eq("ATIVO"));
    }

    @Test
    void enrichCustomerData_WithMobileSegmentSubscribersError_ShouldReturnWithoutMsisdn() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .segment("CLARO_MOVEL")
                .mobileBan("123456789")
                .build();

        // Mock billing details
        MobileBillingDetailsResponse billingResponse = mock(MobileBillingDetailsResponse.class);
        MobileBillingDetailsData billingData = mock(MobileBillingDetailsData.class);
        MobileBillingDetailsCustomer billingCustomer = mock(MobileBillingDetailsCustomer.class);
        
        when(mobileBillingDetailsPort.getCustomerBillingDetailsByMobileBan(any(UUID.class), eq("123456789")))
                .thenReturn(billingResponse);
        when(billingResponse.getData()).thenReturn(billingData);
        when(billingData.getCustomer()).thenReturn(billingCustomer);
        when(billingCustomer.getContactEmail()).thenReturn("joao@example.com");

        when(customerMobilePort.send(any(UUID.class), eq("12345678901"), eq("ATIVO")))
                .thenThrow(new RuntimeException("Subscribers service unavailable"));

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("joao@example.com");
        assertThat(result.getMsisdn()).isNull();
        assertThat(result.getCriteriosAtendidos()).isFalse();

        verify(mobileBillingDetailsPort).getCustomerBillingDetailsByMobileBan(any(UUID.class), eq("123456789"));
        verify(customerMobilePort).send(any(UUID.class), eq("12345678901"), eq("ATIVO"));
    }

    @Test
    void enrichCustomerData_WithNullCustomerData_ShouldReturnNull() {
        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void enrichCustomerData_WithIncompleteCustomerData_ShouldReturnUnchanged() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                // Missing required fields
                .build();

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isEqualTo(customerData);
    }

    @Test
    void enrichCustomerData_WithUnsupportedSegment_ShouldReturnUnchanged() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .segment("UNSUPPORTED_SEGMENT")
                .build();

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isEqualTo(customerData);
    }

    @Test
    void enrichCustomerData_WithContractNumberWithoutSegment_ShouldEnrichAsResidential() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("Maria")
                .cnpj("12345678000199")
                .contractNumber("CONTR123")
                .operatorCode("OP01")
                .cityCode("CITY01")
                .build();

        // Mock new API responses
        var contractsResponse = mock(CustomerContractsSubscribersResponse.class);
        var contractsData = mock(CustomerContractsSubscribersData.class);
        var contract = mock(CustomerContractsSubscribersContract.class);

        when(customerContractsSubscribersPort.send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class))).thenReturn(contractsResponse);
        when(contractsResponse.getData()).thenReturn(contractsData);
        when(contractsData.getContracts()).thenReturn(List.of(contract));
        when(contract.getStatus()).thenReturn("CONECTADO");
        when(contract.getContractId()).thenReturn("CONTR123");
        when(contract.getFirstName()).thenReturn("Maria");
        when(contract.getEmailAddress()).thenReturn("maria@example.com");
        when(customerContactExtractorUtil.extractMobilePhoneFromContracts(contractsResponse)).thenReturn("999999999");

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("maria@example.com");
        assertThat(result.getCriteriosAtendidos()).isTrue();

        verify(customerContractsSubscribersPort).send(any(UUID.class), eq("12345678000199"), any(ExtractedCustomerDataDTO.class));
    }

    @Test
    void enrichCustomerData_WithMobileBanWithoutSegment_ShouldEnrichAsMobile() {
        // Given
        ExtractedCustomerDataDTO customerData = ExtractedCustomerDataDTO.builder()
                .name("João Silva")
                .cpf("12345678901")
                .mobileBan("123456789")
                .build();

        // Mock mobile subscribers
        CustomerMobileResponse mobileResponse = mock(CustomerMobileResponse.class);
        MobileSubscriptionDataDTO mobileData = mock(MobileSubscriptionDataDTO.class);
        SubscriberDTO subscriber = mock(SubscriberDTO.class);
        CustomerMobileDataDTO mobileCustomer = mock(CustomerMobileDataDTO.class);
        AccountDTO account = mock(AccountDTO.class);
        
        when(customerMobilePort.send(any(UUID.class), eq("12345678901"), eq("ATIVO")))
                .thenReturn(mobileResponse);
        when(mobileResponse.getData()).thenReturn(mobileData);
        when(mobileData.getSubscribers()).thenReturn(List.of(subscriber));
        when(subscriber.getCustomer()).thenReturn(mobileCustomer);
        when(mobileCustomer.getAccount()).thenReturn(account);
        when(account.getMobileBan()).thenReturn("123456789");
        when(subscriber.getName()).thenReturn("João Silva");
        when(subscriber.getMsisdn()).thenReturn("11999999999");

        // When
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMsisdn()).isEqualTo("11999999999");
        assertThat(result.getCriteriosAtendidos()).isTrue();

        verify(customerMobilePort).send(any(UUID.class), eq("12345678901"), eq("ATIVO"));
    }
}