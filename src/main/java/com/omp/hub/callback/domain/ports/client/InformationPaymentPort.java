package com.omp.hub.callback.domain.ports.client;

import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;

public interface InformationPaymentPort {

    public InformationPaymentDTO sendCreate(InformationPaymentDTO request);
    public InformationPaymentDTO sendUpdate(InformationPaymentDTO request);
    public InformationPaymentDTO sendFindByIdentifier(String identifier);
    public InformationPaymentDTO updatePaymentInList(String identifier, String paymentType, InformationPaymentDTO request);
}
