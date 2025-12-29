package com.omp.hub.callback.domain.service.impl;

import com.omp.hub.callback.domain.model.dto.callback.transactions.ActivationDTO;
import com.omp.hub.callback.domain.model.dto.callback.transactions.JourneyDataDTO;
import com.omp.hub.callback.domain.model.dto.callback.transactions.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TargetPaymentMethodDTO;
import com.omp.hub.callback.domain.model.dto.callback.transactions.UpdatesDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GenerateCallbackTransactionsServiceImplTest {

    @InjectMocks
    private com.omp.hub.callback.domain.service.generate.impl.GenerateCallbackTransactionsServiceImpl service;

    @Test
    void generateRequest_WithValidRequest_ShouldReturnPopulatedNotificationRequest() {
        // Given
        TransactionsRequest request = new TransactionsRequest();
        request.setCallbackTarget("https://example.com/callback");
        
        com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO event = new com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO();
        event.setType("CHANGE_PAYMENT_METHOD");
        TargetPaymentMethodDTO targetPaymentMethod = new TargetPaymentMethodDTO();
        targetPaymentMethod.setPaymentMethod("PIX_AUTOMATICO");
        targetPaymentMethod.setRecurrenceId("rec-123");
        event.setTargetPaymentMethod(targetPaymentMethod);
        request.setEvent(event);

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals("https://example.com/callback", result.getData().getCallbackTarget());
        assertNotNull(result.getData().getEvent());
        assertEquals("CHANGE_PAYMENT_METHOD", result.getData().getEvent().getType());
        assertNotNull(result.getData().getEvent().getTargetPaymentMethod());
    }

    @Test
    void generateRequest_WithNullCallbackTarget_ShouldUseTargetSystem() {
        // Given
        TransactionsRequest request = new TransactionsRequest();
        request.setCallbackTarget(null);
        request.setTargetSystem("TargetSystemFallback");
        
        com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO event = new com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO();
        event.setType("CHANGE_PAYMENT_METHOD");
        request.setEvent(event);

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals("TargetSystemFallback", result.getData().getCallbackTarget());
        assertNotNull(result.getData().getEvent());
        assertEquals("CHANGE_PAYMENT_METHOD", result.getData().getEvent().getType());
    }
    
    @Test
    void generateRequest_WithBlankCallbackTarget_ShouldUseTargetSystem() {
        // Given
        TransactionsRequest request = new TransactionsRequest();
        request.setCallbackTarget("   ");
        request.setTargetSystem("TargetSystemFallback");
        
        com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO event = new com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO();
        event.setType("PAYMENT");
        request.setEvent(event);

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertEquals("TargetSystemFallback", result.getData().getCallbackTarget());
    }
    
    @Test
    void generateRequest_WithEventLevelFields_ShouldBuildTargetPaymentMethod() {
        // Given - campos no nível do evento (formato antigo)
        TransactionsRequest request = new TransactionsRequest();
        request.setCallbackTarget("TestCallback");
        
        com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO event = new com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO();
        event.setType("CHANGE_PAYMENT_METHOD");
        event.setPaymentMethod("PIX_AUTOMATICO");
        event.setRecurrenceId("rec-456");
        event.setStatus("CRIADA");
        
        ActivationDTO activation = new ActivationDTO();
        JourneyDataDTO journeyData = new JourneyDataDTO();
        journeyData.setTxId("tx-123");
        activation.setJourneyData(journeyData);
        event.setActivation(activation);
        
        UpdatesDTO update = new UpdatesDTO();
        update.setStatus("PENDENTE");
        event.setUpdates(List.of(update));
        
        request.setEvent(event);

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData().getEvent().getTargetPaymentMethod());
        
        // O targetPaymentMethod deve ter sido construído a partir dos campos do evento
        TargetPaymentMethodDTO builtTarget = (TargetPaymentMethodDTO) result.getData().getEvent().getTargetPaymentMethod();
        assertEquals("PIX_AUTOMATICO", builtTarget.getPaymentMethod());
        assertEquals("rec-456", builtTarget.getRecurrenceId());
        assertEquals("CRIADA", builtTarget.getStatus());
        assertNotNull(builtTarget.getActivation());
        assertEquals("tx-123", builtTarget.getActivation().getJourneyData().getTxId());
        assertNotNull(builtTarget.getUpdates());
        assertEquals(1, builtTarget.getUpdates().size());
    }
    
    @Test
    void generateRequest_WithPaymentList_ShouldIncludePayments() {
        // Given
        TransactionsRequest request = new TransactionsRequest();
        request.setCallbackTarget("TestCallback");
        
        com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO event = new com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO();
        event.setType("PAYMENT");
        
        PaymentDTO payment1 = new PaymentDTO();
        payment1.setType("PIX");
        PaymentDTO payment2 = new PaymentDTO();
        payment2.setType("CARD");
        event.setPayment(List.of(payment1, payment2));
        
        request.setEvent(event);

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData().getEvent().getPayment());
        assertEquals(2, result.getData().getEvent().getPayment().size());
    }
    
    @Test
    void generateRequest_WithPopulatedTargetPaymentMethod_ShouldUseThatDirectly() {
        // Given - targetPaymentMethod já populado
        TransactionsRequest request = new TransactionsRequest();
        request.setCallbackTarget("TestCallback");
        
        com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO event = new com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO();
        event.setType("CHANGE_PAYMENT_METHOD");
        
        TargetPaymentMethodDTO target = new TargetPaymentMethodDTO();
        target.setPaymentMethod("DEBITO_AUTOMATICO");
        target.setRecurrenceId("rec-direct-123");
        target.setStatus("ATIVA");
        event.setTargetPaymentMethod(target);
        
        // Mesmo tendo campos no evento, deve usar o targetPaymentMethod existente
        event.setPaymentMethod("PIX_AUTOMATICO");
        event.setRecurrenceId("rec-event-456");
        
        request.setEvent(event);

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        TargetPaymentMethodDTO resultTarget = (TargetPaymentMethodDTO) result.getData().getEvent().getTargetPaymentMethod();
        assertEquals("DEBITO_AUTOMATICO", resultTarget.getPaymentMethod());
        assertEquals("rec-direct-123", resultTarget.getRecurrenceId());
        assertEquals("ATIVA", resultTarget.getStatus());
    }
    
    @Test
    void generateRequest_WithNullEvent_ShouldHandleGracefully() {
        // Given
        TransactionsRequest request = new TransactionsRequest();
        request.setCallbackTarget("TestCallback");
        request.setEvent(null);

        // When/Then - deve lançar NullPointerException
        assertThrows(NullPointerException.class, () -> service.generateRequest(request));
    }
    
    @Test
    void generateRequest_WithOnlyActivation_ShouldBuildTargetPaymentMethod() {
        // Given - somente activation no nível do evento
        TransactionsRequest request = new TransactionsRequest();
        request.setCallbackTarget("TestCallback");
        
        com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO event = new com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO();
        event.setType("CHANGE_PAYMENT_METHOD");
        
        ActivationDTO activation = new ActivationDTO();
        JourneyDataDTO journeyData = new JourneyDataDTO();
        journeyData.setTxId("tx-only-activation");
        activation.setJourneyData(journeyData);
        event.setActivation(activation);
        
        request.setEvent(event);

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData().getEvent().getTargetPaymentMethod());
        
        TargetPaymentMethodDTO builtTarget = (TargetPaymentMethodDTO) result.getData().getEvent().getTargetPaymentMethod();
        assertNotNull(builtTarget.getActivation());
        assertEquals("tx-only-activation", builtTarget.getActivation().getJourneyData().getTxId());
    }
    
    @Test
    void generateRequest_WithEmptyTargetPaymentMethod_ShouldUseEventFields() {
        // Given - targetPaymentMethod vazio (sem paymentMethod nem recurrenceId)
        TransactionsRequest request = new TransactionsRequest();
        request.setCallbackTarget("TestCallback");
        
        com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO event = new com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO();
        event.setType("CHANGE_PAYMENT_METHOD");
        
        // TargetPaymentMethod vazio
        TargetPaymentMethodDTO emptyTarget = new TargetPaymentMethodDTO();
        event.setTargetPaymentMethod(emptyTarget);
        
        // Campos no nível do evento
        event.setPaymentMethod("BOLETO");
        event.setRecurrenceId("rec-fallback");
        event.setStatus("PENDENTE");
        
        request.setEvent(event);

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        TargetPaymentMethodDTO resultTarget = (TargetPaymentMethodDTO) result.getData().getEvent().getTargetPaymentMethod();
        assertEquals("BOLETO", resultTarget.getPaymentMethod());
        assertEquals("rec-fallback", resultTarget.getRecurrenceId());
        assertEquals("PENDENTE", resultTarget.getStatus());
    }
    
    @Test
    void generateRequest_WithNoTargetPaymentMethodFields_ShouldReturnNull() {
        // Given - nem targetPaymentMethod populado, nem campos no evento
        TransactionsRequest request = new TransactionsRequest();
        request.setCallbackTarget("TestCallback");
        
        com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO event = new com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO();
        event.setType("PAYMENT");
        // Sem targetPaymentMethod e sem campos no evento
        
        request.setEvent(event);

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertNull(result.getData().getEvent().getTargetPaymentMethod());
    }

    @Test
    void generateRequest_WithOmpTransactionId_ShouldIncludeOmpTransactionId() {
        // Given
        TransactionsRequest request = new TransactionsRequest();
        request.setOmpTransactionId("omp-tx-123");
        request.setCallbackTarget("TestCallback");
        
        com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO event = new com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO();
        event.setType("PAYMENT");
        request.setEvent(event);

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertNull(result.getData().getOmpTransactionId()); // Campo está comentado
    }

    @Test
    void generateRequest_WithNullOmpTransactionId_ShouldHaveNullOmpTransactionId() {
        // Given
        TransactionsRequest request = new TransactionsRequest();
        request.setOmpTransactionId(null);
        request.setCallbackTarget("TestCallback");
        
        com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO event = new com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO();
        event.setType("PAYMENT");
        request.setEvent(event);

        // When
        OmphubTransactionNotificationRequest result = service.generateRequest(request);

        // Then
        assertNotNull(result);
        assertNull(result.getData().getOmpTransactionId());
    }
}
