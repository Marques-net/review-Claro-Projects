package com.omp.hub.callback.infrastructure.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsResponse;
import com.omp.hub.callback.domain.ports.client.MobileBillingDetailsPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;

@Slf4j
@Component
@RequiredArgsConstructor
public class MobileBillingDetailsClient implements MobileBillingDetailsPort {
    private static final String HTTP_VERB = "GET";

    @Value("${client.mobile.customers.billing.details.url}")
    private String urlClient;

    @Value("${client.mobile.customers.billing.details.target}")
    private String targetValue;

    private final ApigeeUtils apigeeUtils;
    private final ApigeeHeaderService apigeeHeaderService;

    @Override
    public MobileBillingDetailsResponse getCustomerBillingDetailsByMobileBan(UUID uuid, String mobileBan) {
        log.info("TxId: {} - Consultando billing details do cliente móvel - mobileBan: {}", uuid, mobileBan);
        
        log.debug("TxId: {} - URL: {}, target: {}", uuid, urlClient, 
                targetValue != null ? targetValue : "test03");
        
        try {
            MobileBillingDetailsResponse response = apigeeUtils.sendRequestToApigee(
                uuid,
                apigeeUtils.generateRequest(
                    uuid,
                    GenerateRequestDTO.builder()
                        .apiUrl(urlClient)
                        .headers(this.generateHeaders(uuid, mobileBan))
                        .httpVerb(HTTP_VERB)
                        .body(null)
                        .build()),
                urlClient,
                MobileBillingDetailsResponse.class
            );
            
            log.info("TxId: {} - Resposta billing details recebida - sucesso: {}", 
                    uuid, response != null && response.getData() != null);
            
            return response;
        } catch (Exception e) {
            log.error("TxId: {} - Erro ao consultar billing details do cliente móvel - mobileBan: {}, erro: {}", 
                    uuid, mobileBan, e.getMessage(), e);
            return null;
        }
    }

    private Headers generateHeaders(UUID uuid, String mobileBan) {
        String target = targetValue != null ? targetValue : "test03";
        log.debug("TxId: {} - Gerando headers para billing details - mobileBan: {}, target: {}", uuid, mobileBan, target);
        
        Headers.Builder builder = apigeeHeaderService.generateHeaderApigee(uuid);
        builder.add("Accept", "application/json");
        builder.add("x-querystring", "mobileBan=" + mobileBan);
        builder.add("x-target", target);
        
        return builder.build();
    }
}