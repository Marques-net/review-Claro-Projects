package com.omp.hub.callback.infrastructure.client;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.application.utils.apigee.ApigeeUtils;
import com.omp.hub.callback.application.utils.apigee.GenerateRequestDTO;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.SapRedemptionsRequest;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.SapRedemptionsResponse;
import com.omp.hub.callback.domain.ports.client.SapRedemptionsPort;
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
public class SapRedemptionsClient implements SapRedemptionsPort {
    private static final Logger logger = LoggerFactory.getLogger(SapRedemptionsClient.class);
    private static final String HTTP_VERB = "POST";
    @Value("${client.sap.redemptions.url}")
    private String urlClient;

    @Autowired
    private ApigeeUtils apigeeUtils;

    @Autowired
    private ApigeeHeaderService apigeeHeaderService;

    public SapRedemptionsResponse send(UUID uuid, SapRedemptionsRequest request, Headers.Builder builder) {

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
            SapRedemptionsResponse.class);
    }

    private Headers generateHeader(Headers.Builder builder) {
        builder.add("Accept", "application/json");
        Headers headers = builder.build();
        return headers;
    }
}