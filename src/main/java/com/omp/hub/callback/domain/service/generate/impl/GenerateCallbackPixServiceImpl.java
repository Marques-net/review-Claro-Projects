package com.omp.hub.callback.domain.service.generate.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import com.omp.hub.callback.domain.service.generate.GenerateCallbackPixService;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.EventDTO;

@Service
public class GenerateCallbackPixServiceImpl implements GenerateCallbackPixService {

    public OmphubTransactionNotificationRequest generateRequest(PixCallbackRequest request) {
            
        boolean isGevenue = request.getService().toLowerCase().contains("gevenue");

        Map<String, Object> pixData = new HashMap<>();
        pixData.put("paymentDate", request.getPaymentDate());
        pixData.put("endToEndId", request.getEndToEndId());
        pixData.put("value", request.getValue());

        if (isGevenue) {
            pixData.put("txId", request.getTxId());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pix", pixData);

        return OmphubTransactionNotificationRequest.builder()
                .data(com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.DataDTO.builder()
                        .callbackTarget(request.getService() != null ? request.getService() : "")
                        .txId(isGevenue ? request.getTxId() : null)
                        .orderId(!isGevenue ? "H1" : null)
                        .event(EventDTO.builder()
                                .type("PAYMENT")
                                .payment(List.of(map))
                                .build())
                        .build())
                .build();
    }
}
