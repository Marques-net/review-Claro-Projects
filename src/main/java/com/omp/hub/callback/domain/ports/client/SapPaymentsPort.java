package com.omp.hub.callback.domain.ports.client;

import java.util.UUID;

import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsResponse;

import okhttp3.Headers;

public interface SapPaymentsPort {

    SapPaymentsResponse send(UUID uuid, SapPaymentsRequest request, Headers.Builder builder);
}
