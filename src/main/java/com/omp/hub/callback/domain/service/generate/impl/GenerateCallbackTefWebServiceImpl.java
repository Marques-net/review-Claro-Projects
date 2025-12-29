package com.omp.hub.callback.domain.service.generate.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.omp.hub.callback.domain.enums.PaymentTypeEnum;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.DataDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.EventDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import com.omp.hub.callback.domain.service.generate.GenerateCallbackTefWebService;

@Service
public class GenerateCallbackTefWebServiceImpl implements GenerateCallbackTefWebService {

    private static final Logger logger = LoggerFactory.getLogger(GenerateCallbackTefWebServiceImpl.class);

    public OmphubTransactionNotificationRequest generateRequest(TefWebCallbackRequest request, String transactionOrderId) {

        return OmphubTransactionNotificationRequest.builder()
                .data(DataDTO.builder()
                        .callbackTarget(request.getService() != null ? request.getService() : "")
                        .event(EventDTO.builder()
                                .issueDate(java.time.OffsetDateTime.now().toString())
                                .transactionOrderId(transactionOrderId)
                                .status("PAGO")
                                .type("PAYMENT")
                                .payment(this.getPayments(request))
                                .build())
                        .build())
                .build();
    }

    @Override
    public OmphubTransactionNotificationRequest generateConsolidatedRequest(
            TefWebCallbackRequest request, String transactionOrderId, InformationPaymentDTO info) {

        List<Object> consolidatedPayments = new ArrayList<>();

        if (info != null && info.getPayments() != null) {
            for (PaymentDTO payment : info.getPayments()) {
                if (payment.getType() == PaymentTypeEnum.CASH && payment.getCallback() != null) {
                    consolidatedPayments.add(buildCashPayment(payment.getCallback()));
                }
            }
        }

        consolidatedPayments.addAll(this.getPayments(request));

        logger.info("Callback consolidado TEFWEB com {} pagamentos", consolidatedPayments.size());

        return OmphubTransactionNotificationRequest.builder()
                .data(DataDTO.builder()
                        .callbackTarget(request.getService() != null ? request.getService() : "")
                        .event(EventDTO.builder()
                                .issueDate(java.time.OffsetDateTime.now().toString())
                                .transactionOrderId(transactionOrderId)
                                .status("PAGO")
                                .type("PAYMENT")
                                .payment(consolidatedPayments)
                                .build())
                        .build())
                .build();
    }

    private List<Object> getPayments(TefWebCallbackRequest request) {

        List<Object> list = new ArrayList<>();

        if (request != null && request.getSales() != null && !request.getSales().isEmpty()) {

            Map<String, Object> map = new HashMap<>();
            Map<String, Object> map2 = new HashMap<>();
            map2.put("sales", request.getSales());
            map.put("tefweb", map2);
            list.add(map);

        }

        return list;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildCashPayment(Object cashCallback) {
        Map<String, Object> cashPayment = new HashMap<>();

        if (cashCallback instanceof Map) {
            Map<String, Object> callbackMap = (Map<String, Object>) cashCallback;
            cashPayment.put("cash", callbackMap);
        } else {
            cashPayment.put("cash", cashCallback);
        }

        return cashPayment;
    }
}
