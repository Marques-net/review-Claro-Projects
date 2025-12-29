package com.omp.hub.callback.application.usecase.callback.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.service.SapIntegrationService;
import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.OrderDTO;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.OrdersDTO;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.SalesDTO;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.ComplementaryDataDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.DataSingleDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.FraudAnalysisDataDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.PaymentSingleDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.ProductDTO;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import okhttp3.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.omp.hub.callback.domain.enums.PaymentTypeEnum;

@ExtendWith(MockitoExtension.class)
class TefWebCallbackUseCaseImplTest {

    @Mock
    private InformationPaymentPort port;

    @Mock
    private SapIntegrationService sapIntegrationService;

    @Mock
    private ApigeeHeaderService apigeeHeaderService;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private TefWebCallbackUseCaseImpl useCase;

    private TefWebCallbackRequest request;
    private InformationPaymentDTO informationPaymentDTO;
    private DataSingleDTO dataSingleDTO;
    private Headers.Builder mockHeadersBuilder;
    private UUID mockUuid;

    @BeforeEach
    void setUp() {
        request = new TefWebCallbackRequest();

        OrdersDTO orderItem = new OrdersDTO();
        orderItem.setOrderNumber("order-123");

        OrderDTO order = new OrderDTO();
        order.setOrders(Collections.singletonList(orderItem));

        SalesDTO sale = new SalesDTO();
        sale.setOrder(order);

        request.setSales(Collections.singletonList(sale));
        request.setSales(List.of(SalesDTO.builder()
                        .order(OrderDTO.builder()
                                .ompTransactionId("SV00c076fa8a-ca6f-474c-ab09-39ac1427bb2bH1")
                                .build())
                .build()));

        mockUuid = UUID.randomUUID();
        informationPaymentDTO = new InformationPaymentDTO();
        informationPaymentDTO.setUuid(mockUuid);

        PaymentDTO paymentInfo = new PaymentDTO();
        paymentInfo.setJourney("{\"payment\":{\"salesOrderId\":\"sales-123\"}}");
        informationPaymentDTO.setPayments(Collections.singletonList(paymentInfo));

        dataSingleDTO = new DataSingleDTO();
        PaymentSingleDTO paymentSingleDTO = new PaymentSingleDTO();
        paymentSingleDTO.setSalesOrderId("sales-123");
        dataSingleDTO.setPayment(paymentSingleDTO);

        mockHeadersBuilder = new Headers.Builder();

        lenient().when(sapIntegrationService.extractBaseTransactionOrderId(anyString()))
                .thenAnswer(invocation -> {
                    String id = invocation.getArgument(0);
                    if (id != null && id.length() > 4) {
                        return id.substring(2, id.length() - 2).replaceFirst("^0+", "");
                    }
                    return id;
                });
    }

