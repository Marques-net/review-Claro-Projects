package com.omp.hub.callback.domain.service.impl.notification;

import java.util.UUID;

import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
public interface NotificationService {

    void sendPixAutomaticoNotificationWithCustomerData(
            UUID uuid, String txId, PixAutomaticoEventEnum eventType, String name, String msisdn, String email);
}