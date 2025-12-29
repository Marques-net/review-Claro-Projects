package com.omp.hub.callback.application.usecase.callback;

import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;

public interface TefWebCallbackUseCase {

    void sendCallback(TefWebCallbackRequest request);
}
