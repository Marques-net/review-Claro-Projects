package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsResponse;
import com.omp.hub.callback.domain.ports.client.SapPaymentsPort;
import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SapPaymentsClient implements SapPaymentsPort {
    private static final Logger logger = LoggerFactory.getLogger(SapPaymentsClient.class);
    private static final String HTTP_VERB = "POST";

    @Value("${client.sap.payments.url}")
    private String urlClient;

    @Autowired
    private ApigeeUtils apigeeUtils;


    public SapPaymentsResponse send(UUID uuid, SapPaymentsRequest request, Headers.Builder builder) {

        return  apigeeUtils.sendRequestToApigee(
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
                SapPaymentsResponse.class
        );
    }

    private Headers generateHeader(Headers.Builder builder) {
        builder.add("Accept", "application/json");
        return builder.build();
    }
}