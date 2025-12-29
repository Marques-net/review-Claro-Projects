package com.omp.hub.callback.domain.service.generate;

import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;

public interface GenerateCallbackPixService {

    OmphubTransactionNotificationRequest generateRequest(PixCallbackRequest request);
}
