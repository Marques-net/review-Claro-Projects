package com.omp.hub.callback.application.service;

import com.omp.hub.callback.application.utils.ErrorResponseMapper;
import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallbackErrorNotificationService {

    private final InformationPaymentPort informationPaymentPort;
    private final ErrorResponseMapper errorResponseMapper;

    public void notifyJourneyAboutCallbackFailure(String identifier, int maxRetries, Exception lastException) {
        try {
            log.info("Notificando Journey sobre falha definitiva no callback. Identifier: {}", identifier);

            ErrorResponse errorResponse = errorResponseMapper.mapMaxRetriesExceededError(
                identifier, maxRetries, lastException);

            PaymentDTO paymentWithError = PaymentDTO.builder()
                .error(errorResponse)
                .build();

            InformationPaymentDTO updateRequest = InformationPaymentDTO.builder()
                .identifier(identifier)
                .payments(Arrays.asList(paymentWithError))
                .build();

            informationPaymentPort.sendUpdate(updateRequest);

            log.info("Journey notificado com sucesso sobre falha no callback. Identifier: {}", identifier);

        } catch (Exception e) {
            log.error("Erro ao notificar Journey sobre falha no callback. Identifier: {}, Erro: {}", 
                identifier, e.getMessage(), e);
        }
    }
}