package com.omp.hub.callback.domain.ports.client;

import java.util.UUID;

import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersResponse;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;

public interface CustomerContractsSubscribersPort {
    CustomerContractsSubscribersResponse send(UUID uuid, String documento, ExtractedCustomerDataDTO customerData);
}