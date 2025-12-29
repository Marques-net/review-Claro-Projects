package com.omp.hub.callback.domain.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.CallbackDTO;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.*;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.*;
import com.omp.hub.callback.domain.model.dto.sap.payments.OrderReceiptDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsRequest;
import com.omp.hub.callback.domain.service.check.CheckTypeObjectService;

@ExtendWith(MockitoExtension.class)
class GenerateSapPaymentRequestServiceImplTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private CheckTypeObjectService validateService;

    @InjectMocks
    private com.omp.hub.callback.domain.service.generate.impl.GenerateSapPaymentRequestServiceImpl service;

    private UUID uuid;
    private InformationPaymentDTO info;
    private DataSingleDTO dataSingleDTO;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();

        CustomerSingleDTO customer = CustomerSingleDTO.builder()
                .name("João Silva")
                .cpf("12345678900")
                .build();

        PaymentSingleDTO payment = PaymentSingleDTO.builder()
                .salesOrderId("12345")
                .value("100.00")
                .cardData(CardDataDTO.builder()
                        .build())
                .build();

        ComplementaryDataDTO complementaryData = ComplementaryDataDTO.builder()
                .products(Arrays.asList(
                        ProductDTO.builder()
                                .sku("SKU001")
                                .amount("2")
                                .value("50.00")
                                .discountValue("0.00")
                                .totalDiscountValue("0.00")
                                .serialNumber("SN001")
                                .build()
                ))
                .build();

        FraudAnalysisDataDTO fraudAnalysisData = FraudAnalysisDataDTO.builder()
                .complementaryData(complementaryData)
                .build();

        dataSingleDTO = new DataSingleDTO();
        ReflectionTestUtils.setField(dataSingleDTO, "customer", customer);
        ReflectionTestUtils.setField(dataSingleDTO, "payment", payment);
        ReflectionTestUtils.setField(dataSingleDTO, "fraudAnalysisData", fraudAnalysisData);

        info = InformationPaymentDTO.builder()
                .uuid(uuid)
                .store("STORE001")
                .channel("credit")
                .payments(Arrays.asList(
                        PaymentDTO.builder()
                                .journey("{\"customer\":{\"name\":\"João Silva\",\"cpf\":\"12345678900\"},\"payment\":{\"salesOrderId\":\"12345\",\"value\":\"100.00\",\"cardData\":{\"sellerId\":\"seller123\"}},\"fraudAnalysisData\":{\"complementaryData\":{\"products\":[{\"sku\":\"SKU001\",\"amount\":\"2\",\"value\":\"50.00\",\"discountValue\":\"0.00\",\"totalDiscountValue\":\"0.00\",\"serialNumber\":\"SN001\"}]}}}")
                                .build()
                ))
                .build();

        ReflectionTestUtils.setField(service, "atvSimplLoja", "STORE_ATV");
    }

    @Test
    void generateRequest_WithCreditCardCallback_ShouldGenerateSapRequest() throws Exception {
        CreditCardCallbackRequest creditCallback = CreditCardCallbackRequest.builder()
                .orderDate("15/10/2023")
                .value(BigDecimal.valueOf(100.00))
                .card("1234-5678")
                .flag("VISA")
                .build();

        String callbackJson = "{\"orderDate\":\"15/10/2023\",\"value\":100.00,\"card\":\"1234-5678\",\"flag\":\"VISA\"}";

        when(validateService.isValid(callbackJson, CreditCardCallbackRequest.class)).thenReturn(true);
        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenReturn(callbackJson);
        when(mapper.readValue(callbackJson, CreditCardCallbackRequest.class)).thenReturn(creditCallback);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);

        SapPaymentsRequest result = service.generateRequest(mock(CallbackDTO.class), info);

        assertNotNull(result);
        assertNotNull(result.getData());
        assertNotNull(result.getData().getOrder());
        assertEquals("12345", result.getData().getOrder().getId());
        assertEquals("STORE001", result.getData().getOrder().getBusinessLocationId());
    }

    @Test
    void generateRequest_WithNullSalesOrderId_ShouldUseIdentifier() throws Exception {
        // Given
        dataSingleDTO.getPayment().setSalesOrderId(null);
        info.setIdentifier("TEST-IDENTIFIER");

        CreditCardCallbackRequest creditCallback = CreditCardCallbackRequest.builder()
                .orderDate("15/10/2023")
                .value(BigDecimal.valueOf(100.00))
                .card("1234-5678")
                .flag("VISA")
                .build();

        String callbackJson = "{\"orderDate\":\"15/10/2023\",\"value\":100.00,\"card\":\"1234-5678\",\"flag\":\"VISA\"}";

        when(validateService.isValid(callbackJson, CreditCardCallbackRequest.class)).thenReturn(true);
        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenReturn(callbackJson);
        when(mapper.readValue(callbackJson, CreditCardCallbackRequest.class)).thenReturn(creditCallback);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);

        // When
        SapPaymentsRequest result = service.generateRequest(mock(CallbackDTO.class), info);

        // Then
        assertNotNull(result);
        assertEquals("TEST-IDENTIFIER", result.getData().getOrder().getId());
        assertEquals("TEST-IDENTIFIER", result.getData().getOrder().getPosInfo().getTaxCouponNumber());
    }

    @Test
    void generateRequest_WithNullPayments_ShouldThrowBusinessException() throws Exception {
        InformationPaymentDTO infoWithNullPayments = InformationPaymentDTO.builder()
                .uuid(uuid)
                .store("STORE001")
                .channel("credit")
                .payments(null)
                .build();

        assertThrows(BusinessException.class, () -> service.generateRequest(mock(CallbackDTO.class), infoWithNullPayments));
    }

    @Test
    void generateRequest_WithEmptyPayments_ShouldThrowBusinessException() throws Exception {
        InformationPaymentDTO infoWithEmptyPayments = InformationPaymentDTO.builder()
                .uuid(uuid)
                .store("STORE001")
                .channel("credit")
                .payments(Arrays.asList())
                .build();

        assertThrows(BusinessException.class, () -> service.generateRequest(mock(CallbackDTO.class), infoWithEmptyPayments));
    }

    @Test
    void generateRequest_WithAtivacaoSimplificadaChannel_ShouldUseAtvSimplLoja() throws Exception {
        InformationPaymentDTO infoAtvSimpl = InformationPaymentDTO.builder()
                .uuid(uuid)
                .store(null)
                .channel("ativacaosimplificada")
                .payments(Arrays.asList(
                        PaymentDTO.builder()
                                .journey("{\"customer\":{\"name\":\"João Silva\",\"cpf\":\"12345678900\"},\"payment\":{\"salesOrderId\":\"12345\",\"value\":\"100.00\",\"cardData\":{\"sellerId\":\"seller123\"}},\"fraudAnalysisData\":{\"complementaryData\":{\"products\":[{\"sku\":\"SKU001\",\"amount\":\"2\",\"value\":\"50.00\",\"discountValue\":\"0.00\",\"totalDiscountValue\":\"0.00\",\"serialNumber\":\"SN001\"}]}}}")
                                .build()
                ))
                .build();

        CreditCardCallbackRequest creditCallback = CreditCardCallbackRequest.builder()
                .orderDate("15/10/2023")
                .value(BigDecimal.valueOf(100.00))
                .card("1234-5678")
                .flag("VISA")
                .build();

        String callbackJson = "{\"orderDate\":\"15/10/2023\",\"value\":100.00,\"card\":\"1234-5678\",\"flag\":\"VISA\"}";

        when(validateService.isValid(callbackJson, CreditCardCallbackRequest.class)).thenReturn(true);
        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenReturn(callbackJson);
        when(mapper.readValue(callbackJson, CreditCardCallbackRequest.class)).thenReturn(creditCallback);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);

        SapPaymentsRequest result = service.generateRequest(mock(CallbackDTO.class), infoAtvSimpl);

        assertNotNull(result);
        assertEquals("STORE_ATV", result.getData().getOrder().getBusinessLocationId());
    }

    @Test
    void generateRequest_WithSolarChannel_ShouldUseAtvSimplLoja() throws Exception {
        InformationPaymentDTO infoSolar = InformationPaymentDTO.builder()
                .uuid(uuid)
                .store("DEFAULT")
                .channel("solar")
                .payments(Arrays.asList(
                        PaymentDTO.builder()
                                .journey("{\"customer\":{\"name\":\"João Silva\",\"cpf\":\"12345678900\"},\"payment\":{\"salesOrderId\":\"12345\",\"value\":\"100.00\",\"cardData\":{\"sellerId\":\"seller123\"}},\"fraudAnalysisData\":{\"complementaryData\":{\"products\":[{\"sku\":\"SKU001\",\"amount\":\"2\",\"value\":\"50.00\",\"discountValue\":\"0.00\",\"totalDiscountValue\":\"0.00\",\"serialNumber\":\"SN001\"}]}}}")
                                .build()
                ))
                .build();

        CreditCardCallbackRequest creditCallback = CreditCardCallbackRequest.builder()
                .orderDate("15/10/2023")
                .value(BigDecimal.valueOf(100.00))
                .card("1234-5678")
                .flag("VISA")
                .build();

        String callbackJson = "{\"orderDate\":\"15/10/2023\",\"value\":100.00,\"card\":\"1234-5678\",\"flag\":\"VISA\"}";

        when(validateService.isValid(callbackJson, CreditCardCallbackRequest.class)).thenReturn(true);
        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenReturn(callbackJson);
        when(mapper.readValue(callbackJson, CreditCardCallbackRequest.class)).thenReturn(creditCallback);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);

        SapPaymentsRequest result = service.generateRequest(mock(CallbackDTO.class), infoSolar);

        assertNotNull(result);
        assertEquals("STORE_ATV", result.getData().getOrder().getBusinessLocationId());
    }

    @Test
    void generateRequest_WithTefWebCallback_ShouldGenerateSapRequest() throws Exception {
        TefWebCallbackRequest tefwebCallback = TefWebCallbackRequest.builder()
                .sales(Arrays.asList(
                        SalesDTO.builder()
                                .order(OrderDTO.builder()
                                        .totalValue("164900") // 1649.00 em centavos
                                        .build())
                                .transactions(Arrays.asList(
                                        TransactionsDTO.builder()
                                                .transactionData(TransactionDataDTO.builder()
                                                        .transactionDate("15/10/2023")
                                                        .hour("14:30:00")
                                                        .value("150.00")
                                                        .paymentType(PaymentTypeDTO.builder()
                                                                .paymentType("CARTAO")
                                                                .detailPaymentType("CREDITO")
                                                                .numberInstallmentsPayment("3")
                                                                .build())
                                                        .build())
                                                .eletronicTransactionData(EletronicTransactionDataDTO.builder()
                                                        .cardBin("123456")
                                                        .cardEmbossing("7890")
                                                        .flagCode("001")
                                                        .flag("VISA")
                                                        .acquirator(AcquiratorDTO.builder()
                                                                .code("001")
                                                                .description("CIELO")
                                                                .build())
                                                        .hostNsu("123456789")
                                                        .idSitef("987654321")
                                                        .build())
                                                .build()))
                                .build()))
                .build();

        String callbackJson = "{\"sales\":[{\"order\":{\"totalValue\":\"164900\"},\"transactions\":[{\"transactionData\":{\"transactionDate\":\"15/10/2023\",\"hour\":\"14:30:00\",\"value\":\"150.00\",\"paymentType\":{\"paymentType\":\"CARTAO\",\"detailPaymentType\":\"CREDITO\",\"numberInstallmentsPayment\":\"3\"}},\"eletronicTransactionData\":{\"cardBin\":\"123456\",\"cardEmbossing\":\"7890\",\"flagCode\":\"001\",\"flag\":\"VISA\",\"acquirator\":{\"code\":\"001\",\"description\":\"CIELO\"},\"hostNsu\":\"123456789\",\"idSitef\":\"987654321\"}}]}]}";

        when(validateService.isValid(callbackJson, CreditCardCallbackRequest.class)).thenReturn(false);
        when(validateService.isValid(callbackJson, TefWebCallbackRequest.class)).thenReturn(true);
        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenReturn(callbackJson);
        when(mapper.readValue(callbackJson, TefWebCallbackRequest.class)).thenReturn(tefwebCallback);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);

        SapPaymentsRequest result = service.generateRequest(mock(CallbackDTO.class), info);

        assertNotNull(result);
        assertNotNull(result.getData());
        assertNotNull(result.getData().getOrder());
        assertEquals("12345", result.getData().getOrder().getId());
        assertEquals("STORE001", result.getData().getOrder().getBusinessLocationId());
        // Deve usar o valor total da order (1649.00) ao invés do valor da transação (150.00)
        // Deve usar o valor do dataSingleDTO (100.00), não do callback
        assertEquals("100.00", result.getData().getOrder().getTotalAmountReceived());
    }

    @Test
    void generateRequest_WithTefWebCallbackAndNullOrderTotal_ShouldUseTransactionValue() throws Exception {
        TefWebCallbackRequest tefwebCallback = TefWebCallbackRequest.builder()
                .sales(Arrays.asList(
                        SalesDTO.builder()
                                .order(OrderDTO.builder()
                                        .totalValue(null) // null
                                        .build())
                                .transactions(Arrays.asList(
                                        TransactionsDTO.builder()
                                                .transactionData(TransactionDataDTO.builder()
                                                        .transactionDate("15/10/2023")
                                                        .hour("14:30:00")
                                                        .value("150.00")
                                                        .paymentType(PaymentTypeDTO.builder()
                                                                .paymentType("CARTAO")
                                                                .detailPaymentType("CREDITO")
                                                                .numberInstallmentsPayment("1")
                                                                .build())
                                                        .build())
                                                .eletronicTransactionData(EletronicTransactionDataDTO.builder()
                                                        .cardBin("123456")
                                                        .cardEmbossing("7890")
                                                        .flagCode("001")
                                                        .flag("VISA")
                                                        .acquirator(AcquiratorDTO.builder()
                                                                .code("001")
                                                                .description("CIELO")
                                                                .build())
                                                        .hostNsu("123456789")
                                                        .idSitef("987654321")
                                                        .build())
                                                .build()))
                                .build()))
                .build();

        String callbackJson = "{\"sales\":[{\"order\":{\"totalValue\":null},\"transactions\":[{\"transactionData\":{\"transactionDate\":\"15/10/2023\",\"hour\":\"14:30:00\",\"value\":\"150.00\",\"paymentType\":{\"paymentType\":\"CARTAO\",\"detailPaymentType\":\"CREDITO\",\"numberInstallmentsPayment\":\"1\"}},\"eletronicTransactionData\":{\"cardBin\":\"123456\",\"cardEmbossing\":\"7890\",\"flagCode\":\"001\",\"flag\":\"VISA\",\"acquirator\":{\"code\":\"001\",\"description\":\"CIELO\"},\"hostNsu\":\"123456789\",\"idSitef\":\"987654321\"}}]}]}";

        when(validateService.isValid(callbackJson, CreditCardCallbackRequest.class)).thenReturn(false);
        when(validateService.isValid(callbackJson, TefWebCallbackRequest.class)).thenReturn(true);
        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenReturn(callbackJson);
        when(mapper.readValue(callbackJson, TefWebCallbackRequest.class)).thenReturn(tefwebCallback);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);

        SapPaymentsRequest result = service.generateRequest(mock(CallbackDTO.class), info);

        assertNotNull(result);
        // Deve usar o valor do dataSingleDTO (100.00) quando orderTotalValue é null
        assertEquals("100.00", result.getData().getOrder().getTotalAmountReceived());
    }

    @Test
    void generateRequest_WithNullCardData_ShouldUseSystemAsUserName() throws Exception {
        PaymentSingleDTO paymentWithoutCardData = PaymentSingleDTO.builder()
                .salesOrderId("12345")
                .value("100.00")
                .cardData(null) // null
                .build();

        DataSingleDTO dataSingleWithoutCardData = new DataSingleDTO();
        ReflectionTestUtils.setField(dataSingleWithoutCardData, "customer", CustomerSingleDTO.builder()
                .name("João Silva")
                .cpf("12345678900")
                .build());
        ReflectionTestUtils.setField(dataSingleWithoutCardData, "payment", paymentWithoutCardData);
        ReflectionTestUtils.setField(dataSingleWithoutCardData, "fraudAnalysisData", FraudAnalysisDataDTO.builder()
                .complementaryData(ComplementaryDataDTO.builder()
                        .products(Arrays.asList(
                                ProductDTO.builder()
                                        .sku("SKU001")
                                        .amount("2")
                                        .value("50.00")
                                        .discountValue("0.00")
                                        .totalDiscountValue("0.00")
                                        .serialNumber("SN001")
                                        .build()
                        ))
                        .build())
                .build());

        InformationPaymentDTO infoWithoutCardData = InformationPaymentDTO.builder()
                .uuid(uuid)
                .store("STORE001")
                .channel("credit")
                .payments(Arrays.asList(
                        PaymentDTO.builder()
                                .journey("{\"customer\":{\"name\":\"João Silva\",\"cpf\":\"12345678900\"},\"payment\":{\"salesOrderId\":\"12345\",\"value\":\"100.00\"},\"fraudAnalysisData\":{\"complementaryData\":{\"products\":[{\"sku\":\"SKU001\",\"amount\":\"2\",\"value\":\"50.00\",\"discountValue\":\"0.00\",\"totalDiscountValue\":\"0.00\",\"serialNumber\":\"SN001\"}]}}}")
                                .build()
                ))
                .build();

        CreditCardCallbackRequest creditCallback = CreditCardCallbackRequest.builder()
                .orderDate("15/10/2023")
                .value(BigDecimal.valueOf(100.00))
                .card("1234-5678")
                .flag("VISA")
                .build();

        String callbackJson = "{\"orderDate\":\"15/10/2023\",\"value\":100.00,\"card\":\"1234-5678\",\"flag\":\"VISA\"}";

        when(validateService.isValid(callbackJson, CreditCardCallbackRequest.class)).thenReturn(true);
        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenReturn(callbackJson);
        when(mapper.readValue(callbackJson, CreditCardCallbackRequest.class)).thenReturn(creditCallback);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleWithoutCardData);

        SapPaymentsRequest result = service.generateRequest(mock(CallbackDTO.class), infoWithoutCardData);

        assertNotNull(result);
        assertEquals("SYSTEM", result.getData().getOrder().getUserName());
    }

    @Test
    void generateRequest_WithNullCpf_ShouldUseCnpjAsType() throws Exception {
        CustomerSingleDTO customerWithoutCpf = CustomerSingleDTO.builder()
                .name("Empresa XYZ")
                .cpf(null) // null
                .build();

        DataSingleDTO dataSingleWithoutCpf = new DataSingleDTO();
        ReflectionTestUtils.setField(dataSingleWithoutCpf, "customer", customerWithoutCpf);
        ReflectionTestUtils.setField(dataSingleWithoutCpf, "payment", PaymentSingleDTO.builder()
                .salesOrderId("12345")
                .value("100.00")
                .cardData(CardDataDTO.builder()
                        .build())
                .build());
        ReflectionTestUtils.setField(dataSingleWithoutCpf, "fraudAnalysisData", FraudAnalysisDataDTO.builder()
                .complementaryData(ComplementaryDataDTO.builder()
                        .products(Arrays.asList(
                                ProductDTO.builder()
                                        .sku("SKU001")
                                        .amount("2")
                                        .value("50.00")
                                        .discountValue("0.00")
                                        .totalDiscountValue("0.00")
                                        .serialNumber("SN001")
                                        .build()
                        ))
                        .build())
                .build());

        InformationPaymentDTO infoWithoutCpf = InformationPaymentDTO.builder()
                .uuid(uuid)
                .store("STORE001")
                .channel("credit")
                .payments(Arrays.asList(
                        PaymentDTO.builder()
                                .journey("{\"customer\":{\"name\":\"Empresa XYZ\"},\"payment\":{\"salesOrderId\":\"12345\",\"value\":\"100.00\",\"cardData\":{\"sellerId\":\"seller123\"}},\"fraudAnalysisData\":{\"complementaryData\":{\"products\":[{\"sku\":\"SKU001\",\"amount\":\"2\",\"value\":\"50.00\",\"discountValue\":\"0.00\",\"totalDiscountValue\":\"0.00\",\"serialNumber\":\"SN001\"}]}}}")
                                .build()
                ))
                .build();

        CreditCardCallbackRequest creditCallback = CreditCardCallbackRequest.builder()
                .orderDate("15/10/2023")
                .value(BigDecimal.valueOf(100.00))
                .card("1234-5678")
                .flag("VISA")
                .build();

        String callbackJson = "{\"orderDate\":\"15/10/2023\",\"value\":100.00,\"card\":\"1234-5678\",\"flag\":\"VISA\"}";

        when(validateService.isValid(callbackJson, CreditCardCallbackRequest.class)).thenReturn(true);
        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenReturn(callbackJson);
        when(mapper.readValue(callbackJson, CreditCardCallbackRequest.class)).thenReturn(creditCallback);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleWithoutCpf);

        SapPaymentsRequest result = service.generateRequest(mock(CallbackDTO.class), infoWithoutCpf);

        assertNotNull(result);
        assertNotNull(result.getData().getOrder().getCustomer());
        assertNotNull(result.getData().getOrder().getCustomer().getIdentifications());
        assertEquals(1, result.getData().getOrder().getCustomer().getIdentifications().size());
        assertEquals("CNPJ", result.getData().getOrder().getCustomer().getIdentifications().get(0).getType());
    }

    @Test
    void generateRequest_WithSuperTrocaDiscount_ShouldAddPromotionalCoupon() throws Exception {
        PaymentSingleDTO paymentWithDiscount = PaymentSingleDTO.builder()
                .salesOrderId("12345")
                .value("100.00")
                .cardData(CardDataDTO.builder()
                        .build())
                .discounts(Arrays.asList(
                        PaymentDiscountDTO.builder()
                                .id("SUPER_TROCA")
                                .value("50.00")
                                .build()
                ))
                .build();

        DataSingleDTO dataSingleWithDiscount = new DataSingleDTO();
        ReflectionTestUtils.setField(dataSingleWithDiscount, "customer", CustomerSingleDTO.builder()
                .name("João Silva")
                .cpf("12345678900")
                .build());
        ReflectionTestUtils.setField(dataSingleWithDiscount, "payment", paymentWithDiscount);
        ReflectionTestUtils.setField(dataSingleWithDiscount, "fraudAnalysisData", FraudAnalysisDataDTO.builder()
                .complementaryData(ComplementaryDataDTO.builder()
                        .products(Arrays.asList(
                                ProductDTO.builder()
                                        .sku("SKU001")
                                        .amount("2")
                                        .value("50.00")
                                        .discountValue("0.00")
                                        .totalDiscountValue("0.00")
                                        .serialNumber("SN001")
                                        .build()
                        ))
                        .build())
                .build());

        InformationPaymentDTO infoWithDiscount = InformationPaymentDTO.builder()
                .uuid(uuid)
                .store("STORE001")
                .channel("credit")
                .payments(Arrays.asList(
                        PaymentDTO.builder()
                                .journey("{\"customer\":{\"name\":\"João Silva\",\"cpf\":\"12345678900\"},\"payment\":{\"salesOrderId\":\"12345\",\"value\":\"100.00\",\"cardData\":{\"sellerId\":\"seller123\"},\"discounts\":[{\"id\":\"SUPER_TROCA\",\"value\":\"50.00\"}]},\"fraudAnalysisData\":{\"complementaryData\":{\"products\":[{\"sku\":\"SKU001\",\"amount\":\"2\",\"value\":\"50.00\",\"discountValue\":\"0.00\",\"totalDiscountValue\":\"0.00\",\"serialNumber\":\"SN001\"}]}}}")
                                .build()
                ))
                .build();

        CreditCardCallbackRequest creditCallback = CreditCardCallbackRequest.builder()
                .orderDate("15/10/2023")
                .value(BigDecimal.valueOf(100.00))
                .card("1234-5678")
                .flag("VISA")
                .build();

        String callbackJson = "{\"orderDate\":\"15/10/2023\",\"value\":100.00,\"card\":\"1234-5678\",\"flag\":\"VISA\"}";

        when(validateService.isValid(callbackJson, CreditCardCallbackRequest.class)).thenReturn(true);
        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenReturn(callbackJson);
        when(mapper.readValue(callbackJson, CreditCardCallbackRequest.class)).thenReturn(creditCallback);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleWithDiscount);

        SapPaymentsRequest result = service.generateRequest(mock(CallbackDTO.class), infoWithDiscount);

        assertNotNull(result);
        assertNotNull(result.getData().getOrder().getOrderReceipts());
        assertEquals(2, result.getData().getOrder().getOrderReceipts().size()); // original + discount
        
        // Verificar o cupom promocional
        OrderReceiptDTO discountReceipt = result.getData().getOrder().getOrderReceipts().get(1);
        assertEquals("2", discountReceipt.getSequence());
        assertEquals("50.00", discountReceipt.getPaymentAmount());
        assertEquals("CUPOM_PROMOCIONAL", discountReceipt.getPaymentType());
        assertEquals("26", discountReceipt.getId());
        assertEquals("1", discountReceipt.getInstallments());
        assertNotNull(discountReceipt.getOthersDetails());
        assertEquals("23", discountReceipt.getOthersDetails().getVerificationCode());
    }

    @Test
    void generateRequest_WithTrocaFoneDiscount_ShouldAddPromotionalCoupon() throws Exception {
        PaymentSingleDTO paymentWithDiscount = PaymentSingleDTO.builder()
                .salesOrderId("12345")
                .value("100.00")
                .cardData(CardDataDTO.builder()
                        .build())
                .discounts(Arrays.asList(
                        PaymentDiscountDTO.builder()
                                .id("TROCA_FONE")
                                .value("75.00")
                                .build()
                ))
                .build();

        DataSingleDTO dataSingleWithDiscount = new DataSingleDTO();
        ReflectionTestUtils.setField(dataSingleWithDiscount, "customer", CustomerSingleDTO.builder()
                .name("João Silva")
                .cpf("12345678900")
                .build());
        ReflectionTestUtils.setField(dataSingleWithDiscount, "payment", paymentWithDiscount);
        ReflectionTestUtils.setField(dataSingleWithDiscount, "fraudAnalysisData", FraudAnalysisDataDTO.builder()
                .complementaryData(ComplementaryDataDTO.builder()
                        .products(Arrays.asList(
                                ProductDTO.builder()
                                        .sku("SKU001")
                                        .amount("2")
                                        .value("50.00")
                                        .discountValue("0.00")
                                        .totalDiscountValue("0.00")
                                        .serialNumber("SN001")
                                        .build()
                        ))
                        .build())
                .build());

        InformationPaymentDTO infoWithDiscount = InformationPaymentDTO.builder()
                .uuid(uuid)
                .store("STORE001")
                .channel("credit")
                .payments(Arrays.asList(
                        PaymentDTO.builder()
                                .journey("{\"customer\":{\"name\":\"João Silva\",\"cpf\":\"12345678900\"},\"payment\":{\"salesOrderId\":\"12345\",\"value\":\"100.00\",\"cardData\":{\"sellerId\":\"seller123\"},\"discounts\":[{\"id\":\"TROCA_FONE\",\"value\":\"75.00\"}]},\"fraudAnalysisData\":{\"complementaryData\":{\"products\":[{\"sku\":\"SKU001\",\"amount\":\"2\",\"value\":\"50.00\",\"discountValue\":\"0.00\",\"totalDiscountValue\":\"0.00\",\"serialNumber\":\"SN001\"}]}}}")
                                .build()
                ))
                .build();

        CreditCardCallbackRequest creditCallback = CreditCardCallbackRequest.builder()
                .orderDate("15/10/2023")
                .value(BigDecimal.valueOf(100.00))
                .card("1234-5678")
                .flag("VISA")
                .build();

        String callbackJson = "{\"orderDate\":\"15/10/2023\",\"value\":100.00,\"card\":\"1234-5678\",\"flag\":\"VISA\"}";

        when(validateService.isValid(callbackJson, CreditCardCallbackRequest.class)).thenReturn(true);
        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenReturn(callbackJson);
        when(mapper.readValue(callbackJson, CreditCardCallbackRequest.class)).thenReturn(creditCallback);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleWithDiscount);

        SapPaymentsRequest result = service.generateRequest(mock(CallbackDTO.class), infoWithDiscount);

        assertNotNull(result);
        assertNotNull(result.getData().getOrder().getOrderReceipts());
        assertEquals(2, result.getData().getOrder().getOrderReceipts().size()); // original + discount
        
        // Verificar o cupom promocional
        OrderReceiptDTO discountReceipt = result.getData().getOrder().getOrderReceipts().get(1);
        assertEquals("2", discountReceipt.getSequence());
        assertEquals("75.00", discountReceipt.getPaymentAmount());
        assertEquals("CUPOM_PROMOCIONAL", discountReceipt.getPaymentType());
        assertEquals("26", discountReceipt.getId());
        assertEquals("1", discountReceipt.getInstallments());
        assertNotNull(discountReceipt.getOthersDetails());
        assertEquals("22", discountReceipt.getOthersDetails().getVerificationCode());
    }

    @Test
    void generateRequest_WithJsonProcessingException_ShouldThrowBusinessException() throws Exception {
        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("JSON error") {});

        assertThrows(BusinessException.class, () -> service.generateRequest(mock(CallbackDTO.class), info));
    }

    @Test
    void generateRequest_WithInvalidDataJson_ShouldThrowBusinessException() throws Exception {
        String callbackJson = "{\"orderDate\":\"15/10/2023\",\"value\":100.00,\"card\":\"1234-5678\",\"flag\":\"VISA\"}";

        when(mapper.writeValueAsString(any(CallbackDTO.class))).thenReturn(callbackJson);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Invalid JSON") {});

        assertThrows(BusinessException.class, () -> service.generateRequest(mock(CallbackDTO.class), info));
    }
}
