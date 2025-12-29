package com.omp.hub.callback.domain.service.generate;

import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;

public interface GenerateCallbackTefWebService {

    OmphubTransactionNotificationRequest generateRequest(TefWebCallbackRequest request, String ompTransactionId);

    OmphubTransactionNotificationRequest generateConsolidatedRequest(TefWebCallbackRequest request, String transactionOrderId, InformationPaymentDTO info);
}
