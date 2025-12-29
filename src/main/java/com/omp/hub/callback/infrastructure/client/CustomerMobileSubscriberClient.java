package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.customer.CustomerMobileResponse;
import com.omp.hub.callback.domain.ports.client.CustomerMobilePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerMobileSubscriberClient implements CustomerMobilePort {
    private static final String HTTP_VERB = "GET";

    @Value("${client.mobile.subscribers.url}")
    private String urlClient;

    @Value("${client.mobile.subscribers.target}")
    private String targetValue;

    @Autowired
    private final ApigeeUtils apigeeUtils;
    private final ApigeeHeaderService apigeeHeaderService;


    public CustomerMobileResponse send(UUID uuid, String document, String status) {
        log.info("TxId: {} - Consultando subscribers móvel",uuid);
        
        String urlWithQuery = urlClient + "?status=" + status + "&pageNumber=1&pageSize=1";
        String documentType = document != null && document.length() == 11 ? "cpf" : "cnpj";
        
        log.debug("TxId: {} - URL completa: {}, tipo documento: {}, target: {}", uuid, urlWithQuery, documentType, 
                targetValue != null ? targetValue : "test03");

        try {
            CustomerMobileResponse response = apigeeUtils.sendRequestToApigee(
                        uuid,
                        apigeeUtils.generateRequest(
                            uuid,
                            GenerateRequestDTO.builder()
                                .apiUrl(urlWithQuery)
                                .headers(this.generateHeaders(uuid, document, documentType))
                                .httpVerb(HTTP_VERB)
                                .build()),
                        urlWithQuery,
                    CustomerMobileResponse.class
            );
            
            log.info("TxId: {} - Resposta subscribers móvel recebida - items encontrados: {}", 
                    uuid, response != null && response.getData() != null && response.getData().getSubscribers() != null ? 
                    response.getData().getSubscribers().size() : 0);
            
            return response;
        } catch (Exception ex) {
            log.error("TxId: {} - Erro ao consultar subscribers móvel - erro: {}", uuid, ex.getMessage(), ex);
            throw ex;
        }
    }

    private Headers generateHeaders(UUID uuid, String value, String queryType) {
        String target = targetValue != null ? targetValue : "test06"; 
        log.debug("TxId: {} - Gerando headers para consulta - queryType: {}, target: {}", uuid, queryType, target);
        
        Headers.Builder builder = apigeeHeaderService.generateHeaderApigee(uuid);
        builder.add("Accept", "application/json");
        builder.add("Content-Type", "application/json");
        builder.add("X-QueryString", queryType + "=" + value);
        builder.add("x-target", target);
        
        return builder.build();
    }
}