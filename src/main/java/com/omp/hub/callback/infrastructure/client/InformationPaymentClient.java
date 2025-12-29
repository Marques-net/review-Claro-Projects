package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.application.utils.apigee.RequestUtils;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InformationPaymentClient implements InformationPaymentPort {
    private static final Logger logger = LoggerFactory.getLogger(InformationPaymentClient.class);
    private static final String HTTP_VERB_GET = "GET";
    private static final String HTTP_VERB_POST = "POST";
    private static final String HTTP_VERB_PUT = "PUT";

    @Value("${omp.journeys.url}")
    private String urlHost;

    @Value("${client.information.payment.url}")
    private String urlClient;

    @Autowired
    private RequestUtils requestUtils;

    public InformationPaymentDTO sendCreate(InformationPaymentDTO request) {

        return requestUtils.sendRequest(
            requestUtils.generateRequest(
                GenerateRequestDTO.builder()
                    .apiUrl(urlHost + urlClient)
                    .headers(null)
                    .httpVerb(HTTP_VERB_POST)
                    .body(request)
                    .build()),
            urlClient,
            InformationPaymentDTO.class
        );
    }

    public InformationPaymentDTO sendUpdate(InformationPaymentDTO request) {

        return requestUtils.sendRequest(
            requestUtils.generateRequest(
                GenerateRequestDTO.builder()
                    .apiUrl(urlHost + urlClient)
                    .headers(null)
                    .httpVerb(HTTP_VERB_PUT)
                    .body(request)
                    .build()),
            urlClient,
            InformationPaymentDTO.class
        );
    }

    public InformationPaymentDTO sendFindByIdentifier(String identifier) {

        return requestUtils.sendRequest(
            requestUtils.generateRequest(
                GenerateRequestDTO.builder()
                    .apiUrl(urlHost + urlClient + "/" + identifier)
                    .headers(null)
                    .httpVerb(HTTP_VERB_GET)
                    .body(null)
                    .build()),
            urlClient,
            InformationPaymentDTO.class
        );
    }

    public InformationPaymentDTO updatePaymentInList(String identifier, String paymentType, InformationPaymentDTO request) {

        return requestUtils.sendRequest(
            requestUtils.generateRequest(
                GenerateRequestDTO.builder()
                    .apiUrl(urlHost + urlClient + "/" + identifier + "/payments/" + paymentType)
                    .headers(null)
                    .httpVerb(HTTP_VERB_PUT)
                    .body(request)
                    .build()),
            urlClient,
            InformationPaymentDTO.class
        );
    }

}