package com.omp.hub.callback.application.usecase.callback.impl;

import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.omp.hub.callback.application.service.RetryService;
import com.omp.hub.callback.application.usecase.callback.TransactionsCallbackUseCase;
import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.domain.enums.PaymentStatusEnum;
import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.ports.client.TransationsNotificationsPort;
import com.omp.hub.callback.domain.service.generate.GenerateCallbackTransactionsService;
import com.omp.hub.callback.domain.service.impl.notification.NotificationManagerService;
import com.omp.hub.callback.domain.service.impl.notification.PixEventMappingService;

import okhttp3.Headers;

@Component
public class TransactionsCallbackUseCaseImpl implements TransactionsCallbackUseCase {

    private static final Logger logger = LoggerFactory.getLogger(TransactionsCallbackUseCaseImpl.class);

    @Autowired
    private InformationPaymentPort port;

    @Autowired
    private GenerateCallbackTransactionsService service;

    @Autowired
    private TransationsNotificationsPort transactionsPort;

    @Autowired
    private ApigeeHeaderService apigeeHeaderService;

    @Autowired
    private NotificationManagerService notificationManagerService;

    @Autowired
    private PixEventMappingService pixEventMappingService;

    @Autowired
    private RetryService retryService;

    @Override
    public void sendCallback(TransactionsRequest request){

        UUID uuid = UUID.randomUUID();

        String identifier = extractTxId(request);
        String eventType = request.getEvent() != null ? request.getEvent().getType() : null;

        logger.info("TxId: {} - Processando callback de transação - EventType: {}", identifier, eventType);

        try {

            processNotificationsIfEligible(uuid, identifier, request);

            Headers.Builder builder = apigeeHeaderService.generateHeaderApigee(uuid);
            final Headers.Builder finalBuilder = builder;
            final UUID finalUuid = uuid;

            retryService.executeWithRetrySyncVoid(finalUuid, "Channel Notification",
                    () -> transactionsPort.send(finalUuid, service.generateRequest(request), finalBuilder), request);

            if (identifier != null) {
                try {
                    InformationPaymentDTO infoDTO = InformationPaymentDTO.builder()
                            .identifier(identifier)
                            .payments(Collections.singletonList(PaymentDTO.builder()
                                    .paymentStatus(PaymentStatusEnum.APPROVED)
                                    .build()))
                            .paymentStatus(PaymentStatusEnum.APPROVED)
                            .build();
                    port.sendUpdate(infoDTO);
                } catch (Exception updateException) {
                    logger.error("TxId: {} - Erro ao atualizar informação de pagamento: {}", 
                            identifier, updateException.getMessage(), updateException);
                }
            }

            logger.info("TxId: {} - Callback de transação processado com sucesso", identifier);

        } catch (BusinessException e) {
            logger.error("TxId: {} - Erro de negócio no callback de transação - Erro: {}", identifier, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("TxId: {} - Erro interno no callback de transação - Erro: {}", identifier, e.getMessage(), e);
            throw new BusinessException("Ocorreu um erro interno no callback de transação", "TRANSACTION_CALLBACK_ERROR",
                    getRootCauseMessage(e),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractTxId(TransactionsRequest request) {

        if (request.getOmpTransactionId() != null){
            return request.getOmpTransactionId();
        }
        if (request.getEvent().getTxId() != null) {
            return request.getEvent().getTxId();
        }
        else if (request.getEvent().getOriginPaymentMethod() != null &&
                        request.getEvent().getOriginPaymentMethod().getTxId() != null) {
            return request.getEvent().getOriginPaymentMethod().getTxId();
        }
        else if (request.getEvent().getPayment() != null && !request.getEvent().getPayment().isEmpty() &&
                        request.getEvent().getPayment().get(0).getPix() != null &&
                        request.getEvent().getPayment().get(0).getPix().getTxId() != null) {
            return request.getEvent().getPayment().get(0).getPix().getTxId();
        }
        else if (request.getEvent().getActivation() != null && request.getEvent().getActivation().getJourneyData() != null &&
                        request.getEvent().getActivation().getJourneyData().getTxId() != null) {
            return request.getEvent().getActivation().getJourneyData().getTxId();
        }
        return null;
    }

    private void processNotificationsIfEligible(UUID uuid, String txId, TransactionsRequest request) {
        String eventType = request.getEvent() != null ? request.getEvent().getType() : null;
        String status = request.getEvent() != null ? request.getEvent().getStatus() : null;
        String paymentMethod = request.getEvent() != null ? request.getEvent().getPaymentMethod() : null;
        String recurrenceId = request.getEvent() != null ? request.getEvent().getRecurrenceId() : null;

        if (status == null && request.getEvent() != null && request.getEvent().getOriginPaymentMethod() != null) {
            status = request.getEvent().getOriginPaymentMethod().getStatus();
        }

        if (pixEventMappingService.shouldNotify(txId, eventType)) {
            logger.info("TxId: {} - Evento elegível para notificação - EventType: {}, Status: {}, PaymentMethod: {}, RecurrenceId: {}", 
                       txId, eventType, status, paymentMethod, recurrenceId);

            PixAutomaticoEventEnum eventEnum = pixEventMappingService.mapEventTypeToEnum(txId, eventType, status, paymentMethod, recurrenceId);
            if (eventEnum != null) {
                notificationManagerService.processPixAutomaticoNotification(uuid, txId, eventEnum);
            } else {
                logger.warn("TxId: {} - Não foi possível mapear evento para notificação - EventType: {}, Status: {}", 
                           txId, eventType, status);
            }
        } else {
            logger.debug("TxId: {} - Evento não elegível para notificação - EventType: {}", txId, eventType);
        }
    }

    private String getRootCauseMessage(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : e.getMessage();
    }
}