    @Test
    void sendCallback_WithValidOrderNumberAndSalesOrderId_ShouldProcessAllSteps() throws Exception {
        // Given
        ProductDTO product = new ProductDTO();
        product.setCode("T30");

        ComplementaryDataDTO complementaryData = new ComplementaryDataDTO();
        complementaryData.setProducts(Collections.singletonList(product));

        FraudAnalysisDataDTO fraudAnalysisData = new FraudAnalysisDataDTO();
        fraudAnalysisData.setComplementaryData(complementaryData);

        dataSingleDTO.setFraudAnalysisData(fraudAnalysisData);

        when(port.sendFindByIdentifier(anyString())).thenReturn(informationPaymentDTO);
        when(mapper.readValue(nullable(String.class), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(mapper).readValue(anyString(), eq(DataSingleDTO.class));
        verify(apigeeHeaderService).generateHeaderApigee(mockUuid);
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void sendCallback_WithNullSales_ShouldOnlySendTransactionNotification() throws Exception {
        // Given
        request.setSales(null);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(sapIntegrationService).sendChannelNotification(any(UUID.class), eq(request), eq(mockHeadersBuilder), any());
    }

    @Test
    void sendCallback_WithEmptySales_ShouldOnlySendTransactionNotification() throws Exception {
        // Given
        request.setSales(Collections.emptyList());
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(sapIntegrationService).sendChannelNotification(any(UUID.class), eq(request), eq(mockHeadersBuilder), any());
    }

    @Test
    void sendCallback_WithNullOrderNumber_ShouldOnlySendTransactionNotification() throws Exception {
        // Given
        when(port.sendFindByIdentifier(anyString())).thenReturn(informationPaymentDTO);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(sapIntegrationService).sendChannelNotification(any(UUID.class), eq(request), eq(mockHeadersBuilder), any());
    }

    @Test
    void sendCallback_WithNullSalesOrderId_ShouldCallSapRedemptionsAndPaymentsWithIdentifier() throws Exception {
        // Given
        dataSingleDTO.getPayment().setSalesOrderId(null);
        when(port.sendFindByIdentifier(anyString())).thenReturn(informationPaymentDTO);
        when(mapper.readValue(nullable(String.class), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);
        when(sapIntegrationService.hasSalesOrderId(any(DataSingleDTO.class), anyString())).thenReturn(true);

        // When
        useCase.sendCallback(request);

        // Then
        verify(sapIntegrationService).sendToSapRedemptionsAndPayments(any(UUID.class), eq(request), eq(informationPaymentDTO), eq(mockHeadersBuilder), any(DataSingleDTO.class));
        verify(sapIntegrationService).sendChannelNotification(any(UUID.class), eq(request), eq(mockHeadersBuilder), any());
        verify(port, times(2)).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void sendCallback_WithDesistenciaSuperTrocaProduct_ShouldCallBillingPayments() throws Exception {
        // Given
        ProductDTO product1 = new ProductDTO();
        product1.setCode("OTHER");

        ProductDTO product2 = new ProductDTO();
        product2.setCode("T30");

        ComplementaryDataDTO complementaryData = new ComplementaryDataDTO();
        complementaryData.setProducts(List.of(product1, product2));

        FraudAnalysisDataDTO fraudAnalysisData = new FraudAnalysisDataDTO();
        fraudAnalysisData.setComplementaryData(complementaryData);

        dataSingleDTO.setFraudAnalysisData(fraudAnalysisData);

        when(port.sendFindByIdentifier(anyString())).thenReturn(informationPaymentDTO);
        when(mapper.readValue(nullable(String.class), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);
        when(sapIntegrationService.hasBillingProducts(any(DataSingleDTO.class))).thenReturn(true);

        // When
        useCase.sendCallback(request);

        // Then
        verify(sapIntegrationService).sendToSapBillingPayments(any(UUID.class), eq(request), eq(informationPaymentDTO), eq(mockHeadersBuilder), any(DataSingleDTO.class));
        verify(sapIntegrationService).sendChannelNotification(any(UUID.class), eq(request), eq(mockHeadersBuilder), any());
        verify(port, times(2)).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void sendCallback_WithOtherProducts_ShouldNotCallBillingPayments() throws Exception {
        // Given
        dataSingleDTO.getPayment().setSalesOrderId(null);

        ProductDTO product = new ProductDTO();
        product.setName("Other Product");

        ComplementaryDataDTO complementaryData = new ComplementaryDataDTO();
        complementaryData.setProducts(Collections.singletonList(product));

        FraudAnalysisDataDTO fraudAnalysisData = new FraudAnalysisDataDTO();
        fraudAnalysisData.setComplementaryData(complementaryData);

        dataSingleDTO.setFraudAnalysisData(fraudAnalysisData);

        when(port.sendFindByIdentifier(anyString())).thenReturn(informationPaymentDTO);
        when(mapper.readValue(nullable(String.class), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);

        // When
        useCase.sendCallback(request);

        // Then
        verify(sapIntegrationService).sendChannelNotification(any(UUID.class), eq(request), eq(mockHeadersBuilder), any());
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void sendCallback_WhenDeserializationFails_ShouldThrowBusinessException() throws Exception {
        // Given
        when(port.sendFindByIdentifier(anyString())).thenReturn(informationPaymentDTO);
        when(mapper.readValue(nullable(String.class), eq(DataSingleDTO.class)))
                .thenThrow(new JsonProcessingException("Deserialization error") {});

        // When & Then
        assertThrows(BusinessException.class, () -> useCase.sendCallback(request));
    }

    @Test
    void sendCallback_WhenSapIntegrationFails_ShouldUpdateStatusAndRethrow() throws Exception {
        // Given
        BusinessException businessException = new BusinessException("SAP error", "SAP_ERR");

        ProductDTO product = new ProductDTO();
        product.setCode("T30");

        ComplementaryDataDTO complementaryData = new ComplementaryDataDTO();
        complementaryData.setProducts(Collections.singletonList(product));

        FraudAnalysisDataDTO fraudAnalysisData = new FraudAnalysisDataDTO();
        fraudAnalysisData.setComplementaryData(complementaryData);

        dataSingleDTO.setFraudAnalysisData(fraudAnalysisData);

        when(port.sendFindByIdentifier(anyString())).thenReturn(informationPaymentDTO);
        when(mapper.readValue(nullable(String.class), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);
        when(sapIntegrationService.hasSalesOrderId(any(DataSingleDTO.class), anyString())).thenReturn(true);

        doThrow(businessException).when(sapIntegrationService).sendToSapRedemptionsAndPayments(any(UUID.class), eq(request), eq(informationPaymentDTO), eq(mockHeadersBuilder), any(DataSingleDTO.class));

        // When & Then
        assertThrows(BusinessException.class, () -> useCase.sendCallback(request));
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void sendCallback_WhenGenericExceptionOccurs_ShouldWrapInBusinessException() throws Exception {
        // Given
        RuntimeException genericException = new RuntimeException("Unexpected error");

        when(port.sendFindByIdentifier(anyString())).thenReturn(informationPaymentDTO);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenThrow(genericException);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> useCase.sendCallback(request));
        assertNotNull(exception);
    }

    @Test
    void sendCallback_WithMultiplePayment_ShouldProcessMixedPaymentFlow() throws Exception {
        // Given
        request.setMultiplePayment(true);
        request.setMixedPaymentTypes(List.of("TEFWEB", "CASH"));
        
        informationPaymentDTO.setMultiplePayment(true);

        when(port.sendFindByIdentifier(anyString())).thenReturn(informationPaymentDTO);
        when(mapper.readValue(nullable(String.class), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);
        when(sapIntegrationService.shouldSendToSap(informationPaymentDTO, PaymentTypeEnum.TEFWEB)).thenReturn(true);
        when(sapIntegrationService.hasSalesOrderId(any(DataSingleDTO.class), anyString())).thenReturn(true);

        // When
        useCase.sendCallback(request);

        // Then
        verify(sapIntegrationService).extractBaseTransactionOrderId(anyString());
        verify(sapIntegrationService).shouldSendToSap(any(InformationPaymentDTO.class), eq(PaymentTypeEnum.TEFWEB));
        verify(sapIntegrationService).sendToSapRedemptionsAndPayments(any(UUID.class), eq(request), any(InformationPaymentDTO.class), eq(mockHeadersBuilder), any(DataSingleDTO.class));
//        verify(sapIntegrationService).sendChannelNotification(mockUuid, request, mockHeadersBuilder, any());
        verify(port, times(2)).sendUpdate(any(InformationPaymentDTO.class));
    }

    @Test
    void sendCallback_WithMultiplePayment_ShouldNotSendToSapWhenNotApproved() throws Exception {
        // Given
        request.setMultiplePayment(true);
        request.setMixedPaymentTypes(List.of("TEFWEB", "CASH"));
        
        informationPaymentDTO.setMultiplePayment(true);

        when(port.sendFindByIdentifier(anyString())).thenReturn(informationPaymentDTO);
        when(mapper.readValue(nullable(String.class), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);
        when(mapper.writeValueAsString(any())).thenReturn("{}");
        when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(mockHeadersBuilder);
        when(sapIntegrationService.shouldSendToSap(informationPaymentDTO, PaymentTypeEnum.TEFWEB)).thenReturn(false);

        // When
        useCase.sendCallback(request);

        // Then
        verify(sapIntegrationService).extractBaseTransactionOrderId(anyString());
        verify(sapIntegrationService).shouldSendToSap(informationPaymentDTO, PaymentTypeEnum.TEFWEB);
        verify(sapIntegrationService, never()).sendToSapRedemptionsAndPayments(any(), any(), any(), any(), any());
//        verify(sapIntegrationService).sendChannelNotification(mockUuid, request, mockHeadersBuilder, any());
    }
}
