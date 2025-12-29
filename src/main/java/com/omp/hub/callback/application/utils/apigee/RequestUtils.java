package com.omp.hub.callback.application.utils.apigee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.Objects;

@Component
public class RequestUtils {

    private static final Logger logger = LoggerFactory.getLogger(RequestUtils.class);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private OkHttpClient okHttpClient;

    public Request generateRequest(GenerateRequestDTO<?> dto) {
        try {
            logger.info("Inicio chamada para " + dto.getApiUrl() + ":");
            RequestBody body = RequestBody.create(mapper.writeValueAsString(dto.getBody()), null);

            if (dto.getHeaders() == null)
                dto.setHeaders(Headers.of("Content-Type", "application/json"));

            if (dto.getHttpVerb().equals("POST"))
                return new Request.Builder().url(dto.getApiUrl()).post(body).headers(dto.getHeaders()).build();
            if (dto.getHttpVerb().equals("GET"))
                return new Request.Builder().url(dto.getApiUrl()).get().headers(dto.getHeaders()).build();
            if (dto.getHttpVerb().equals("PUT"))
                return new Request.Builder().url(dto.getApiUrl()).put(body).headers(dto.getHeaders()).build();
            if (dto.getHttpVerb().equals("PATCH"))
                return new Request.Builder().url(dto.getApiUrl()).patch(body).headers(dto.getHeaders()).build();
            if (dto.getHttpVerb().equals("DELETE"))
                return new Request.Builder().url(dto.getApiUrl()).delete().headers(dto.getHeaders()).build();
            return null;
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    public <T> T sendRequest(Request request, String urlClient, Class<T> nameClass) {
        try {
            if (Objects.nonNull(request)) {
                Response response = okHttpClient.newCall(request).execute();
                String responseStr = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()){
                    ErrorResponse error = mapper.readValue(responseStr, ErrorResponse.class);
                    throw new BusinessException(error);
                }

                logger.info("RESPONSE: " + responseStr);
                logger.info("Fim chamada para " + urlClient + ":");
                return !responseStr.isEmpty() ? mapper.readValue(responseStr, nameClass) : null;
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
