package com.omp.hub.callback.domain.ports.client;

import java.util.UUID;

import com.omp.hub.callback.domain.model.dto.customer.residential.CustomerResidentialResponse;

public interface CustomerResidentialPort {

    CustomerResidentialResponse getCustomerContractsByPhoneNumber(UUID uuid, String phoneNumber);
    CustomerResidentialResponse getCustomerContractsByDocument(UUID uuid, String document);
}
