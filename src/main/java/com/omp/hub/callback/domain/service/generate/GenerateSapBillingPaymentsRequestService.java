package com.omp.hub.callback.domain.service.generate;

import com.omp.hub.callback.domain.model.dto.callback.CallbackDTO;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsRequest;

public interface GenerateSapBillingPaymentsRequestService {

    SapBillingPaymentsRequest generateRequest(CallbackDTO request, InformationPaymentDTO info);
}
