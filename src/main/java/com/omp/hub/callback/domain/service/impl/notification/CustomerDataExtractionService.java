package com.omp.hub.callback.domain.service.impl.notification;

import java.util.UUID;

import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;

public interface CustomerDataExtractionService {

    ExtractedCustomerDataDTO extractCustomerDataFromPaymentInfo(UUID uuid, String txId);

    ExtractedCustomerDataDTO enrichCustomerData(UUID uuid, ExtractedCustomerDataDTO customerData);
}