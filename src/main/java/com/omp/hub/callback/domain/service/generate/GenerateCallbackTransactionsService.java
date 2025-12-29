package com.omp.hub.callback.domain.service.generate;

import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;

public interface GenerateCallbackTransactionsService {

    OmphubTransactionNotificationRequest generateRequest(TransactionsRequest request);
}
