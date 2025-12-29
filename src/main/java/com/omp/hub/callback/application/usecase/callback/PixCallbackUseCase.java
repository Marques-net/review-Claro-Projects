package com.omp.hub.callback.application.usecase.callback;

import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;


public interface PixCallbackUseCase {

    void sendCallback(PixCallbackRequest request);
    

}
