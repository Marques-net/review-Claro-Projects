package com.omp.hub.callback.domain.service.impl.notification;

import java.util.UUID;

import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;

public interface NotificationManagerService {

    boolean processPixAutomaticoNotification(UUID uuid, String txId, PixAutomaticoEventEnum eventType);

    ExtractedCustomerDataDTO extractCustomerDataFromTxId(UUID uuid, String txId);
}