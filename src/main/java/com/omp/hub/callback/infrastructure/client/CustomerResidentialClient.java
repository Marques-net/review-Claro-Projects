package com.omp.hub.callback.infrastructure.client;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.customer.residential.CustomerResidentialResponse;
import com.omp.hub.callback.domain.ports.client.CustomerResidentialPort;

import lombok.RequiredArgsConstructor;
import okhttp3.Headers;

@Component
@RequiredArgsConstructor
public class CustomerResidentialClient implements CustomerResidentialPort {

    private static final Logger logger = LoggerFactory.getLogger(CustomerResidentialClient.class);
    private static final String HTTP_VERB = "GET";

    private final ApigeeUtils apigeeUtils;
    private final ApigeeHeaderService apigeeHeaderService;

    @Value("${client.customer.residential.url}")
    private String urlClient;

    @Override
    public CustomerResidentialResponse getCustomerContractsByPhoneNumber(UUID uuid, String phoneNumber) {
        logger.info("Iniciando consulta de contratos residenciais por phoneNumber: {}", phoneNumber);

        try {
            return apigeeUtils.sendRequestToApigee(uuid,
                    apigeeUtils.generateRequest(uuid,
                            GenerateRequestDTO.builder()
                                    .apiUrl(urlClient)
                                    .headers(generateHeaders(uuid, phoneNumber, "phoneNumber"))
                                    .httpVerb(HTTP_VERB)
                                    .build()),
                    urlClient,
                    CustomerResidentialResponse.class);

        } catch (Exception e) {
            logger.error("Erro ao consultar contratos residenciais por phoneNumber {}: {}",
                    phoneNumber, e.getMessage(), e);
            throw e;
        }
    }

     @Override
    public CustomerResidentialResponse getCustomerContractsByDocument(UUID uuid, String document) {
        logger.info("Iniciando consulta de contratos residenciais por document: {}", document);

        try {
            return apigeeUtils.sendRequestToApigee(uuid,
                    apigeeUtils.generateRequest(uuid,
                            GenerateRequestDTO.builder()
                                    .apiUrl(urlClient)
                                    .headers(generateHeaders(uuid, document, "document"))
                                    .httpVerb(HTTP_VERB)
                                    .build()),
                    urlClient,
                    CustomerResidentialResponse.class);

        } catch (Exception e) {
            logger.error("Erro ao consultar contratos residenciais por document {}: {}",
                    document, e.getMessage(), e);
            throw e;
        }
    }


    private Headers generateHeaders(UUID uuid,String value, String queryType) {
        Headers.Builder builder = apigeeHeaderService.generateHeaderApigee(uuid);
        builder.add("Accept", "application/json");
        builder.add("Content-Type", "application/json");
        builder.add("X-QueryString", queryType + "=" + value);
        return builder.build();
    }
}
