package com.omp.hub.callback.domain.service.impl.notification.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.omp.hub.callback.domain.model.dto.customer.AccountDTO;
import com.omp.hub.callback.domain.model.dto.customer.CustomerMobileDataDTO;
import com.omp.hub.callback.domain.model.dto.customer.CustomerMobileResponse;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
import com.omp.hub.callback.domain.model.dto.customer.MobileSubscriptionDataDTO;
import com.omp.hub.callback.domain.model.dto.customer.SubscriberDTO;
import com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsCustomer;
import com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsData;
import com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsResponse;
import com.omp.hub.callback.domain.ports.client.CustomerContractsSubscribersPort;
import com.omp.hub.callback.domain.ports.client.CustomerMobilePort;
import com.omp.hub.callback.domain.ports.client.CustomerResidentialPort;
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
    private CustomerResidentialPort customerResidentialPort;

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
    private ExtractedCustomerDataDTO customerData;
    private String document;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        document = "12345678901";
        customerData = ExtractedCustomerDataDTO.builder()
                .name("TESTE QA")
                .cpf(document)
                .segment("CLARO_MOVEL")
                .mobileBan("146164452")
                .build();
    }

    @Test
    void testEnrichMobileCustomerData_Success_CriteriaNotMet() {
        // Arrange - Teste quando os critérios NÃO são atendidos
        MobileBillingDetailsResponse billingResponse = createMockBillingResponse();

        when(mobileBillingDetailsPort.getCustomerBillingDetailsByMobileBan(any(UUID.class), eq("146164452")))
                .thenReturn(billingResponse);
        // Não mockar o customerMobilePort para simular que os critérios não são atendidos

        // Act
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Assert
        assertNotNull(result);
        assertEquals("teste@gmail.com", result.getEmail()); // Email deve ser obtido da billing API
        assertNull(result.getMsisdn()); // MSISDN deve ser null quando critérios não são atendidos
        assertEquals("TESTE QA", result.getName());
        assertEquals("146164452", result.getMobileBan());
        assertFalse(result.getCriteriosAtendidos()); // Flag deve ser false
    }

    @Test
    void testEnrichMobileCustomerData_Success_CriteriaMet() {
        // Arrange - Teste quando os critérios SÃO atendidos
        MobileBillingDetailsResponse billingResponse = createMockBillingResponse();
        CustomerMobileResponse subscribersResponse = createMockSubscribersResponse();

        when(mobileBillingDetailsPort.getCustomerBillingDetailsByMobileBan(any(UUID.class), eq("146164452")))
                .thenReturn(billingResponse);
        when(customerMobilePort.send(any(UUID.class), eq(document), eq("ATIVO")))
                .thenReturn(subscribersResponse);

        // Act
        ExtractedCustomerDataDTO result = service.enrichCustomerData(uuid, customerData);

        // Assert
        assertNotNull(result);
        assertEquals("teste@gmail.com", result.getEmail()); // Email da billing API
        assertEquals("11992212346", result.getMsisdn()); // MSISDN da subscribers API quando critérios atendidos
        assertEquals("TESTE QA", result.getName());
        assertEquals("146164452", result.getMobileBan());
        assertTrue(result.getCriteriosAtendidos()); // Flag deve ser true
    }

    private MobileBillingDetailsResponse createMockBillingResponse() {
        MobileBillingDetailsCustomer customer = MobileBillingDetailsCustomer.builder()
                .contactEmail("teste@gmail.com")
                .name("TESTE QA")
                .cpf("12345678901")
                .build();

        MobileBillingDetailsData data = MobileBillingDetailsData.builder()
                .customer(customer)
                .build();

        return MobileBillingDetailsResponse.builder()
                .apiVersion("1;2023-06-28")
                .transactionId("297f89d7-726c-43e2-b078-bb391ce535ec")
                .data(data)
                .build();
    }

    private CustomerMobileResponse createMockSubscribersResponse() {
        // Criar subscriber que atende todos os critérios
        AccountDTO account = AccountDTO.builder()
                .mobileBan("146164452") // Mesmo mobileBan do customerData
                .build();

        CustomerMobileDataDTO customer = CustomerMobileDataDTO.builder()
                .account(account)
                .build();

        SubscriberDTO subscriber = SubscriberDTO.builder()
                .msisdn("11992212346")
                .name("TESTE QA") // Mesmo nome do customerData
                .customer(customer)
                .build();

        MobileSubscriptionDataDTO data = MobileSubscriptionDataDTO.builder()
                .subscribers(Arrays.asList(subscriber))
                .totalRecords("1")
                .build();

        return CustomerMobileResponse.builder()
                .apiVersion("1.0")
                .transactionId("test-transaction-id")
                .data(data)
                .build();
    }
}