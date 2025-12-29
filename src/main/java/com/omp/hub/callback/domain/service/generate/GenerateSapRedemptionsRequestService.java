package com.omp.hub.callback.domain.service.generate;

import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.SapRedemptionsRequest;
import org.springframework.stereotype.Service;

@Service
public interface GenerateSapRedemptionsRequestService {

    public SapRedemptionsRequest generateRequest(InformationPaymentDTO info);
}