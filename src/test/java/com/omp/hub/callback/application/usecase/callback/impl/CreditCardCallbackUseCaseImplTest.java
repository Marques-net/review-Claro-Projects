package com.omp.hub.callback.application.usecase.callback.impl;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.service.RetryService;
import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.DataSingleDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.PaymentSingleDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.SapRedemptionsRequest;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.ports.client.SapPaymentsPort;
import com.omp.hub.callback.domain.ports.client.SapRedemptionsPort;
import com.omp.hub.callback.domain.ports.client.TransationsNotificationsPort;
import com.omp.hub.callback.domain.service.generate.GenerateCallbackCardService;
import com.omp.hub.callback.domain.service.generate.GenerateSapPaymentsRequestService;
import com.omp.hub.callback.domain.service.generate.GenerateSapRedemptionsRequestService;

import okhttp3.Headers;

@ExtendWith(MockitoExtension.class)
class CreditCardCallbackUseCaseImplTest {
    @Mock
    private InformationPaymentPort port;

    @Mock
    private ApigeeHeaderService apigeeHeaderService;

    @Mock
    private SapRedemptionsPort redemptionsPort;

    @Mock
    private GenerateSapRedemptionsRequestService generateRedemptionsService;

    @Mock
    private SapPaymentsPort paymentsPort;

    @Mock
    private GenerateSapPaymentsRequestService generatePaymentsService;

    @Mock
    private GenerateCallbackCardService service;

    @Mock
    private TransationsNotificationsPort transactionsPort;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private RetryService retryService;

    @InjectMocks
    private CreditCardCallbackUseCaseImpl useCase;

    private CreditCardCallbackRequest request;
    private String identifier;
    private InformationPaymentDTO info;
    private DataSingleDTO dataSingleDTO;
    private Headers.Builder headersBuilder;

