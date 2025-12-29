package com.omp.hub.callback.application.usecase.callback.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.service.SapIntegrationService;
import com.omp.hub.callback.application.usecase.callback.TefWebCallbackUseCase;
import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.domain.enums.PaymentStatusEnum;
import com.omp.hub.callback.domain.enums.PaymentTypeEnum;
import com.omp.hub.callback.domain.enums.RetryErrorMessageEnum;
import com.omp.hub.callback.domain.enums.TefWebStatusEnum;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.SalesDTO;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TransactionsDTO;
import com.omp.hub.callback.domain.model.dto.journey.UpdateCancellationStatusDTO;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.DataSingleDTO;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import okhttp3.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class TefWebCallbackUseCaseImpl implements TefWebCallbackUseCase {

    private static final Logger logger = LoggerFactory.getLogger(TefWebCallbackUseCaseImpl.class);

    @Autowired
    private InformationPaymentPort port;

    @Autowired
    private SapIntegrationService sapIntegrationService;

    @Autowired
    private ApigeeHeaderService apigeeHeaderService;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void sendCallback(TefWebCallbackRequest request) {
        UUID uuid = UUID.randomUUID();
        String identifier = null;
        String originalTransactionId = null;

        try {
            Headers.Builder builder;

            if (request.getSales() != null && !request.getSales().isEmpty()
                    && request.getSales().get(0).getOrder() != null && request.getSales().get(0).getOrder().getOmpTransactionId() != null) {
                originalTransactionId = request.getSales().get(0).getOrder().getOmpTransactionId();
                identifier = sapIntegrationService.extractBaseTransactionOrderId(originalTransactionId);
            }

            if (identifier != null) {
                InformationPaymentDTO info = port.sendFindByIdentifier(identifier);
                logger.info("TxId: " + identifier + " - GET INFO: " + mapper.writeValueAsString(info));

                if (info != null && info.getUuid() != null) {
                    uuid = info.getUuid();
                }

                String journey = null;
                if (info != null && info.getPayments() != null) {
                    for (PaymentDTO p : info.getPayments()) {
                        if (p.getJourney() != null) {
                            journey = (String) p.getJourney();
                            break;
                        }
                    }
                }

                DataSingleDTO dto = null;

                if (journey != null && !journey.isBlank()) {
                    String journeyTrim = journey.trim();
                    if (journeyTrim.startsWith("{")) {
                        dto = mapper.readValue(journeyTrim, DataSingleDTO.class);
                    } else {
                        String unwrapped = mapper.readValue(journeyTrim, String.class);
                        if (unwrapped != null) {
                            dto = mapper.readValue(unwrapped, DataSingleDTO.class);
                        }
                    }
                } else {
                    logger.warn("TxId: {} - Journey está nulo ou vazio, continuando sem dados de jornada", identifier);
                }

                builder = apigeeHeaderService.generateHeaderApigee(uuid);

                final Headers.Builder finalBuilder = builder;
                final InformationPaymentDTO finalInfo = info;
                final UUID finalUuid = uuid;
                final String finalIdentifier = identifier;
                final DataSingleDTO finalDto = dto;
                final String finalOriginalTransactionId = originalTransactionId;

                // Detectar múltiplas transações TefWeb no callback
                boolean hasMultipleTransactions = false;
                if (request.getSales() != null && !request.getSales().isEmpty() 
                        && request.getSales().get(0).getTransactions() != null) {
                    int transactionCount = request.getSales().get(0).getTransactions().size();
                    hasMultipleTransactions = transactionCount > 1;
                    
                    if (hasMultipleTransactions) {
                        logger.info("TxId: {} - Detectadas {} transações TefWeb no mesmo Sale. Setando multiplePayment=true", 
                            identifier, transactionCount);
                        finalInfo.setMultiplePayment(true);
                    }
                }

                logger.info("TxId: {} - MultiplePayment flag: {} (transactions: {})", 
                    identifier, finalInfo.getMultiplePayment(), 
                    hasMultipleTransactions ? request.getSales().get(0).getTransactions().size() : 1);

                // Detectar se é callback de cancelamento
                if (isCancellationCallback(request)) {
                    logger.info("TxId: {} - Callback de CANCELAMENTO TEFWeb detectado", identifier);
                    processCancellationCallback(request, finalInfo, finalUuid, finalIdentifier, finalBuilder);
                } else {
                    // Fluxo existente para pagamento normal
                    if (Boolean.TRUE.equals(finalInfo.getMultiplePayment())) {
                        processMultiplePayment(request, finalInfo, finalUuid, finalIdentifier, finalDto, finalBuilder, finalOriginalTransactionId);
                    } else {
                        processSinglePayment(request, finalInfo, finalUuid, finalIdentifier, finalDto, finalBuilder);
                    }
                }
            } else {
                builder = apigeeHeaderService.generateHeaderApigee(uuid);
                final Headers.Builder finalBuilder = builder;
                final UUID finalUuid = uuid;

                try {
                    sapIntegrationService.sendChannelNotification(finalUuid, request, finalBuilder, null);
                } catch (Exception e) {
                    ErrorResponse error = ErrorResponse.builder()
                            .message(determineErrorMessage(e.getMessage()))
                            .details(getRootCauseMessage(e))
                            .errorCode("ERROR_MAX_RETRIES_EXCEEDED")
                            .timestamp(Instant.now())
                            .status(HttpStatus.BAD_GATEWAY.value())
                            .build();
                    if (e.getCause() instanceof BusinessException) {
                        throw (BusinessException) e.getCause();
                    }
                    throw new BusinessException(error);
                }
            }
        } catch (JsonProcessingException e) {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Erro de conversao")
                    .details(getRootCauseMessage(e))
                    .errorCode("ERROR_CONVERT_DATA")
                    .status(HttpStatus.BAD_GATEWAY.value())
                    .build();
            throw new BusinessException(error);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    private void processMultiplePayment(TefWebCallbackRequest request, InformationPaymentDTO info,
            UUID uuid, String identifier, DataSingleDTO dto, Headers.Builder headerBuilder, String originalTransactionId) {

        logger.info("TxId: {} - Processando pagamento misto TEFWEB", uuid);

        sapIntegrationService.updatePaymentStatusApproved(identifier, PaymentTypeEnum.TEFWEB, originalTransactionId, request);

        InformationPaymentDTO updatedInfo = port.sendFindByIdentifier(identifier);

        if (sapIntegrationService.shouldSendToSap(updatedInfo, PaymentTypeEnum.TEFWEB)) {
            logger.info("TxId: {} - Todos os pagamentos aprovados, enviando para SAP com ID original", uuid);
            
            // Busca novamente as informações para garantir que CASH foi aprovado automaticamente
            InformationPaymentDTO finalInfo = port.sendFindByIdentifier(identifier);
            
            processSinglePayment(request, finalInfo, uuid, identifier, dto, headerBuilder);
        } else {
            logger.info("TxId: {} - Aguardando demais pagamentos serem aprovados", uuid);
        }
    }

    private void processSinglePayment(TefWebCallbackRequest request, InformationPaymentDTO info,
            UUID uuid, String identifier, DataSingleDTO dto, Headers.Builder headerBuilder) {

        if (sapIntegrationService.hasSalesOrderId(dto, identifier)) {
            try {
                sapIntegrationService.sendToSapRedemptionsAndPayments(uuid, request, info, headerBuilder, dto);
                // sapIntegrationService.sendChannelNotification(uuid, request, headerBuilder, info);
                updatePaymentStatusSuccess(identifier, uuid);
            } catch (Exception e) {
                handlePaymentError(e, identifier, uuid);
            }
        } else if (sapIntegrationService.hasBillingProducts(dto)) {
            try {
                sapIntegrationService.sendToSapBillingPayments(uuid, request, info, headerBuilder, dto);
                // sapIntegrationService.sendChannelNotification(uuid, request, headerBuilder, info);
                updatePaymentStatusSuccess(identifier, uuid);
            } catch (Exception e) {
                handlePaymentError(e, identifier, uuid);
            }
        } 

        try {
            sapIntegrationService.sendChannelNotification(uuid, request, headerBuilder, info);
            if (identifier != null) {
                updatePaymentStatusSuccess(identifier, uuid);
            }
        } catch (Exception e) {
            handlePaymentError(e, identifier, uuid);
        }
    }

    private void updatePaymentStatusSuccess(String identifier, UUID uuid) {
        if (identifier != null) {
            try {
                InformationPaymentDTO infoDTO = InformationPaymentDTO.builder()
                        .identifier(identifier)
                        .paymentStatus(PaymentStatusEnum.APPROVED)
                        .payments(List.of(PaymentDTO.builder()
                                .paymentStatus(PaymentStatusEnum.APPROVED)
                                .build()))
                        .build();
                port.sendUpdate(infoDTO);
            } catch (Exception updateException) {
                logger.error("TxId: {} - Erro ao atualizar informacao de pagamento: {}",
                        uuid, updateException.getMessage(), updateException);
            }
        }
    }

    private void handlePaymentError(Exception e, String identifier, UUID uuid) {
        ErrorResponse error;
        
        BusinessException businessException = extractBusinessException(e);
        if (businessException != null && businessException.getError() != null) {
            ErrorResponse originalError = businessException.getError();
            error = ErrorResponse.builder()
                    .message(originalError.getMessage())
                    .details(originalError.getDetails())
                    .errorCode(originalError.getErrorCode())
                    .timestamp(Instant.now())
                    .status(originalError.getStatus())
                    .build();
        } else {
            String errorMessage = determineErrorMessage(e.getMessage());
            error = ErrorResponse.builder()
                    .message(errorMessage)
                    .details(getRootCauseMessage(e))
                    .errorCode("ERROR_MAX_RETRIES_EXCEEDED")
                    .timestamp(Instant.now())
                    .status(HttpStatus.BAD_GATEWAY.value())
                    .build();
        }

        if (identifier != null) {
            InformationPaymentDTO infoDTO = InformationPaymentDTO.builder()
                    .identifier(identifier)
                    .paymentStatus(PaymentStatusEnum.ERROR)
                    .payments(List.of(PaymentDTO.builder()
                            .paymentStatus(PaymentStatusEnum.ERROR)
                            .error(error)
                            .build()))
                    .build();
            port.sendUpdate(infoDTO);
        }

        throw new BusinessException(error);
    }

    private BusinessException extractBusinessException(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof BusinessException) {
                return (BusinessException) cause;
            }
            cause = cause.getCause();
        }
        return null;
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

    /**
     * Detecta se o callback é de cancelamento/estorno baseado no status da transação
     * Status 5 = ESTORNADA, 6 = AGUARDANDO ESTORNO, 9 = ESTORNO PARCIAL
     */
    private boolean isCancellationCallback(TefWebCallbackRequest request) {
        if (request.getSales() == null || request.getSales().isEmpty()) {
            return false;
        }

        SalesDTO sale = request.getSales().get(0);
        if (sale.getTransactions() == null || sale.getTransactions().isEmpty()) {
            return false;
        }

        TransactionsDTO transaction = sale.getTransactions().get(0);
        if (transaction.getTransactionData() == null) {
            return false;
        }

        String status = transaction.getTransactionData().getTransactionStatus();

        boolean isCancellation = TefWebStatusEnum.isCancellationStatus(status);
        
        if (isCancellation) {
            TefWebStatusEnum statusEnum = TefWebStatusEnum.fromCode(status);
            logger.info("Callback de cancelamento detectado. Status TEFWeb: {} - {}", 
                status, statusEnum != null ? statusEnum.getDescription() : "Desconhecido");
        }
        
        return isCancellation;
    }

    /**
     * Processa callback de cancelamento TEFWeb
     */
    private void processCancellationCallback(
            TefWebCallbackRequest request,
            InformationPaymentDTO info,
            UUID uuid,
            String identifier,
            Headers.Builder headerBuilder) {

        logger.info("TxId: {} - Processando callback de CANCELAMENTO TEFWeb", identifier);

        String transactionStatus = extractTransactionStatus(request);
        logger.info("TxId: {} - Status da transação TEFWeb: {}", identifier, transactionStatus);

        // 1. Atualizar Journey MS com o status de cancelamento
        try {
            updateJourneyCancellationStatus(identifier, transactionStatus, uuid);
        } catch (Exception e) {
            logger.error("TxId: {} - Erro ao atualizar status de cancelamento no Journey MS: {}",
                    identifier, e.getMessage(), e);
            // Continua o fluxo mesmo com erro na atualização
        }

        // 2. Notificar Canal (Frontend)
        try {
            sapIntegrationService.sendChannelNotification(uuid, request, headerBuilder, info);
            logger.info("TxId: {} - Callback de cancelamento enviado ao canal com sucesso", identifier);
        } catch (Exception e) {
            logger.error("TxId: {} - Erro ao enviar callback de cancelamento ao canal: {}",
                    identifier, e.getMessage(), e);

            ErrorResponse error = ErrorResponse.builder()
                    .message("Erro ao notificar canal sobre cancelamento")
                    .details(getRootCauseMessage(e))
                    .errorCode("ERROR_CHANNEL_NOTIFICATION_CANCELLATION")
                    .timestamp(Instant.now())
                    .status(HttpStatus.BAD_GATEWAY.value())
                    .build();
            throw new BusinessException(error);
        }
    }

    /**
     * Atualiza o status de cancelamento no Journey MS
     */
    private void updateJourneyCancellationStatus(String identifier, String tefwebStatus, UUID uuid) {
        PaymentStatusEnum journeyStatus;
        String statusDescription;

        TefWebStatusEnum tefWebEnum = TefWebStatusEnum.fromCode(tefwebStatus);
        
        if (tefWebEnum == null) {
            journeyStatus = PaymentStatusEnum.CANCEL_FAILED;
            statusDescription = "Falha no processo de estorno";
            logger.warn("TxId: {} - Status TEFWeb desconhecido para cancelamento: {}", identifier, tefwebStatus);
        } else {
            switch (tefWebEnum) {
                case ESTORNADA:
                    journeyStatus = PaymentStatusEnum.CANCELED;
                    statusDescription = tefWebEnum.getDescription();
                    break;
                case AGUARDANDO_ESTORNO:
                    journeyStatus = PaymentStatusEnum.CANCELING;
                    statusDescription = tefWebEnum.getDescription();
                    break;
                case ESTORNO_PARCIAL:
                    journeyStatus = PaymentStatusEnum.PARTIALLY_CANCELLED;
                    statusDescription = tefWebEnum.getDescription();
                    break;
                default:
                    journeyStatus = PaymentStatusEnum.CANCEL_FAILED;
                    statusDescription = "Falha no processo de estorno";
            }
        }

        logger.info("TxId: {} - Mapeando status TEFWeb '{}' para Journey status '{}': {}",
                identifier, tefwebStatus, journeyStatus, statusDescription);

        try {
            InformationPaymentDTO updateRequest = InformationPaymentDTO.builder()
                    .identifier(identifier)
                    .paymentStatus(journeyStatus)
                    .build();

            port.sendUpdate(updateRequest);

            logger.info("TxId: {} - Status de cancelamento atualizado no Journey MS: {} ({})",
                    identifier, journeyStatus, statusDescription);
        } catch (Exception e) {
            logger.error("TxId: {} - Erro ao atualizar status de cancelamento no Journey MS: {}",
                    identifier, e.getMessage(), e);
            throw new BusinessException(
                    "Erro ao atualizar status de cancelamento no Journey MS",
                    "JOURNEY_CANCELLATION_UPDATE_ERROR"
            );
        }
    }

    /**
     * Extrai o status da transação do callback TEFWeb
     */
    private String extractTransactionStatus(TefWebCallbackRequest request) {
        if (request.getSales() != null && !request.getSales().isEmpty()) {
            SalesDTO sale = request.getSales().get(0);
            if (sale.getTransactions() != null && !sale.getTransactions().isEmpty()) {
                TransactionsDTO transaction = sale.getTransactions().get(0);
                if (transaction.getTransactionData() != null) {
                    return transaction.getTransactionData().getTransactionStatus();
                }
            }
        }
        return null;
    }
}
