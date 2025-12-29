package com.omp.hub.callback.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.validator.CallbackValidator;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;
import com.omp.hub.callback.domain.service.check.CheckTypeObjectService;

@ExtendWith(MockitoExtension.class)
class CallbackTypeDetectorServiceTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private CheckTypeObjectService validateService;

    @Mock
    private CallbackValidator callbackValidator;

    @InjectMocks
    private CallbackTypeDetectorService service;

    private String jsonPayload;

    @BeforeEach
    void setUp() {
        jsonPayload = "{\"callbackTarget\":\"test\",\"event\":{\"type\":\"PAYMENT\"}}";
    }

    @Test
    void detectTypeAndValidate_WithTransactionsRequest_ShouldValidateAsTransactions() throws JsonProcessingException {
        // Given
        TransactionsRequest request = new TransactionsRequest();
        when(validateService.isValid(anyString(), eq(TransactionsRequest.class))).thenReturn(true);
        when(mapper.readValue(anyString(), eq(TransactionsRequest.class))).thenReturn(request);

        // When
        assertDoesNotThrow(() -> service.detectTypeAndValidate(jsonPayload));

        // Then
        verify(mapper).readValue(jsonPayload, TransactionsRequest.class);
        verify(callbackValidator).validate(request, "Transactions");
    }

    @Test
    void detectTypeAndValidate_WithTefWebRequest_ShouldValidateAsTefWeb() throws JsonProcessingException {
        // Given
        TefWebCallbackRequest request = new TefWebCallbackRequest();
        when(validateService.isValid(anyString(), eq(TransactionsRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(TefWebCallbackRequest.class))).thenReturn(true);
        when(mapper.readValue(anyString(), eq(TefWebCallbackRequest.class))).thenReturn(request);

        // When
        assertDoesNotThrow(() -> service.detectTypeAndValidate(jsonPayload));

        // Then
        verify(mapper).readValue(jsonPayload, TefWebCallbackRequest.class);
        verify(callbackValidator).validate(request, "TefWeb");
    }

    @Test
    void detectTypeAndValidate_WithCreditCardRequest_ShouldValidateAsCreditCard() throws JsonProcessingException {
        // Given
        CreditCardCallbackRequest request = new CreditCardCallbackRequest();
        when(validateService.isValid(anyString(), eq(TransactionsRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(TefWebCallbackRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(CreditCardCallbackRequest.class))).thenReturn(true);
        when(mapper.readValue(anyString(), eq(CreditCardCallbackRequest.class))).thenReturn(request);

        // When
        assertDoesNotThrow(() -> service.detectTypeAndValidate(jsonPayload));

        // Then
        verify(mapper).readValue(jsonPayload, CreditCardCallbackRequest.class);
        verify(callbackValidator).validate(request, "CreditCard");
    }

    @Test
    void detectTypeAndValidate_WithPixRequest_ShouldValidateAsPix() throws JsonProcessingException {
        // Given
        PixCallbackRequest request = new PixCallbackRequest();
        when(validateService.isValid(anyString(), eq(TransactionsRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(TefWebCallbackRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(CreditCardCallbackRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(PixCallbackRequest.class))).thenReturn(true);
        when(mapper.readValue(anyString(), eq(PixCallbackRequest.class))).thenReturn(request);

        // When
        assertDoesNotThrow(() -> service.detectTypeAndValidate(jsonPayload));

        // Then
        verify(mapper).readValue(jsonPayload, PixCallbackRequest.class);
        verify(callbackValidator).validate(request, "Pix");
    }

    @Test
    void detectTypeAndValidate_WithUnknownType_ShouldNotValidate() throws JsonProcessingException {
        // Given
        when(validateService.isValid(anyString(), any())).thenReturn(false);

        // When
        assertDoesNotThrow(() -> service.detectTypeAndValidate(jsonPayload));

        // Then
        verify(mapper, never()).readValue(anyString(), eq(TransactionsRequest.class));
        verify(mapper, never()).readValue(anyString(), eq(TefWebCallbackRequest.class));
        verify(mapper, never()).readValue(anyString(), eq(CreditCardCallbackRequest.class));
        verify(mapper, never()).readValue(anyString(), eq(PixCallbackRequest.class));
        verify(callbackValidator, never()).validate(any(), anyString());
    }

    @Test
    void isValidCallbackType_WithTransactionsRequest_ShouldReturnTrue() {
        // Given
        when(validateService.isValid(anyString(), eq(TransactionsRequest.class))).thenReturn(true);

        // When
        boolean result = service.isValidCallbackType(jsonPayload);

        // Then
        assertTrue(result);
    }

    @Test
    void isValidCallbackType_WithTefWebRequest_ShouldReturnTrue() {
        // Given
        when(validateService.isValid(anyString(), eq(TransactionsRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(TefWebCallbackRequest.class))).thenReturn(true);

        // When
        boolean result = service.isValidCallbackType(jsonPayload);

        // Then
        assertTrue(result);
    }

    @Test
    void isValidCallbackType_WithCreditCardRequest_ShouldReturnTrue() {
        // Given
        when(validateService.isValid(anyString(), eq(TransactionsRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(TefWebCallbackRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(CreditCardCallbackRequest.class))).thenReturn(true);

        // When
        boolean result = service.isValidCallbackType(jsonPayload);

        // Then
        assertTrue(result);
    }

    @Test
    void isValidCallbackType_WithPixRequest_ShouldReturnTrue() {
        // Given
        when(validateService.isValid(anyString(), eq(TransactionsRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(TefWebCallbackRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(CreditCardCallbackRequest.class))).thenReturn(false);
        when(validateService.isValid(anyString(), eq(PixCallbackRequest.class))).thenReturn(true);

        // When
        boolean result = service.isValidCallbackType(jsonPayload);

        // Then
        assertTrue(result);
    }

    @Test
    void isValidCallbackType_WithUnknownType_ShouldReturnFalse() {
        // Given
        when(validateService.isValid(anyString(), any())).thenReturn(false);

        // When
        boolean result = service.isValidCallbackType(jsonPayload);

        // Then
        assertFalse(result);
    }
}
