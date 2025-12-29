package com.omp.hub.callback.domain.service.generate;

import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;

public interface GenerateCallbackCardService {

    OmphubTransactionNotificationRequest generateRequest(CreditCardCallbackRequest request);
}
