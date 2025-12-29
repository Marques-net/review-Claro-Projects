package com.omp.hub.callback.application.usecase.callback.impl;

import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.omp.hub.callback.application.usecase.callback.PixCallbackUseCase;
import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.domain.enums.PaymentStatusEnum;
import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.ports.client.TransationsNotificationsPort;
import com.omp.hub.callback.domain.service.generate.GenerateCallbackPixService;
import com.omp.hub.callback.domain.service.impl.notification.NotificationManagerService;
import com.omp.hub.callback.domain.service.impl.notification.PixEventMappingService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PixCallbackUseCaseImpl implements PixCallbackUseCase {

    private static final Logger logger = LoggerFactory.getLogger(PixCallbackUseCaseImpl.class);

    private final InformationPaymentPort port;
    private final GenerateCallbackPixService service;
    private final TransationsNotificationsPort transactionsPort;
    private final ApigeeHeaderService apigeeHeaderService;
    private final PixEventMappingService pixEventMappingService;
    private final NotificationManagerService notificationManagerService;

    private String txId;

    @Override
    public void sendCallback(PixCallbackRequest request) {

        this.txId = request.getOmpTransactionId() != null ? request.getOmpTransactionId() : request.getTxId();

        try {
            UUID uuid = UUID.randomUUID();
            String gevenue = "gevenue";

            if (request.getService() != null && !request.getService().isEmpty() && request.getService().toLowerCase().contains(gevenue)) {
                sendHubNotification(uuid, request);
            }
            else {
                processPixAutomaticoNotification(uuid, request);
            }

            if (request.getOmpTransactionId() != null && !request.getOmpTransactionId().isEmpty()) {
                InformationPaymentDTO paymentInfo = InformationPaymentDTO.builder()
                    .identifier(request.getOmpTransactionId())
                    .payments(Collections.singletonList(PaymentDTO.builder()
                        .paymentStatus(PaymentStatusEnum.APPROVED)
                        .build()))
                    .paymentStatus(PaymentStatusEnum.APPROVED)
                    .build();
                port.sendUpdate(paymentInfo);
            }
            else if (request.getTxId() != null && !request.getTxId().isEmpty()) {
                InformationPaymentDTO paymentInfo = InformationPaymentDTO.builder()
                    .identifier(request.getTxId())
                    .payments(Collections.singletonList(PaymentDTO.builder()
                        .paymentStatus(PaymentStatusEnum.APPROVED)
                        .build()))
                    .paymentStatus(PaymentStatusEnum.APPROVED)
                    .build();
                port.sendUpdate(paymentInfo);
            }

        } catch (BusinessException e) {
            logger.error("Erro de negócio no callback PIX - TxId: {}, Erro: {}",
                    this.txId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Erro interno no callback PIX - TxId: {}, Erro: {}",
                    this.txId, e.getMessage(), e);

            throw new BusinessException("Ocorreu um erro interno no callback PIX", "PIX_CALLBACK_ERROR",
                    e.getMessage() + "\n" + e.getCause() + "\n" + e.getClass(),
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void processPixAutomaticoNotification(UUID uuid, PixCallbackRequest request) {

        logger.info("Notificação HUB enviada com sucesso para evento PIX Automático - txId: {}", this.txId);

        if (!pixEventMappingService.isPixAutomaticoEvent(this.txId, request.getPaymentType())) {
            sendHubNotification(uuid, request);
            return;
        }

        PixAutomaticoEventEnum eventType = pixEventMappingService.mapPaymentTypeToEvent(this.txId, request.getPaymentType());
        if (eventType == null) {
            logger.warn("TxId: {} - Não foi possível mapear evento PIX Automático para: {}", this.txId, request.getPaymentType());

            throw new BusinessException("Ocorreu um erro interno no callback PIX", "PIX_CALLBACK_ERROR",
                    "Não foi possível mapear evento PIX Automático para o PaymentType: " + request.getPaymentType(),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        logger.info("Processando notificação PIX Automático - txId: {}, evento: {}",
                this.txId, eventType.getDescription());

        logger.info("Iniciando envio de notificação HUB para evento PIX Automático - txId: {}, evento: {}",
                this.txId, eventType.getDescription());

        sendHubNotification(uuid, request);

        logger.info("Notificação HUB enviada com sucesso para evento PIX Automático - txId: {}, evento: {}",
                this.txId, eventType.getDescription());

        logger.info("Iniciando envio de notificação RTDM para evento PIX Automático - txId: {}, evento: {}",
                this.txId, eventType.getDescription());

        boolean criteriosAtendidos = sendRtdmNotification(uuid, request, eventType);
        
        if (criteriosAtendidos) {
            logger.info("Critérios atendidos - Notificação RTDM enviada com sucesso para evento PIX Automático - txId: {}, evento: {}",
                    this.txId, eventType.getDescription());
        } else {
            logger.info("Critérios NÃO atendidos - Notificação RTDM não enviada para evento PIX Automático - txId: {}, evento: {}",
                    this.txId, eventType.getDescription());
        }
    }

    private boolean sendRtdmNotification(UUID uuid, PixCallbackRequest request, PixAutomaticoEventEnum eventType) {
        logger.info("Processando notificação RTDM - txId: {}, evento: {}",
                this.txId, eventType.getDescription());

        return notificationManagerService.processPixAutomaticoNotification(uuid, this.txId, eventType);
    }


    private void sendHubNotification(UUID uuid, PixCallbackRequest request) {
        logger.info("Enviando callback PIX - TxId: {}", this.txId);

        transactionsPort.send(uuid,
                service.generateRequest(request),
                apigeeHeaderService.generateHeaderApigee(uuid));

        logger.info("Callback PIX enviado com sucesso - TxId: {}", this.txId);
    }
}
