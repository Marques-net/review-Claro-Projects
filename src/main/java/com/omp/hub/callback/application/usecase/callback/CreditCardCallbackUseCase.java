package com.omp.hub.callback.application.usecase.callback;

import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;

public interface CreditCardCallbackUseCase {

    void sendCallback(CreditCardCallbackRequest request);
}
