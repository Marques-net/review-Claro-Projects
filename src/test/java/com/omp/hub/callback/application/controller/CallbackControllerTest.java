package com.omp.hub.callback.application.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.service.CallbackTypeDetectorService;
import com.omp.hub.callback.application.validator.CallbackValidationException;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.AcquiratorDTO;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.AntifraudDTO;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;

import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;
import com.omp.hub.callback.domain.model.dto.response.CallbackResponse;
import com.omp.hub.callback.domain.service.impl.callback.CallbackService;

@ExtendWith(MockitoExtension.class)
class CallbackControllerTest {

    @InjectMocks
    private CallbackController callbackController;

    @Mock
    private CallbackService callbackService;
    
    @Mock
    private ObjectMapper mapper;

    @Mock
    private CallbackTypeDetectorService callbackTypeDetector;

    private CallbackRequest<PixCallbackRequest> pixCallbackRequest;
    private CallbackRequest<CreditCardCallbackRequest> creditCardCallbackRequest;
    private CallbackRequest<TefWebCallbackRequest> tefWebCallbackRequest;
    private CallbackRequest<TransactionsRequest> transactionsCallbackRequest;
    private CallbackRequest<Object> invalidCallbackRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(callbackController, "asyncProcessingEnabled", true);

        PixCallbackRequest pixData = PixCallbackRequest.builder()
                .service("PIX")
                .paymentType("PIX")
                .paymentDate("2024-01-01T10:00:00")
                .value("100.50")
                .endToEndId("E1234567890")
                .txId("test-tx-123")
                .build();
        pixCallbackRequest = CallbackRequest.<PixCallbackRequest>builder()
                .data(pixData)
                .build();

        AcquiratorDTO acquirator = AcquiratorDTO.builder()
                .nsu("NSU456")
                .authorizationCode("AUTH123")
                .acquiratorCode("001")
                .transactionId("ACQ-TX-789")
                .responseCode("00")
                .responseDescription("Approved")
                .build();
                
        AntifraudDTO antifraud = AntifraudDTO.builder()
                .statusCode("APPROVED")
                .decision("ACCEPT")
                .build();

        CreditCardCallbackRequest creditCardData = CreditCardCallbackRequest.builder()
                .sucess(true)
                .service("CREDIT_CARD")
                .statusCode("200")
                .statusMessage("Approved")
                .transactionId("cc-tx-456")
                .flag("VISA")
                .card("**** **** **** 1234")
                .value(new BigDecimal("150.75"))
                .numberInstallments(3)
                .orderId("ORDER-001")
                .orderDate("2024-01-01")
                .acquirator(acquirator)
                .antifraud(antifraud)
                .build();
        creditCardCallbackRequest = CallbackRequest.<CreditCardCallbackRequest>builder()
                .data(creditCardData)
                .build();

        TefWebCallbackRequest tefWebData = TefWebCallbackRequest.builder()
                .service("TEF_WEB")
                .paymentType("DEBIT")
                .sales(new ArrayList<>()) 
                .build();
        tefWebCallbackRequest = CallbackRequest.<TefWebCallbackRequest>builder()
                .data(tefWebData)
                .build();

        EventDTO event = EventDTO.builder()
                .type("PAYMENT_COMPLETED")
                .build();
                
        TransactionsRequest transactionsData = TransactionsRequest.builder()
                .ompTransactionId("OMP-TX-001")
                .callbackTarget("SYSTEM_A")
                .event(event)
                .build();
        transactionsCallbackRequest = CallbackRequest.<TransactionsRequest>builder()
                .data(transactionsData)
                .build();

