package com.omp.hub.callback.domain.ports.client;

import okhttp3.Headers;

import java.util.UUID;

import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsResponse;

public interface TransationsNotificationsPort {

    void send(UUID uuid, OmphubTransactionNotificationRequest request, Headers.Builder builder);
}
