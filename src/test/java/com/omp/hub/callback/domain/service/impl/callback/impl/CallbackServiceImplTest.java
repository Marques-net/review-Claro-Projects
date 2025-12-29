package com.omp.hub.callback.domain.service.impl.callback.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.usecase.callback.CreditCardCallbackUseCase;
import com.omp.hub.callback.application.usecase.callback.PixCallbackUseCase;
import com.omp.hub.callback.application.usecase.callback.TefWebCallbackUseCase;
import com.omp.hub.callback.application.usecase.callback.TransactionsCallbackUseCase;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;
import com.omp.hub.callback.domain.service.check.CheckTypeObjectService;
import com.omp.hub.callback.infrastructure.persistence.message.sqs.SqsMessageRepository;

@ExtendWith(MockitoExtension.class)
class CallbackServiceImplTest {

    @Mock
    private PixCallbackUseCase pixCallbackUseCase;

    @Mock
    private CreditCardCallbackUseCase creditCardCallbackUseCase;

    @Mock
    private TefWebCallbackUseCase tefwebCallbackUseCase;

    @Mock
    private TransactionsCallbackUseCase transactionsCallbackUseCase;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CheckTypeObjectService validateService;

    @Mock
    private SqsMessageRepository sqsMessageRepository;

    @InjectMocks
    private CallbackServiceImpl callbackService;

    private String validPixCallback;
    private String validCreditCardCallback;
    private String validTefWebCallback;
    private String validTransactionsCallback;
    private String invalidCallback;
    private CallbackRequest<PixCallbackRequest> pixCallbackRequest;

    @BeforeEach
    void setUp() {
        validPixCallback = "{\"service\":\"PIX\",\"txId\":\"test-tx-123\"}";
        validCreditCardCallback = "{\"service\":\"CREDIT_CARD\",\"transactionId\":\"cc-tx-456\"}";
        validTefWebCallback = "{\"service\":\"TEF_WEB\",\"paymentType\":\"DEBIT\"}";
        validTransactionsCallback = "{\"callbackTarget\":\"SYSTEM_A\"}";
        invalidCallback = "invalid json";
        
        PixCallbackRequest pixData = PixCallbackRequest.builder()
                .service("PIX")
                .txId("test-tx-123")
                .build();
        pixCallbackRequest = CallbackRequest.<PixCallbackRequest>builder()
                .data(pixData)
                .build();
    }

    @Test
    void processCallback_WithValidPixCallback_ShouldProcessPixCallback() throws Exception {
        // Given
        PixCallbackRequest pixRequest = new PixCallbackRequest();
        when(validateService.isValid(validPixCallback, PixCallbackRequest.class)).thenReturn(true);
        when(objectMapper.readValue(validPixCallback, PixCallbackRequest.class)).thenReturn(pixRequest);
        doNothing().when(pixCallbackUseCase).sendCallback(pixRequest);

        // When
        callbackService.processCallback(validPixCallback);

        // Then
        verify(validateService).isValid(validPixCallback, PixCallbackRequest.class);
        verify(pixCallbackUseCase).sendCallback(pixRequest);
        verifyNoInteractions(creditCardCallbackUseCase, tefwebCallbackUseCase, transactionsCallbackUseCase);
    }

    @Test
    void processCallback_WithValidCreditCardCallback_ShouldProcessCreditCardCallback() throws Exception {
        // Given
        CreditCardCallbackRequest creditCardRequest = new CreditCardCallbackRequest();
        when(validateService.isValid(validCreditCardCallback, PixCallbackRequest.class)).thenReturn(false);
        when(validateService.isValid(validCreditCardCallback, CreditCardCallbackRequest.class)).thenReturn(true);
        when(objectMapper.readValue(validCreditCardCallback, CreditCardCallbackRequest.class)).thenReturn(creditCardRequest);
        doNothing().when(creditCardCallbackUseCase).sendCallback(creditCardRequest);

        // When
        callbackService.processCallback(validCreditCardCallback);

        // Then
        verify(validateService).isValid(validCreditCardCallback, PixCallbackRequest.class);
        verify(validateService).isValid(validCreditCardCallback, CreditCardCallbackRequest.class);
        verify(creditCardCallbackUseCase).sendCallback(creditCardRequest);
        verifyNoInteractions(pixCallbackUseCase, tefwebCallbackUseCase, transactionsCallbackUseCase);
    }

