package com.omp.hub.callback.domain.service.generate.impl;

import org.springframework.stereotype.Service;

import com.omp.hub.callback.domain.model.dto.callback.transactions.EventDTO;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TargetPaymentMethodDTO;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.DataDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import com.omp.hub.callback.domain.service.generate.GenerateCallbackTransactionsService;

@Service
public class GenerateCallbackTransactionsServiceImpl implements GenerateCallbackTransactionsService {

    public OmphubTransactionNotificationRequest generateRequest(TransactionsRequest request) {

        String callbackTarget = request.getCallbackTarget() != null && !request.getCallbackTarget().isBlank() ?
                request.getCallbackTarget() : request.getTargetSystem();
        
        Object targetPaymentMethod = buildTargetPaymentMethod(request.getEvent());
        
        return OmphubTransactionNotificationRequest.builder()
                .data(DataDTO.builder()
                        .callbackTarget(callbackTarget)
                        .event(com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.EventDTO.builder()
                                .type(request.getEvent().getType())
                                .targetPaymentMethod(targetPaymentMethod)
                                .payment(request.getEvent().getPayment() != null ? 
                                        request.getEvent().getPayment().stream()
                                                .map(p -> (Object) p)
                                                .toList() : null)
                                .build())
                        .build())
                .build();
    }
    
    private Object buildTargetPaymentMethod(EventDTO event) {
        if (event == null) {
            return null;
        }
        
        if (event.getTargetPaymentMethod() != null && 
            (event.getTargetPaymentMethod().getPaymentMethod() != null || 
             event.getTargetPaymentMethod().getRecurrenceId() != null)) {
            return event.getTargetPaymentMethod();
        }
        
        if (event.getPaymentMethod() != null || event.getRecurrenceId() != null || 
            event.getStatus() != null || event.getActivation() != null || event.getUpdates() != null) {

            return TargetPaymentMethodDTO.builder()
                    .paymentMethod(event.getPaymentMethod())
                    .recurrenceId(event.getRecurrenceId())
                    .status(event.getStatus())
                    .activation(event.getActivation())
                    .updates(event.getUpdates())
                    .build();
        }
        
        return event.getTargetPaymentMethod();
    }
}