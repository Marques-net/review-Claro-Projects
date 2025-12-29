package com.omp.hub.callback.domain.ports.client;

import java.util.UUID;

import com.omp.hub.callback.domain.model.dto.customer.CustomerMobileResponse;

public interface CustomerMobilePort {

    CustomerMobileResponse send(UUID uuid, String document, String status);

}
