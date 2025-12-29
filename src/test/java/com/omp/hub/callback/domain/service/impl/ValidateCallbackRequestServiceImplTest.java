package com.omp.hub.callback.domain.service.impl;

import com.omp.hub.callback.domain.service.check.impl.CheckTypeObjectServiceImpl;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidateCallbackRequestServiceImplTest {

    private CheckTypeObjectServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CheckTypeObjectServiceImpl();
    }

    @Test
    void isValid_WithValidPixCallbackJson_ShouldReturnTrue() {
        String validJson = "{\"txId\":\"123\",\"service\":\"PIX\",\"paymentType\":\"PIX_NORMAL\"}";
        Boolean result = service.isValid(validJson, PixCallbackRequest.class);
        assertTrue(result);
    }

    @Test
    void isValid_WithValidPixCallbackJsonWithIdentifier_ShouldReturnTrue() {
        String validJson = "{\"identifier\":\"123\",\"service\":\"PIX\"}";
        Boolean result = service.isValid(validJson, PixCallbackRequest.class);
        assertTrue(result);
    }

    @Test
    void isValid_WithInvalidJson_ShouldReturnFalse() {
        String invalidJson = "{invalid json}";
        Boolean result = service.isValid(invalidJson, PixCallbackRequest.class);
        assertFalse(result);
    }

    @Test
    void isValid_WithEmptyJson_ShouldReturnFalse() {
        String emptyJson = "";
        Boolean result = service.isValid(emptyJson, PixCallbackRequest.class);
        assertFalse(result);
    }

    @Test
    void isValid_WithNullJson_ShouldReturnFalse() {
        String nullJson = null;
        Boolean result = service.isValid(nullJson, PixCallbackRequest.class);
        assertFalse(result);
    }

    @Test
    void isValid_WithValidCreditCardCallbackJson_ShouldReturnTrue() {
        String validJson = "{\"payment\":{\"orderId\":\"order123\"}}";
        Boolean result = service.isValid(validJson, CreditCardCallbackRequest.class);
        assertTrue(result);
    }

    @Test
    void isValid_WithValidTefWebCallbackJson_ShouldReturnTrue() {
        String validJson = "{\"service\":\"TEFWEB\",\"paymentType\":\"DEBIT\",\"sales\":[]}";
        Boolean result = service.isValid(validJson, TefWebCallbackRequest.class);
        assertTrue(result);
    }

    @Test
    void isValid_WithValidTransactionsRequestJson_ShouldReturnTrue() {
        String validJson = "{\"callbackTarget\":\"TARGET\",\"event\":{}}";
        Boolean result = service.isValid(validJson, TransactionsRequest.class);
        assertTrue(result);
    }

    @Test
    void isValid_WithMismatchedJsonAndClass_ShouldReturnFalse() {
        String pixJson = "{\"txId\":\"123\",\"service\":\"PIX\"}";
        Boolean result = service.isValid(pixJson, CreditCardCallbackRequest.class);
        assertFalse(result);
    }

    @Test
    void isValid_WithMalformedJson_ShouldReturnFalse() {
        String malformedJson = "{\"key\":\"value\"";
        Boolean result = service.isValid(malformedJson, PixCallbackRequest.class);
        assertFalse(result);
    }

    @Test
    void isValid_WithJsonArrayInsteadOfObject_ShouldReturnFalse() {
        String jsonArray = "[{\"txId\":\"123\"}]";
        Boolean result = service.isValid(jsonArray, PixCallbackRequest.class);
        assertFalse(result);
    }

    @Test
    void isValid_WithJsonContainingSpecialCharacters_ShouldReturnTrue() {
        String jsonWithSpecialChars = "{\"txId\":\"123-ABC_DEF\"}";
        Boolean result = service.isValid(jsonWithSpecialChars, PixCallbackRequest.class);
        assertTrue(result);
    }

    @Test
    void isValid_WithMissingRequiredFieldsForTefWeb_ShouldReturnFalse() {
        String json = "{\"service\":\"TEFWEB\"}";
        Boolean result = service.isValid(json, TefWebCallbackRequest.class);
        assertFalse(result);
    }

    @Test
    void isValid_WithMissingRequiredFieldsForTransactions_ShouldReturnFalse() {
        String json = "{\"ompTransactionId\":\"123\"}";
        Boolean result = service.isValid(json, TransactionsRequest.class);
        assertFalse(result);
    }
}
