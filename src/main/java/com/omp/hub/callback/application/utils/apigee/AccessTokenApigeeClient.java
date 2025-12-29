package com.omp.hub.callback.application.utils.apigee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.utils.apigee.dto.ApigeeTokenDTO;
import lombok.RequiredArgsConstructor;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccessTokenApigeeClient implements AccessTokenPort {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenApigeeClient.class);
    private static final String HTTP_VERB = "POST";

    @Value("${apigee.url}")
    private String apiUrl;

    @Value("${apigee.credentials.basic}")
    private String apigeeBasic;

    @Value("${client.apigee.token.url}")
    private String urlClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ApigeeUtils apigeeUtils;

    public ApigeeTokenDTO getAccessToken(UUID uuid) {

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials").build();

        Map<String, String> map = new HashMap<>();
        map.put("x-client-auth", apigeeBasic);

        return  apigeeUtils.sendRequestToApigee(
                    uuid,
                    apigeeUtils.generateRequest(
                            uuid,
                        GenerateRequestDTO.builder()
                            .apiUrl(urlClient)
                            .headers(Headers.of(map))
                            .httpVerb(HTTP_VERB)
                            .body(body)
                            .build()),
                    urlClient,
                    ApigeeTokenDTO.class
                );
    }
}