package com.omp.hub.callback.domain.ports.client;

import java.util.UUID;

import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageRequest;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageResponse;

import okhttp3.Headers;

public interface CommunicationPort {

    CommunicationMessageResponse sendMessage(UUID uuid, CommunicationMessageRequest request, Headers.Builder builder);
}
