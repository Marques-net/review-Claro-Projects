package com.omp.hub.callback.application.utils.apigee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Component
public class ApigeeUtils {

    private static final Logger logger = LoggerFactory.getLogger(ApigeeUtils.class);

    @Autowired
    private ObjectMapper mapper;

    @Value("${apigee.url}")
    private String apigeeUrl;

    @Autowired
    private OkHttpClient okHttpClient;

    public void convertApigeeErrorToBusinessError(Response response, String responseBody, String urlClient) {

        try {
            if (!response.isSuccessful() && responseBody != null && !responseBody.isEmpty()) {

                ApigeeResponse<?> errorResp = mapper.readValue(responseBody, ApigeeResponse.class);

                ErrorResponse error = ErrorResponse.builder()
                        .message(errorResp.getError().getMessage())
                        .details(errorResp.getError().getDetailedMessage())
                        .errorCode(errorResp.getError().getErrorCode())
                        .status(errorResp.getError().getHttpCode())
                        .timestamp(Instant.now())
                        .build();

                throw new BusinessException(error);
            }
            else if (String.valueOf(response.code()).matches("^(4|5)\\d{2}$")) {
                ErrorResponse error = ErrorResponse.builder()
                        .message("Erro ao executar chamada: " + urlClient)
                        .details("Recebemos um erro " + response.code() + " porém não recebemos nenhum body no response")
                        .errorCode("ERROR_NO_BODY_RESPONSE")
                        .status(response.code())
                        .timestamp(Instant.now())
                        .build();

                throw new BusinessException(error);
            }
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    public Request generateRequest(UUID uuid, GenerateRequestDTO<?> dto) {
        try {
            logger.info("TxId: " + uuid.toString() + " - Inicio chamada para " + dto.getApiUrl() + ":");

            RequestBody body;
            if (dto.getBody() instanceof RequestBody)
                body = (RequestBody) dto.getBody();
            else
                body = RequestBody.create(dto.getBody() != null ? mapper.writeValueAsString(dto.getBody()) : "", null);

            logger.info("TxId: " + uuid.toString() + " - HEADERS: " + (dto.getHeaders() != null ? dto.getHeaders().toString() : "null"));
            logger.info("TxId: " + uuid.toString() + " - BODY: " + mapper.writeValueAsString(dto.getBody()));

            if (dto.getHeaders() == null)
                dto.setHeaders(Headers.of("Content-Type", "application/json"));

            if (dto.getHttpVerb().equals("POST"))
                return new Request.Builder().url(apigeeUrl + dto.getApiUrl()).post(body).headers(dto.getHeaders())
                        .build();
            if (dto.getHttpVerb().equals("GET"))
                return new Request.Builder().url(apigeeUrl + dto.getApiUrl()).get().headers(dto.getHeaders()).build();
            if (dto.getHttpVerb().equals("PUT"))
                return new Request.Builder().url(apigeeUrl + dto.getApiUrl()).put(body).headers(dto.getHeaders())
                        .build();
            if (dto.getHttpVerb().equals("PATCH"))
                return new Request.Builder().url(apigeeUrl + dto.getApiUrl()).patch(body).headers(dto.getHeaders())
                        .build();
            if (dto.getHttpVerb().equals("DELETE"))
                return new Request.Builder().url(apigeeUrl + dto.getApiUrl()).get().headers(dto.getHeaders()).build();
            return null;
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    public <T> T sendRequestToApigee(UUID uuid, Request request, String urlClient, Class<T> nameClass) {
        try {
            if (Objects.nonNull(request)) {
                Response response = okHttpClient.newCall(request).execute();
                String responseStr = response.body() != null ? response.body().string() : "";
                logger.info("TxId: " + uuid.toString() + " - RESPONSE: " + responseStr);
                convertApigeeErrorToBusinessError(response, responseStr, urlClient);
                logger.info("TxId: " + uuid.toString() + " - Fim chamada para " + urlClient + ":");

                if (nameClass != null) {
                    // Se não há conteúdo na resposta mas esperamos um objeto, retorna null
                    if (responseStr.trim().isEmpty()) {
                        logger.warn("TxId: " + uuid.toString()
                                + " - Resposta vazia recebida, mas esperava-se um objeto do tipo: "
                                + nameClass.getSimpleName());
                        return null;
                    }
                    return mapper.readValue(responseStr, nameClass);
                } else {
                    return null;
                }
            }
            return null;
        } catch (BusinessException e) {
            throw e;
        } catch (ConnectException e) {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Erro de conexão")
                    .details(e.getMessage())
                    .status(HttpStatus.BAD_GATEWAY.value())
                    .errorCode("ERROR_BAD_GATEWAY")
                    .timestamp(Instant.now())
                    .build();
            throw new BusinessException(error);
        } catch (SocketTimeoutException e) {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Erro de conexão")
                    .details(e.getMessage())
                    .status(HttpStatus.REQUEST_TIMEOUT.value())
                    .errorCode("ERROR_CONNECTION_TIMEOUT")
                    .timestamp(Instant.now())
                    .build();
            throw new BusinessException(error);
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }
}