    @Test
    void processCallback_WithValidTefWebCallback_ShouldProcessTefWebCallback() throws Exception {
        // Given
        TefWebCallbackRequest tefWebRequest = new TefWebCallbackRequest();
        when(validateService.isValid(validTefWebCallback, PixCallbackRequest.class)).thenReturn(false);
        when(validateService.isValid(validTefWebCallback, CreditCardCallbackRequest.class)).thenReturn(false);
        when(validateService.isValid(validTefWebCallback, TefWebCallbackRequest.class)).thenReturn(true);
        when(objectMapper.readValue(validTefWebCallback, TefWebCallbackRequest.class)).thenReturn(tefWebRequest);
        doNothing().when(tefwebCallbackUseCase).sendCallback(tefWebRequest);

        // When
        callbackService.processCallback(validTefWebCallback);

        // Then
        verify(validateService).isValid(validTefWebCallback, PixCallbackRequest.class);
        verify(validateService).isValid(validTefWebCallback, CreditCardCallbackRequest.class);
        verify(validateService).isValid(validTefWebCallback, TefWebCallbackRequest.class);
        verify(tefwebCallbackUseCase).sendCallback(tefWebRequest);
        verifyNoInteractions(pixCallbackUseCase, creditCardCallbackUseCase, transactionsCallbackUseCase);
    }

    @Test
    void processCallback_WithValidTransactionsCallback_ShouldProcessTransactionsCallback() throws Exception {
        // Given
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        when(validateService.isValid(validTransactionsCallback, PixCallbackRequest.class)).thenReturn(false);
        when(validateService.isValid(validTransactionsCallback, CreditCardCallbackRequest.class)).thenReturn(false);
        when(validateService.isValid(validTransactionsCallback, TefWebCallbackRequest.class)).thenReturn(false);
        when(validateService.isValid(validTransactionsCallback, TransactionsRequest.class)).thenReturn(true);
        when(objectMapper.readValue(validTransactionsCallback, TransactionsRequest.class)).thenReturn(transactionsRequest);
        doNothing().when(transactionsCallbackUseCase).sendCallback(transactionsRequest);

        // When
        callbackService.processCallback(validTransactionsCallback);

        // Then
        verify(validateService).isValid(validTransactionsCallback, PixCallbackRequest.class);
        verify(validateService).isValid(validTransactionsCallback, CreditCardCallbackRequest.class);
        verify(validateService).isValid(validTransactionsCallback, TefWebCallbackRequest.class);
        verify(validateService).isValid(validTransactionsCallback, TransactionsRequest.class);
        verify(transactionsCallbackUseCase).sendCallback(transactionsRequest);
        verifyNoInteractions(pixCallbackUseCase, creditCardCallbackUseCase, tefwebCallbackUseCase);
    }

    @Test
    void processCallback_WithInvalidCallbackType_ShouldThrowBusinessException() {
        // Given
        when(validateService.isValid(invalidCallback, PixCallbackRequest.class)).thenReturn(false);
        when(validateService.isValid(invalidCallback, CreditCardCallbackRequest.class)).thenReturn(false);
        when(validateService.isValid(invalidCallback, TefWebCallbackRequest.class)).thenReturn(false);
        when(validateService.isValid(invalidCallback, TransactionsRequest.class)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            callbackService.processCallback(invalidCallback);
        });

        assertEquals("Payload inválido: não corresponde a nenhum tipo de callback suportado (PIX, CreditCard, TefWeb ou Transactions)", exception.getError().getMessage());
        assertEquals("INVALID_CALLBACK_TYPE", exception.getError().getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getError().getStatus());

        verifyNoInteractions(pixCallbackUseCase, creditCardCallbackUseCase, tefwebCallbackUseCase, transactionsCallbackUseCase);
    }

    @Test
    void processCallback_WithJsonProcessingException_ShouldThrowBusinessException() throws Exception {
        // Given
        when(validateService.isValid(validPixCallback, PixCallbackRequest.class)).thenReturn(true);
        when(objectMapper.readValue(validPixCallback, PixCallbackRequest.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "Invalid JSON"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            callbackService.processCallback(validPixCallback);
        });

        assertEquals("ERROR_CONVERT_JSON", exception.getError().getErrorCode());
        assertEquals("Erro convert Json", exception.getError().getMessage());
    }

    @Test
    void processCallbackAsync_WithValidCallbackRequest_ShouldSendToSqs() throws Exception {
        // Given
        doNothing().when(sqsMessageRepository).sendMessage(any(CallbackRequest.class));

        // When
        callbackService.processCallbackAsync(pixCallbackRequest);

        // Then
        verify(sqsMessageRepository).sendMessage(pixCallbackRequest);
    }

    @Test
    void processCallbackAsync_WithSqsFailure_ShouldThrowBusinessException() throws Exception {
        // Given
        doThrow(new RuntimeException("SQS error")).when(sqsMessageRepository).sendMessage(any(CallbackRequest.class));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            callbackService.processCallbackAsync(pixCallbackRequest);
        });

        assertEquals("ERROR_SEND_TO_QUEUE", exception.getError().getErrorCode());
        assertEquals("Erro ao enviar callback para fila", exception.getError().getMessage());
    }
}