    @BeforeEach
    void setUp() {
        identifier = "ORDER123";
        request = CreditCardCallbackRequest.builder()
                .orderId("ORDER123")
                .orderDate("15/10/2023")
                .value(java.math.BigDecimal.valueOf(100.00))
                .card("1234-5678")
                .flag("VISA")
                .build();

        PaymentSingleDTO payment = PaymentSingleDTO.builder()
                .salesOrderId("SO123")
                .value("100.00")
                .build();

        dataSingleDTO = new DataSingleDTO();
        dataSingleDTO.setPayment(payment);

        info = InformationPaymentDTO.builder()
                .identifier(identifier)
                .store("STORE001")
                .uuid(UUID.randomUUID())
                .payments(Arrays.asList(
                        PaymentDTO.builder()
                                .journey("{\"payment\":{\"salesOrderId\":\"SO123\",\"value\":\"100.00\"}}")
                                .build()
                ))
                .build();

        headersBuilder = new Headers.Builder();

        // Configure retryService to execute the lambda immediately and propagate exceptions
        lenient().doAnswer(invocation -> {
            try {
                Runnable runnable = invocation.getArgument(2);
                runnable.run();
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).when(retryService).executeWithRetrySyncVoid(any(), anyString(), any(), any());
    }

    @Test
    void sendCallback_WithValidOrderIdAndSalesOrderId_ShouldProcessCompleteFlow() throws Exception {
        // Given
        when(port.sendFindByIdentifier(eq("ORDER123"))).thenReturn(info);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);
        when(apigeeHeaderService.generateHeaderApigee(any())).thenReturn(headersBuilder);
        when(generateRedemptionsService.generateRequest(info)).thenReturn(mock(SapRedemptionsRequest.class));
        when(generatePaymentsService.generateRequest(request, info)).thenReturn(mock(SapPaymentsRequest.class));
        when(service.generateRequest(request)).thenReturn(mock(OmphubTransactionNotificationRequest.class));

        // When
        useCase.sendCallback(request);

        // Then
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
        verify(port).sendFindByIdentifier(eq("ORDER123"));
        verify(mapper).readValue(anyString(), eq(DataSingleDTO.class));
        verify(apigeeHeaderService).generateHeaderApigee(any());
        verify(redemptionsPort).send(any(), any(SapRedemptionsRequest.class), eq(headersBuilder));
        verify(paymentsPort).send(any(), any(SapPaymentsRequest.class), eq(headersBuilder));
        verify(transactionsPort).send(any(), any(OmphubTransactionNotificationRequest.class), eq(headersBuilder));
    }

    @Test
    void sendCallback_WithNullOrderId_ShouldSkipPaymentProcessing() throws Exception {
        // Given
        CreditCardCallbackRequest requestWithNullOrderId = CreditCardCallbackRequest.builder()
                .orderId(null)
                .build();

        when(apigeeHeaderService.generateHeaderApigee(any())).thenReturn(headersBuilder);
        when(service.generateRequest(requestWithNullOrderId)).thenReturn(mock(OmphubTransactionNotificationRequest.class));

        // When
        useCase.sendCallback(requestWithNullOrderId);

        // Then
        verify(port, never()).sendUpdate(any(InformationPaymentDTO.class));
        verify(port, never()).sendFindByIdentifier(anyString());
        verify(redemptionsPort, never()).send(any(), any(), any());
        verify(paymentsPort, never()).send(any(), any(), any());
        verify(transactionsPort).send(any(), any(OmphubTransactionNotificationRequest.class), eq(headersBuilder));
    }

    @Test
    void sendCallback_WithEmptyOrderId_ShouldSkipPaymentProcessing() throws Exception {
        // Given
        CreditCardCallbackRequest requestWithEmptyOrderId = CreditCardCallbackRequest.builder()
                .orderId("")
                .build();

        when(apigeeHeaderService.generateHeaderApigee(any())).thenReturn(headersBuilder);
        when(service.generateRequest(requestWithEmptyOrderId)).thenReturn(mock(OmphubTransactionNotificationRequest.class));

        // When
        useCase.sendCallback(requestWithEmptyOrderId);

        // Then
        verify(port, never()).sendUpdate(any(InformationPaymentDTO.class));
        verify(port, never()).sendFindByIdentifier(anyString());
        verify(redemptionsPort, never()).send(any(), any(), any());
        verify(paymentsPort, never()).send(any(), any(), any());
        verify(transactionsPort).send(any(), any(OmphubTransactionNotificationRequest.class), eq(headersBuilder));
    }

    @Test
    void sendCallback_WithDeserializationException_ShouldUpdateStatusToErrorAndThrowBusinessException() throws Exception {
        // Given
        when(port.sendFindByIdentifier(eq("ORDER123"))).thenReturn(info);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenThrow(new RuntimeException("Deserialization error"));

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(BusinessException.class, () -> useCase.sendCallback(request));

        verify(port, times(2)).sendUpdate(any(InformationPaymentDTO.class));
        verify(port).sendFindByIdentifier(eq("ORDER123"));
        verify(redemptionsPort, never()).send(any(), any(), any());
        verify(paymentsPort, never()).send(any(), any(), any());
        verify(transactionsPort, never()).send(any(), any(), any());
    }

    @Test
    void sendCallback_WithNullDto_ShouldSkipSapCalls() throws Exception {
        // Given
        when(port.sendFindByIdentifier(eq("ORDER123"))).thenReturn(info);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(null);
        when(apigeeHeaderService.generateHeaderApigee(any())).thenReturn(headersBuilder);
        when(service.generateRequest(request)).thenReturn(mock(OmphubTransactionNotificationRequest.class));

        // When
        useCase.sendCallback(request);

        // Then
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
        verify(redemptionsPort, never()).send(any(), any(), any());
        verify(paymentsPort, never()).send(any(), any(), any());
        verify(transactionsPort).send(any(), any(OmphubTransactionNotificationRequest.class), eq(headersBuilder));
    }

    @Test
    void sendCallback_WithNullSalesOrderId_ShouldSkipSapCalls() throws Exception {
        // Given
        PaymentSingleDTO paymentWithoutSalesOrderId = PaymentSingleDTO.builder()
                .salesOrderId(null)
                .value("100.00")
                .build();

        DataSingleDTO dataSingleWithoutSalesOrderId = new DataSingleDTO();
        dataSingleWithoutSalesOrderId.setPayment(paymentWithoutSalesOrderId);

        when(port.sendFindByIdentifier(eq("ORDER123"))).thenReturn(info);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleWithoutSalesOrderId);
        when(apigeeHeaderService.generateHeaderApigee(any())).thenReturn(headersBuilder);
        when(service.generateRequest(request)).thenReturn(mock(OmphubTransactionNotificationRequest.class));

        // When
        useCase.sendCallback(request);

        // Then
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
        verify(redemptionsPort, never()).send(any(), any(), any());
        verify(paymentsPort, never()).send(any(), any(), any());
        verify(transactionsPort).send(any(), any(OmphubTransactionNotificationRequest.class), eq(headersBuilder));
    }

