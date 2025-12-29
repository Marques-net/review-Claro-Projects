package com.omp.hub.callback.infrastructure.client;


import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationErrorDTO;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageRequest;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageResponse;
import com.omp.hub.callback.domain.ports.client.CommunicationPort;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import okhttp3.Headers.Builder;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunicationClient implements CommunicationPort {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationClient.class);
    private static final String HTTP_VERB = "POST";

    private final ApigeeUtils apigeeUtils;

    @Value("${client.communication.url}")
    private String urlClient;

    @Override
    public CommunicationMessageResponse sendMessage(UUID uuid, CommunicationMessageRequest request, Headers.Builder builder) {
        logger.info("Iniciando chamada para envio de mensagem de comunicação: {}",
                request.getData() != null ? request.getData().getChannel() : "canal não informado");

        try {
            Headers headers = generateHeaders(uuid, builder);
            
            CommunicationMessageResponse response = apigeeUtils.sendRequestToApigee(uuid,
                apigeeUtils.generateRequest(uuid,
                    GenerateRequestDTO.builder()
                        .apiUrl(urlClient)
                        .headers(headers)
                        .httpVerb(HTTP_VERB)
                        .body(request)
                        .build()),
                urlClient,
                CommunicationMessageResponse.class);
            
            return response;

        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem de comunicação: {}", e.getMessage(), e);
            
            CommunicationMessageResponse errorResponse = CommunicationMessageResponse.builder()
                .error(CommunicationErrorDTO.builder()
                    .message("Exception: " + e.getMessage())
                    .build())
                .build();
            
            return errorResponse;
        }
    }

    private Headers generateHeaders(UUID uuid, Builder builder) {
        
        return builder.build();
    }
}
