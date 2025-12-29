package com.omp.hub.callback.domain.ports.client;

import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.DataDTO;
import com.omp.hub.callback.domain.model.dto.journey.HeadersDTO;

import java.util.UUID;

public interface UtilsPort {

    String getStorePdv(HeadersDTO headers);

    InformationPaymentDTO generateInfoPayment(
            UUID uuid, DataDTO dto,
            HeadersDTO headers, String identifier);
}