        invalidCallbackRequest = CallbackRequest.<Object>builder()
                .data(Map.of("invalid", "data"))
                .build();
    }

    @Test
    void getPaymentMethods_WithValidPixCallback_ShouldReturnSuccess() throws Exception {
        String jsonData = "{\"service\":\"PIX\",\"txId\":\"test-tx-123\"}";
        when(mapper.writeValueAsString(any())).thenReturn(jsonData);
        when(callbackTypeDetector.isValidCallbackType(anyString())).thenReturn(true);
        doNothing().when(callbackTypeDetector).detectTypeAndValidate(anyString());
        doNothing().when(callbackService).processCallbackAsync(any(CallbackRequest.class));

        ResponseEntity<?> response = callbackController.processCallback(pixCallbackRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CallbackResponse body = (CallbackResponse) response.getBody();
        assertNotNull(body);
        assertEquals("1;2019-09-11", body.getApiVersion());
        assertNotNull(body.getTransactionId());
        assertNotNull(body.getData());
        assertEquals("SUCCESS", body.getData().getResult());
        verify(callbackService).processCallbackAsync(any(CallbackRequest.class));
    }

    @Test
    void getPaymentMethods_WithValidCreditCardCallback_ShouldReturnSuccess() throws Exception {
        String jsonData = "{\"service\":\"CREDIT_CARD\",\"transactionId\":\"cc-tx-456\"}";
        when(mapper.writeValueAsString(any())).thenReturn(jsonData);
        when(callbackTypeDetector.isValidCallbackType(anyString())).thenReturn(true);
        doNothing().when(callbackTypeDetector).detectTypeAndValidate(anyString());
        doNothing().when(callbackService).processCallbackAsync(any(CallbackRequest.class));

        ResponseEntity<?> response = callbackController.processCallback(creditCardCallbackRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CallbackResponse body = (CallbackResponse) response.getBody();
        assertNotNull(body);
        assertEquals("1;2019-09-11", body.getApiVersion());
        assertNotNull(body.getTransactionId());
        assertNotNull(body.getData());
        assertEquals("SUCCESS", body.getData().getResult());
        verify(callbackService).processCallbackAsync(any(CallbackRequest.class));
    }

    @Test
    void getPaymentMethods_WithValidTefWebCallback_ShouldReturnSuccess() throws Exception {
        String jsonData = "{\"service\":\"TEF_WEB\",\"transactionId\":\"tef-tx-789\"}";
        when(mapper.writeValueAsString(any())).thenReturn(jsonData);
        when(callbackTypeDetector.isValidCallbackType(anyString())).thenReturn(true);
        doNothing().when(callbackTypeDetector).detectTypeAndValidate(anyString());
        doNothing().when(callbackService).processCallbackAsync(any(CallbackRequest.class));

        ResponseEntity<?> response = callbackController.processCallback(tefWebCallbackRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CallbackResponse body = (CallbackResponse) response.getBody();
        assertNotNull(body);
        assertEquals("1;2019-09-11", body.getApiVersion());
        assertNotNull(body.getTransactionId());
        assertNotNull(body.getData());
        assertEquals("SUCCESS", body.getData().getResult());
        verify(callbackService).processCallbackAsync(any(CallbackRequest.class));
    }

    @Test
    void getPaymentMethods_WithValidTransactionsCallback_ShouldReturnSuccess() throws Exception {
        String jsonData = "{\"service\":\"TRANSACTIONS\",\"transactionId\":\"trans-tx-101\"}";
        when(mapper.writeValueAsString(any())).thenReturn(jsonData);
        when(callbackTypeDetector.isValidCallbackType(anyString())).thenReturn(true);
        doNothing().when(callbackTypeDetector).detectTypeAndValidate(anyString());
        doNothing().when(callbackService).processCallbackAsync(any(CallbackRequest.class));

        ResponseEntity<?> response = callbackController.processCallback(transactionsCallbackRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CallbackResponse body = (CallbackResponse) response.getBody();
        assertNotNull(body);
        assertEquals("1;2019-09-11", body.getApiVersion());
        assertNotNull(body.getTransactionId());
        assertNotNull(body.getData());
        assertEquals("SUCCESS", body.getData().getResult());
        verify(callbackService).processCallbackAsync(any(CallbackRequest.class));
    }

    @Test
    void getPaymentMethods_WithInvalidCallback_ShouldThrowBusinessException() throws Exception {
        String jsonData = "{\"invalid\":\"data\"}";
        when(mapper.writeValueAsString(any())).thenReturn(jsonData);
        when(callbackTypeDetector.isValidCallbackType(anyString())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            callbackController.processCallback(invalidCallbackRequest);
        });

        assertNotNull(exception);
        assertEquals("Payload inválido: não corresponde a nenhum tipo de callback suportado (PIX, CreditCard, TefWeb ou Transactions)", exception.getError().getMessage());
        assertEquals("INVALID_CALLBACK_TYPE", exception.getError().getErrorCode());
    }

    @Test
    void getPaymentMethods_WithJsonProcessingException_ShouldThrowBusinessException() throws Exception {
        when(mapper.writeValueAsString(any())).thenThrow(com.fasterxml.jackson.core.JsonProcessingException.class);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            callbackController.processCallback(pixCallbackRequest);
        });

        assertNotNull(exception);
        assertEquals("Erro convert Json", exception.getError().getMessage());
    }

    @Test
    void getPaymentMethods_WithAsyncProcessingDisabled_ShouldProcessSynchronously() throws Exception {
        ReflectionTestUtils.setField(callbackController, "asyncProcessingEnabled", false);
        
        String jsonData = "{\"service\":\"PIX\",\"txId\":\"test-tx-123\"}";
        when(mapper.writeValueAsString(any())).thenReturn(jsonData);
        when(callbackTypeDetector.isValidCallbackType(anyString())).thenReturn(true);
        doNothing().when(callbackTypeDetector).detectTypeAndValidate(anyString());
        doNothing().when(callbackService).processCallback(anyString());

        ResponseEntity<?> response = callbackController.processCallback(pixCallbackRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(callbackService).processCallback(jsonData);
        verify(callbackService, never()).processCallbackAsync(any(CallbackRequest.class));
        
        ReflectionTestUtils.setField(callbackController, "asyncProcessingEnabled", true);
    }

    @Test
    void getPaymentMethods_WithCallbackValidationException_ShouldThrowBusinessException() throws Exception {
        String jsonData = "{\"service\":\"PIX\",\"txId\":\"test-tx-123\"}";
        when(mapper.writeValueAsString(any())).thenReturn(jsonData);
        when(callbackTypeDetector.isValidCallbackType(anyString())).thenReturn(true);
        doThrow(new CallbackValidationException("Validation failed", "field1: must not be null"))
                .when(callbackTypeDetector).detectTypeAndValidate(anyString());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            callbackController.processCallback(pixCallbackRequest);
        });

        assertNotNull(exception);
        assertEquals("Validation failed", exception.getError().getMessage());
        assertEquals("VALIDATION_ERROR", exception.getError().getErrorCode());
        assertEquals("field1: must not be null", exception.getError().getDetails());
    }

    @Test
    void getPaymentMethods_WithUnexpectedException_ShouldThrowBusinessException() throws Exception {
        String jsonData = "{\"service\":\"PIX\",\"txId\":\"test-tx-123\"}";
        when(mapper.writeValueAsString(any())).thenReturn(jsonData);
        when(callbackTypeDetector.isValidCallbackType(anyString())).thenReturn(true);
        doNothing().when(callbackTypeDetector).detectTypeAndValidate(anyString());
        doThrow(new RuntimeException("Unexpected error"))
                .when(callbackService).processCallbackAsync(any(CallbackRequest.class));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            callbackController.processCallback(pixCallbackRequest);
        });

        assertNotNull(exception);
        assertEquals("Erro ao processar callback", exception.getError().getMessage());
        assertEquals("ERROR_PROCESS_CALLBACK", exception.getError().getErrorCode());
    }

    @Test
    void getPaymentMethods_WithBusinessException_ShouldRethrowAsIs() throws Exception {
        String jsonData = "{\"service\":\"PIX\",\"txId\":\"test-tx-123\"}";
        when(mapper.writeValueAsString(any())).thenReturn(jsonData);
        when(callbackTypeDetector.isValidCallbackType(anyString())).thenReturn(true);
        
        BusinessException originalException = new BusinessException("Original error", "ORIGINAL_ERROR");
        doThrow(originalException).when(callbackTypeDetector).detectTypeAndValidate(anyString());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            callbackController.processCallback(pixCallbackRequest);
        });

        assertNotNull(exception);
        assertEquals(originalException, exception);
    }
}

