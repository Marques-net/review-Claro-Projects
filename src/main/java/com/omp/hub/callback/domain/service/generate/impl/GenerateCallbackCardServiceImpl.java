package com.omp.hub.callback.domain.service.generate.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.EventDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import com.omp.hub.callback.domain.service.generate.GenerateCallbackCardService;

@Service
public class GenerateCallbackCardServiceImpl implements GenerateCallbackCardService {

    public OmphubTransactionNotificationRequest generateRequest(CreditCardCallbackRequest request) {

        Map<String, Object> cardData = new HashMap<>();
        cardData.put("statusCode", request.getStatusCode());
        cardData.put("statusMessage", request.getStatusMessage());
        cardData.put("transactionId", request.getTransactionId());
        cardData.put("flag", request.getFlag());
        cardData.put("card", request.getCard());
        cardData.put("value", request.getValue() != null ? request.getValue().toString() : null);
        cardData.put("numberInstallments", request.getNumberInstallments() != null ? request.getNumberInstallments().toString() : null);
        cardData.put("orderId", request.getOrderId());
        cardData.put("orderDate", request.getOrderDate());
        cardData.put("acquirator", request.getAcquirator());
        cardData.put("retryProcessor", request.getRetryProcessor());
        cardData.put("antifraud", request.getAntifraud());

        Map<String, Object> map = new HashMap<>();
        map.put("card", cardData);


        return OmphubTransactionNotificationRequest.builder()
                .data(com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.DataDTO.builder()
                        .callbackTarget(request.getService() != null ? request.getService() : "")
                        .event(EventDTO.builder()
                                .type("PAYMENT")
                                .payment(List.of(map))
                                .build())
                        .build())
                .build();
    }
}
