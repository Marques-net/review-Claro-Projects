package com.omp.hub.callback.infrastructure.client;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import com.omp.hub.callback.domain.ports.client.TransationsNotificationsPort;

import lombok.RequiredArgsConstructor;
import okhttp3.Headers;

@Component
@RequiredArgsConstructor
public class TransationsNotificationsClient implements TransationsNotificationsPort {
    private static final Logger logger = LoggerFactory.getLogger(TransationsNotificationsClient.class);
    private static final String HTTP_VERB = "POST";

    @Value("${client.transaction.notification.url}")
    private String urlClient;

    @Autowired
    private ApigeeUtils apigeeUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void send(UUID uuid, OmphubTransactionNotificationRequest request, Headers.Builder builder){
        logger.info("UUID: {} - Enviando notificação de transação para URL: {}", uuid, urlClient);
        
        if (logger.isDebugEnabled()) {
            try {
                String jsonPayload = objectMapper.writeValueAsString(request);
                logger.debug("UUID: {} - Payload: {}", uuid, jsonPayload);
            } catch (Exception e) {
                logger.debug("UUID: {} - Não foi possível serializar payload para log", uuid);
            }
        }

        apigeeUtils.sendRequestToApigee(
            uuid,
            apigeeUtils.generateRequest(
                uuid,
                GenerateRequestDTO.builder()
                    .apiUrl(urlClient)
                    .headers(this.generateHeader(builder))
                    .httpVerb(HTTP_VERB)
                    .body(request)
                    .build()),
            urlClient,
            null
        );
    }

    private Headers generateHeader(Headers.Builder builder) {
        builder.add("Accept", "application/json");
        return builder.build();
    }
}
