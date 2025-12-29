package com.omp.hub.callback.infrastructure.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsResponse;
import com.omp.hub.callback.domain.ports.client.SapBillingPaymentsPort;

import lombok.RequiredArgsConstructor;
import okhttp3.Headers;

@Component
@RequiredArgsConstructor
public class SapBillingPaymentsClient implements SapBillingPaymentsPort {
    private static final String HTTP_VERB = "POST";

    @Value("${client.sap.billing.payments.url}")
    private String urlClient;

    @Autowired
    private ApigeeUtils apigeeUtils;

    public SapBillingPaymentsResponse send(UUID uuid, SapBillingPaymentsRequest request, Headers.Builder builder) {

        return apigeeUtils.sendRequestToApigee(
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
            SapBillingPaymentsResponse.class);
    }

    private Headers generateHeader(Headers.Builder builder) {
        builder.add("Accept", "application/json");
        Headers headers = builder.build();
        return headers;
    }
}
