package com.omp.hub.callback.infrastructure.client;

import java.util.UUID;

import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersResponse;
import com.omp.hub.callback.domain.ports.client.CustomerContractsSubscribersPort;

import lombok.RequiredArgsConstructor;
import okhttp3.Headers;

@Component
@RequiredArgsConstructor
public class CustomerContractsSubscribersClient implements CustomerContractsSubscribersPort {
    private static final Logger logger = LoggerFactory.getLogger(CustomerContractsSubscribersClient.class);
    private static final String HTTP_VERB = "GET";

    @Value("${client.domains.customercontractssubscribers.url}")
    private String urlClient;

    @Value("${client.domains.customercontractssubscribers.target}")
    private String targetValue;

    private final ApigeeUtils apigeeUtils;
    private final ApigeeHeaderService apigeeHeaderService;

    @Override
    public CustomerContractsSubscribersResponse send(UUID uuid, String documento, ExtractedCustomerDataDTO customerData) {
        logger.info("Consultando dados do cliente via API Customer Contracts - documento: {}", documento);
        
        try {
            String docType = documento.length() <= 11 ? "cpf" : "cnpj";
            String urlWithParams = urlClient;
            
            return apigeeUtils.sendRequestToApigee(
                uuid,
                apigeeUtils.generateRequest(
                    uuid,
                    GenerateRequestDTO.builder()
                        .apiUrl(urlWithParams)
                        .headers(this.generateHeaders(uuid, documento, customerData))
                        .httpVerb(HTTP_VERB)
                        .body(null)
                        .build()),
                urlWithParams,
                CustomerContractsSubscribersResponse.class
            );
        } catch (Exception e) {
            logger.error("Erro ao consultar dados do cliente na API Customer Contracts - documento: {}, Erro: {}", 
                    documento, e.getMessage(), e);
            return null;
        }
    }

    private Headers generateHeaders(UUID uuid, String documento, ExtractedCustomerDataDTO customerData) {
        String target = targetValue != null ? targetValue : "test03";
        logger.debug("TxId: {} - Gerando headers para billing details - target: {}", uuid, target);

        Headers.Builder builder = apigeeHeaderService.generateHeaderApigee(uuid);
        builder.add("Accept", "application/json");
        builder.add("x-querystring", "customerAccountId=" + customerData.getOperatorCode() + customerData.getContractNumber());
        builder.add("x-target", target);

        return builder.build();
    }
}