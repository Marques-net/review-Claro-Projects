package com.omp.hub.callback.application.usecase.callback.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.service.RetryService;
import com.omp.hub.callback.application.usecase.callback.CreditCardCallbackUseCase;
import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.domain.enums.PaymentStatusEnum;
import com.omp.hub.callback.domain.enums.RetryErrorMessageEnum;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.DataSingleDTO;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.ports.client.SapPaymentsPort;
import com.omp.hub.callback.domain.ports.client.SapRedemptionsPort;
import com.omp.hub.callback.domain.ports.client.TransationsNotificationsPort;
import com.omp.hub.callback.domain.service.generate.GenerateCallbackCardService;
import com.omp.hub.callback.domain.service.generate.GenerateSapPaymentsRequestService;
import com.omp.hub.callback.domain.service.generate.GenerateSapRedemptionsRequestService;

import okhttp3.Headers;

@Component
public class CreditCardCallbackUseCaseImpl implements CreditCardCallbackUseCase {

    @Autowired
    private InformationPaymentPort port;

    @Autowired
    private ApigeeHeaderService apigeeHeaderService;

    @Autowired
    private SapRedemptionsPort redemptionsPort;

    @Autowired
    private GenerateSapRedemptionsRequestService generateRedemptionsService;

    @Autowired
    private SapPaymentsPort paymentsPort;

    @Autowired
    private GenerateSapPaymentsRequestService generatePaymentsService;

    @Autowired
    private GenerateCallbackCardService service;

    @Autowired
    private TransationsNotificationsPort transactionsPort;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RetryService retryService;

