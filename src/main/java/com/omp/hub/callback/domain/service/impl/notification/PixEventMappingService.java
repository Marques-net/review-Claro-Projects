package com.omp.hub.callback.domain.service.impl.notification;

import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;

public interface PixEventMappingService {

    PixAutomaticoEventEnum mapPaymentTypeToEvent(String txId, String paymentType);

    boolean isPixAutomaticoEvent(String txId, String paymentType);

    boolean shouldNotify(String txId, String eventType);

    PixAutomaticoEventEnum mapEventTypeToEnum(String txId, String eventType);

    PixAutomaticoEventEnum mapEventTypeToEnum(String txId, String eventType, String status, String paymentMethod);

    PixAutomaticoEventEnum mapEventTypeToEnum(String txId, String eventType, String status, String paymentMethod, String recurrenceId);

    String getTemplateCodeForEventType(String txId, PixAutomaticoEventEnum eventType);
}
