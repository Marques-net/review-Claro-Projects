package com.omp.hub.callback.application.usecase.callback;

import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;

public interface TransactionsCallbackUseCase {

    void sendCallback(TransactionsRequest request);
}