    @Override
    public void sendCallback(CreditCardCallbackRequest request) {

        Headers.Builder builder;
        UUID uuid = UUID.randomUUID();

        if (request.getOrderId() != null && !request.getOrderId().isEmpty()) {
            InformationPaymentDTO paymentInfo = InformationPaymentDTO.builder()
                    .identifier(request.getOrderId())
                    .payments(Collections.singletonList(PaymentDTO.builder()
                            .paymentStatus(PaymentStatusEnum.APPROVED)
                            .build()))
                    .paymentStatus(PaymentStatusEnum.APPROVED)
                    .build();
            port.sendUpdate(paymentInfo);

            InformationPaymentDTO info = port.sendFindByIdentifier(request.getOrderId());

            uuid = info.getUuid();

            DataSingleDTO dto = null;
            try {
                String data = (String) info.getPayments().get(0).getJourney();
                String journeyTrim = data.trim();

                if (journeyTrim.startsWith("{")) {
                    dto = mapper.readValue(journeyTrim, DataSingleDTO.class);
                } else {
                    String unwrapped = mapper.readValue(journeyTrim, String.class);
                    dto = mapper.readValue(unwrapped, DataSingleDTO.class);
                }

            } catch (Exception e) {
                ErrorResponse error = ErrorResponse.builder()
                        .message("Erro na deserialização dos dados")
                        .details("Não foi possível converter os dados para DataSingleDTO: " + e.getMessage())
                        .errorCode("ERROR_DATA_DESERIALIZATION")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .timestamp(Instant.now())
                        .build();

                InformationPaymentDTO infoDTO = InformationPaymentDTO.builder()
                        .identifier(request.getOrderId())
                        .payments(Collections.singletonList(PaymentDTO.builder()
                                .paymentStatus(PaymentStatusEnum.ERROR)
                                .build()))
                        .paymentStatus(PaymentStatusEnum.ERROR)
                        .build();
                port.sendUpdate(infoDTO);
                throw new BusinessException(error);
            }

            if (dto != null && dto.getPayment().getSalesOrderId() != null && !dto.getPayment().getSalesOrderId().isEmpty()) {

                builder = apigeeHeaderService.generateHeaderApigee(uuid);
                
                final Headers.Builder finalBuilder = builder;
                final InformationPaymentDTO finalInfo = info;
                final UUID finalUuid = uuid;

                try {
                    retryService.executeWithRetrySyncVoid(finalUuid, "SAP Redemptions",
                            () -> redemptionsPort.send(finalUuid, generateRedemptionsService.generateRequest(finalInfo), finalBuilder), request);

                    retryService.executeWithRetrySyncVoid(finalUuid, "SAP Payments",
                            () -> paymentsPort.send(finalUuid, generatePaymentsService.generateRequest(request, finalInfo), finalBuilder), request);

                    retryService.executeWithRetrySyncVoid(finalUuid, "Channel Notification",
                            () -> transactionsPort.send(finalUuid, service.generateRequest(request), finalBuilder), request);

                } catch (Exception e) {
                    ErrorResponse error = ErrorResponse.builder()
                            .message(determineErrorMessage(e.getMessage()))
                            .details(getRootCauseMessage(e))
                            .errorCode("ERROR_MAX_RETRIES_EXCEEDED")
                            .status(HttpStatus.BAD_GATEWAY.value())
                            .timestamp(Instant.now())
                            .build();

                    InformationPaymentDTO infoDTO = InformationPaymentDTO.builder()
                            .identifier(request.getOrderId())
                            .payments(Collections.singletonList(PaymentDTO.builder()
                                    .paymentStatus(PaymentStatusEnum.ERROR)
                                    .error(error)
                                    .build()))
                            .paymentStatus(PaymentStatusEnum.ERROR)
                            .build();
                    port.sendUpdate(infoDTO);
                    
                    if (e instanceof BusinessException businessException) {
                        throw businessException;
                    }
                    throw new BusinessException(error);
                }
            } else {
                builder = apigeeHeaderService.generateHeaderApigee(uuid);
                final Headers.Builder finalBuilder = builder;
                final UUID finalUuid = uuid;

                try {
                    retryService.executeWithRetrySyncVoid(finalUuid, "Channel Notification",
                            () -> transactionsPort.send(finalUuid, service.generateRequest(request), finalBuilder), request);

                } catch (Exception e) {
                    ErrorResponse error = ErrorResponse.builder()
                            .message(determineErrorMessage(e.getMessage()))
                            .details(getRootCauseMessage(e))
                            .errorCode("ERROR_MAX_RETRIES_EXCEEDED")
                            .status(HttpStatus.BAD_GATEWAY.value())
                            .timestamp(Instant.now())
                            .build();

                    InformationPaymentDTO infoDTO = InformationPaymentDTO.builder()
                            .identifier(request.getOrderId())
                            .payments(Collections.singletonList(PaymentDTO.builder()
                                    .paymentStatus(PaymentStatusEnum.ERROR)
                                    .error(error)
                                    .build()))
                            .paymentStatus(PaymentStatusEnum.ERROR)
                            .build();
                    port.sendUpdate(infoDTO);
                    
                    if (e instanceof BusinessException businessException) {
                        throw businessException;
                    }
                    throw new BusinessException(error);
                }
            }
        } else {
            builder = apigeeHeaderService.generateHeaderApigee(uuid);
            final Headers.Builder finalBuilder = builder;
            final UUID finalUuid = uuid;

            try {
                retryService.executeWithRetrySyncVoid(finalUuid, "Channel Notification",
                        () -> transactionsPort.send(finalUuid, service.generateRequest(request), finalBuilder), request);

            } catch (Exception e) {
                ErrorResponse error = ErrorResponse.builder()
                        .message(determineErrorMessage(e.getMessage()))
                        .details(getRootCauseMessage(e))
                        .errorCode("ERROR_MAX_RETRIES_EXCEEDED")
                        .status(HttpStatus.BAD_GATEWAY.value())
                        .timestamp(Instant.now())
                        .build();
                throw new BusinessException(error);
            }
        }
    }

    private String determineErrorMessage(String errorDetails) {
        return RetryErrorMessageEnum.getMessageByOperationName(errorDetails);
    }

    private String getRootCauseMessage(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : e.getMessage();
    }
}

