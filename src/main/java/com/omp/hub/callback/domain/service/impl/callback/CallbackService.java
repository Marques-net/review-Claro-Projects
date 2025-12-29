package com.omp.hub.callback.domain.service.impl.callback;

import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;

public interface CallbackService {
    
    void processCallback(String object);
    
    void processCallbackAsync(CallbackRequest<?> callbackRequest);

}