    @Test
    void sendCallback_WithEmptySalesOrderId_ShouldSkipSapCalls() throws Exception {
        // Given
        PaymentSingleDTO paymentWithEmptySalesOrderId = PaymentSingleDTO.builder()
                .salesOrderId("")
                .value("100.00")
                .build();

        DataSingleDTO dataSingleWithEmptySalesOrderId = new DataSingleDTO();
        dataSingleWithEmptySalesOrderId.setPayment(paymentWithEmptySalesOrderId);

        when(port.sendFindByIdentifier(eq("ORDER123"))).thenReturn(info);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleWithEmptySalesOrderId);
        when(apigeeHeaderService.generateHeaderApigee(any())).thenReturn(headersBuilder);
        when(service.generateRequest(request)).thenReturn(mock(OmphubTransactionNotificationRequest.class));

        // When
        useCase.sendCallback(request);

        // Then
        verify(port).sendUpdate(any(InformationPaymentDTO.class));
        verify(redemptionsPort, never()).send(any(), any(), any());
        verify(paymentsPort, never()).send(any(), any(), any());
        verify(transactionsPort).send(any(), any(OmphubTransactionNotificationRequest.class), eq(headersBuilder));
    }

    @Test
    void sendCallback_WithRedemptionsPortException_ShouldUpdateStatusToErrorAndThrowBusinessException() throws Exception {
        // Given
        BusinessException redemptionsException = new BusinessException(new RuntimeException("Redemptions port error"));

        // Configure retryService to throw exception when redemptions call fails
        lenient().doAnswer(invocation -> {
            String operationName = invocation.getArgument(1);
            if ("SAP Redemptions".equals(operationName)) {
                throw redemptionsException;
            }
            Runnable runnable = invocation.getArgument(2);
            runnable.run();
            return null;
        }).when(retryService).executeWithRetrySyncVoid(any(), anyString(), any(), any());

        when(port.sendFindByIdentifier(eq("ORDER123"))).thenReturn(info);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);
        when(apigeeHeaderService.generateHeaderApigee(any())).thenReturn(headersBuilder);
        lenient().when(generateRedemptionsService.generateRequest(info)).thenReturn(mock(SapRedemptionsRequest.class));

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(BusinessException.class, () -> useCase.sendCallback(request));

        verify(port, times(2)).sendUpdate(any(InformationPaymentDTO.class));
        verify(paymentsPort, never()).send(any(), any(), any());
        verify(transactionsPort, never()).send(any(), any(), any());
    }

    @Test
    void sendCallback_WithPaymentsPortException_ShouldUpdateStatusToErrorAndThrowBusinessException() throws Exception {
        // Given
        BusinessException paymentsException = new BusinessException(new RuntimeException("Payments port error"));

        // Configure retryService to throw exception when payments call fails
        lenient().doAnswer(invocation -> {
            String operationName = invocation.getArgument(1);
            if ("SAP Payments".equals(operationName)) {
                throw paymentsException;
            }
            Runnable runnable = invocation.getArgument(2);
            runnable.run();
            return null;
        }).when(retryService).executeWithRetrySyncVoid(any(), anyString(), any(), any());

        when(port.sendFindByIdentifier(eq("ORDER123"))).thenReturn(info);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingleDTO);
        when(apigeeHeaderService.generateHeaderApigee(any())).thenReturn(headersBuilder);
        lenient().when(generateRedemptionsService.generateRequest(info)).thenReturn(mock(SapRedemptionsRequest.class));
        lenient().when(generatePaymentsService.generateRequest(request, info)).thenReturn(mock(SapPaymentsRequest.class));

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(BusinessException.class, () -> useCase.sendCallback(request));

        verify(port, times(2)).sendUpdate(any(InformationPaymentDTO.class));
        verify(transactionsPort, never()).send(any(), any(), any());
    }
}
