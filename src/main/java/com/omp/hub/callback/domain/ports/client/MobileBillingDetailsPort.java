package com.omp.hub.callback.domain.ports.client;

import java.util.UUID;

import com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsResponse;

public interface MobileBillingDetailsPort {

    MobileBillingDetailsResponse getCustomerBillingDetailsByMobileBan(UUID uuid, String mobileBan);

}