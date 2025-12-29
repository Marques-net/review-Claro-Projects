package com.omp.hub.callback.domain.service.validation;

import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.SapRedemptionsRequest;

public interface SapRequestValidationService {
    
    void validateRedemptionsRequest(SapRedemptionsRequest request);
    
    void validatePaymentsRequest(SapPaymentsRequest request);
    
    void validateBillingPaymentsRequest(SapBillingPaymentsRequest request);
    
}
