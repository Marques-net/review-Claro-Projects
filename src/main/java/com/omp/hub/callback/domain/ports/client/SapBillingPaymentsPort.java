package com.omp.hub.callback.domain.ports.client;

import java.util.UUID;

import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsResponse;

import okhttp3.Headers;

public interface SapBillingPaymentsPort {

    SapBillingPaymentsResponse send(UUID uuid, SapBillingPaymentsRequest request, Headers.Builder builder);
}
