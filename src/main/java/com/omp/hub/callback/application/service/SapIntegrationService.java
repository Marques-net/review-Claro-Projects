package com.omp.hub.callback.application.service;

import com.omp.hub.callback.domain.enums.PaymentStatusEnum;
import com.omp.hub.callback.domain.enums.PaymentTypeEnum;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.DataSingleDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.ProductDTO;
import com.omp.hub.callback.domain.model.dto.omphub.transaction.notification.OmphubTransactionNotificationRequest;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.ports.client.SapBillingPaymentsPort;
import com.omp.hub.callback.domain.ports.client.SapPaymentsPort;
import com.omp.hub.callback.domain.ports.client.SapRedemptionsPort;
import com.omp.hub.callback.domain.ports.client.TransationsNotificationsPort;
import com.omp.hub.callback.domain.service.generate.GenerateCallbackTefWebService;
import com.omp.hub.callback.domain.service.generate.GenerateSapBillingPaymentsRequestService;
import com.omp.hub.callback.domain.service.generate.GenerateSapPaymentsRequestService;
import com.omp.hub.callback.domain.service.generate.GenerateSapRedemptionsRequestService;
import com.omp.hub.callback.infrastructure.config.MultiplePaymentConfig;
import okhttp3.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SapIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(SapIntegrationService.class);

    @Autowired
    private InformationPaymentPort informationPaymentPort;

    @Autowired
    private SapRedemptionsPort redemptionsPort;

    @Autowired
    private GenerateSapRedemptionsRequestService generateRedemptionsService;

    @Autowired
    private SapPaymentsPort paymentsPort;

    @Autowired
    private GenerateSapPaymentsRequestService generatePaymentsService;

    @Autowired
    private SapBillingPaymentsPort billingPaymentsPort;

    @Autowired
    private GenerateSapBillingPaymentsRequestService generateBillingPaymentsService;

    @Autowired
    private TransationsNotificationsPort transactionsPort;

    @Autowired
    private GenerateCallbackTefWebService callbackTefWebService;

    @Autowired
    private RetryService retryService;

    @Autowired
    private MultiplePaymentConfig multiplePaymentConfig;

    public boolean shouldSendToSap(InformationPaymentDTO info, PaymentTypeEnum currentPaymentType) {
        logger.info("Verificando shouldSendToSap - identifier: {}, multiplePayment: {}, currentPaymentType: {}", 
            info.getIdentifier(), info.getMultiplePayment(), currentPaymentType);
        
        if (!Boolean.TRUE.equals(info.getMultiplePayment())) {
            logger.info("Pagamento simples - enviando para SAP");
            return true;
        }

        boolean result = areAllPaymentsApproved(info, currentPaymentType);
        logger.info("Resultado areAllPaymentsApproved: {}", result);
        return result;
    }

    public boolean areAllPaymentsApproved(InformationPaymentDTO info, PaymentTypeEnum currentPaymentType) {
        if (info == null || info.getPayments() == null || info.getPayments().isEmpty()) {
            return false;
        }

        logger.info("Verificando se todos os pagamentos estão aprovados - identifier: {}", info.getIdentifier());
        
        boolean hasPendingPayments = false;
        boolean onlyCashIsPending = true;
        PaymentDTO pendingCashPayment = null;
        
        for (PaymentDTO payment : info.getPayments()) {
            logger.info("  Pagamento: tipo={}, status={}", payment.getType(), payment.getPaymentStatus());
            
            if (payment.getPaymentStatus() != PaymentStatusEnum.APPROVED) {
                hasPendingPayments = true;
                
                // Se algum pagamento diferente de CASH está pendente, não podemos ignorar
                if (payment.getType() != PaymentTypeEnum.CASH) {
                    onlyCashIsPending = false;
                } else {
                    pendingCashPayment = payment;
                }
                
                logger.info("Pagamento {} ainda não está APPROVED (status: {})", 
                    payment.getType(), payment.getPaymentStatus());
            }
        }

        // Se não há pagamentos pendentes, todos estão aprovados
        if (!hasPendingPayments) {
            logger.info("Todos os pagamentos estão APPROVED! Pode enviar para SAP.");
            return true;
        }
        
        // Se apenas CASH está pendente E o pagamento CASH realmente existe na lista, aprova automaticamente
        // pois CASH não tem callback específico
        if (onlyCashIsPending && pendingCashPayment != null) {
            logger.info("Apenas CASH está pendente. Tentando aprovar automaticamente CASH no pagamento misto.");
            
            try {
                String transactionOrderId = pendingCashPayment.getTransactionOrderId();
                if (transactionOrderId == null && info.getTransactionOrderId() != null) {
                    transactionOrderId = info.getTransactionOrderId();
                }
                
                // Cria DTO do CASH com o valor já existente E o paymentOrder
                InformationPaymentDTO cashUpdateDTO = InformationPaymentDTO.builder()
                        .payments(List.of(PaymentDTO.builder()
                                .type(PaymentTypeEnum.CASH)
                                .transactionOrderId(transactionOrderId)
                                .paymentStatus(PaymentStatusEnum.APPROVED)
                                .value(pendingCashPayment.getValue())
                                .paymentOrder(pendingCashPayment.getPaymentOrder())
                                .updatedAt(Instant.now())
                                .updatedAtTimestamp(Instant.now().toEpochMilli())
                                .build()))
                        .build();
                
                // Aprova o CASH automaticamente
                logger.info("Aprovando CASH com paymentOrder: {} para identifier: {}", pendingCashPayment.getPaymentOrder(), info.getIdentifier());
                informationPaymentPort.updatePaymentInList(info.getIdentifier(), PaymentTypeEnum.CASH.name(), cashUpdateDTO);
                
                logger.info("CASH aprovado automaticamente. Permitindo envio para SAP.");
                return true;
            } catch (Exception e) {
                logger.error("Erro ao tentar aprovar CASH automaticamente para identifier: {}. Erro: {}. Continuando sem aprovar CASH.", 
                    info.getIdentifier(), e.getMessage());
                // Se falhar ao aprovar CASH, considera que não pode enviar para SAP ainda
                // pois o CASH pode realmente não existir ou já ter sido processado
                return false;
            }
        }
        
        logger.info("Existem pagamentos pendentes além de CASH. Aguardando...");
        return false;
    }

    public void updatePaymentStatusApproved(String identifier, PaymentTypeEnum paymentType, String transactionOrderId) {
        updatePaymentStatusApproved(identifier, paymentType, transactionOrderId, null);
    }

    public void updatePaymentStatusApproved(String identifier, PaymentTypeEnum paymentType, String transactionOrderId, Object callbackData) {
        logger.info("Atualizando status do pagamento para APPROVED - identifier: {}, tipo: {}, transactionOrderId: {}, hasCallback: {}", 
                identifier, paymentType, transactionOrderId, callbackData != null);
        
        if (callbackData != null) {
            logger.info("Callback sendo enviado - identifier: {}, tipo: {}, callbackClass: {}", 
                    identifier, paymentType, callbackData.getClass().getName());
        }

        // Buscar informações atuais do pagamento
        InformationPaymentDTO existingInfo = informationPaymentPort.sendFindByIdentifier(identifier);
        
        // Detectar múltiplas transações TefWeb
        List<PaymentDTO> paymentsToUpdate = new java.util.ArrayList<>();
        boolean hasMultipleTransactions = false;
        
        // Contar quantos TEFWEBs já existem na base
        int existingTefwebCount = 0;
        if (paymentType == PaymentTypeEnum.TEFWEB && existingInfo != null && existingInfo.getPayments() != null) {
            existingTefwebCount = (int) existingInfo.getPayments().stream()
                .filter(p -> p.getType() == PaymentTypeEnum.TEFWEB)
                .count();
            logger.info("Identificados {} TEFWEBs existentes para identifier: {}", existingTefwebCount, identifier);
        }
        
        if (paymentType == PaymentTypeEnum.TEFWEB && callbackData instanceof com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest) {
            com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest tefwebRequest = 
                (com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest) callbackData;
            
            if (tefwebRequest.getSales() != null && !tefwebRequest.getSales().isEmpty() 
                    && tefwebRequest.getSales().get(0).getTransactions() != null) {
                
                List<com.omp.hub.callback.domain.model.dto.callback.tefweb.TransactionsDTO> transactions = 
                    tefwebRequest.getSales().get(0).getTransactions();
                
                logger.info("Identificadas {} transações TefWeb no callback atual para identifier: {}", transactions.size(), identifier);
                
                // Detectar múltiplos TEFWEBs: se já existem múltiplos OU se vêm múltiplos no callback
                if (existingTefwebCount > 1 || transactions.size() > 1) {
                    hasMultipleTransactions = true;
                    logger.info("Múltiplas transações TEFWEB detectadas (existentes: {}, no callback: {})", 
                        existingTefwebCount, transactions.size());
                    
                    // Buscar os paymentOrders dos TEFWEBs existentes para preservá-los
                    List<Integer> existingTefwebOrders = new java.util.ArrayList<>();
                    List<PaymentDTO> existingTefwebPayments = new java.util.ArrayList<>();
                    if (existingInfo != null && existingInfo.getPayments() != null) {
                        existingInfo.getPayments().stream()
                            .filter(p -> p.getType() == paymentType)
                            .sorted((p1, p2) -> {
                                Integer order1 = p1.getPaymentOrder() != null ? p1.getPaymentOrder() : Integer.MAX_VALUE;
                                Integer order2 = p2.getPaymentOrder() != null ? p2.getPaymentOrder() : Integer.MAX_VALUE;
                                return Integer.compare(order1, order2);
                            })
                            .forEach(p -> {
                                existingTefwebOrders.add(p.getPaymentOrder());
                                existingTefwebPayments.add(p);
                            });
                    }
                    
                    logger.info("PaymentOrders TEFWEB existentes: {}", existingTefwebOrders);
                    
                    // Se o callback tem apenas 1 transação mas existem múltiplos TEFWEBs,
                    // atualizar apenas o TEFWEB correspondente preservando os outros
                    if (transactions.size() == 1 && existingTefwebCount > 1) {
                        logger.info("Callback com 1 transação em contexto de múltiplos TEFWEBs - atualizando apenas o correspondente");
                        
                        // Extrair valor da transação
                        com.omp.hub.callback.domain.model.dto.callback.tefweb.TransactionsDTO transaction = transactions.get(0);
                        java.math.BigDecimal callbackValue = null;
                        try {
                            if (transaction.getTransactionData() != null && transaction.getTransactionData().getValue() != null) {
                                callbackValue = new java.math.BigDecimal(transaction.getTransactionData().getValue())
                                    .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                            }
                        } catch (Exception e) {
                            logger.warn("Erro ao extrair valor da transação: {}", e.getMessage());
                        }
                        
                        // Encontrar qual TEFWEB atualizar: sempre pegar o primeiro PENDING
                        // (pois os callbacks chegam na ordem em que foram processados)
                        PaymentDTO tefwebToUpdate = null;
                        int indexToUpdate = -1;
                        
                        // Pegar o primeiro TEFWEB PENDING (callbacks chegam em ordem)
                        for (int i = 0; i < existingTefwebPayments.size(); i++) {
                            PaymentDTO existing = existingTefwebPayments.get(i);
                            if (existing.getPaymentStatus() == PaymentStatusEnum.PENDING) {
                                tefwebToUpdate = existing;
                                indexToUpdate = i;
                                logger.info("Atualizando primeiro TEFWEB PENDING encontrado (paymentOrder: {}) com valor do callback: R$ {}", 
                                    existing.getPaymentOrder(), callbackValue);
                                break;
                            }
                        }
                        
                        if (tefwebToUpdate != null) {
                            // Recriar todos os TEFWEBs, atualizando apenas o correspondente
                            for (int i = 0; i < existingTefwebPayments.size(); i++) {
                                PaymentDTO existing = existingTefwebPayments.get(i);
                                if (i == indexToUpdate) {
                                    // Este é o que vamos atualizar
                                    paymentsToUpdate.add(PaymentDTO.builder()
                                            .type(paymentType)
                                            .transactionOrderId(transactionOrderId)
                                            .paymentStatus(PaymentStatusEnum.APPROVED)
                                            .callback(callbackData)
                                            .value(callbackValue)
                                            .paymentOrder(existing.getPaymentOrder())
                                            .updatedAt(Instant.now())
                                            .updatedAtTimestamp(Instant.now().toEpochMilli())
                                            .build());
                                    logger.info("Atualizando TEFWEB paymentOrder={} para APPROVED com valor R$ {}", 
                                        existing.getPaymentOrder(), callbackValue);
                                } else {
                                    // Preservar os outros TEFWEBs como estão
                                    paymentsToUpdate.add(existing);
                                    logger.info("Preservando TEFWEB paymentOrder={} com status {}", 
                                        existing.getPaymentOrder(), existing.getPaymentStatus());
                                }
                            }
                        }
                    } else {
                        // Callback com múltiplas transações - substituir todos os TEFWEBs
                        logger.info("Callback com {} transações - substituindo todos os TEFWEBs", transactions.size());
                        
                        for (int i = 0; i < transactions.size(); i++) {
                            com.omp.hub.callback.domain.model.dto.callback.tefweb.TransactionsDTO transaction = transactions.get(i);
                            java.math.BigDecimal value = null;
                            
                            try {
                                if (transaction.getTransactionData() != null && transaction.getTransactionData().getValue() != null) {
                                    value = new java.math.BigDecimal(transaction.getTransactionData().getValue())
                                        .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                                }
                            } catch (Exception e) {
                                logger.warn("Erro ao extrair valor da transação {}: {}", i, e.getMessage());
                            }
                            
                            Integer paymentOrder = (i < existingTefwebOrders.size()) ? existingTefwebOrders.get(i) : (i + 1);
                            
                            paymentsToUpdate.add(PaymentDTO.builder()
                                    .type(paymentType)
                                    .transactionOrderId(transactionOrderId)
                                    .paymentStatus(PaymentStatusEnum.APPROVED)
                                    .callback(callbackData)
                                    .value(value)
                                    .transactionIndex(i)
                                    .paymentOrder(paymentOrder)
                                    .updatedAt(Instant.now())
                                    .updatedAtTimestamp(Instant.now().toEpochMilli())
                                    .build());
                            
                            logger.info("Criado payment TEFWEB #{} com paymentOrder={}, valor={}", i + 1, paymentOrder, value);
                        }
                    }
                }
            }
        }
        
        // Se não detectou múltiplas transações, usar lógica original
        if (paymentsToUpdate.isEmpty()) {
            paymentsToUpdate.add(PaymentDTO.builder()
                    .type(paymentType)
                    .transactionOrderId(transactionOrderId)
                    .paymentStatus(PaymentStatusEnum.APPROVED)
                    .callback(callbackData)
                    .updatedAt(Instant.now())
                    .updatedAtTimestamp(Instant.now().toEpochMilli())
                    .build());
        }

        if (hasMultipleTransactions) {
            // Substituir todos os payments TEFWEB pelos novos, preservando outros tipos
            List<PaymentDTO> allPayments = new java.util.ArrayList<>();
            
            // Adicionar payments de outros tipos que não são TEFWEB (preservando ordem original)
            if (existingInfo != null && existingInfo.getPayments() != null) {
                for (PaymentDTO existingPayment : existingInfo.getPayments()) {
                    if (existingPayment.getType() != paymentType) {
                        allPayments.add(existingPayment);
                    }
                }
            }
            
            // Adicionar os novos payments TEFWEB
            allPayments.addAll(paymentsToUpdate);
            
            // Ordenar por paymentOrder para manter consistência
            allPayments.sort((p1, p2) -> {
                Integer order1 = p1.getPaymentOrder() != null ? p1.getPaymentOrder() : Integer.MAX_VALUE;
                Integer order2 = p2.getPaymentOrder() != null ? p2.getPaymentOrder() : Integer.MAX_VALUE;
                return Integer.compare(order1, order2);
            });
            
            // Atualizar usando sendUpdate para substituir a lista completa
            InformationPaymentDTO fullUpdateDTO = InformationPaymentDTO.builder()
                    .identifier(identifier)
                    .payments(allPayments)
                    .updatedAt(Instant.now())
                    .updatedAtTimestamp(Instant.now().toEpochMilli())
                    .build();
            
            logger.info("Atualizando {} payments totais (incluindo {} transações TEFWEB), ordenados por paymentOrder", 
                    allPayments.size(), paymentsToUpdate.size());
            
            informationPaymentPort.sendUpdate(fullUpdateDTO);
        } else {
            // Usar o método original para atualizar apenas um payment
            InformationPaymentDTO updateDTO = InformationPaymentDTO.builder()
                    .payments(paymentsToUpdate)
                    .build();

            informationPaymentPort.updatePaymentInList(identifier, paymentType.name(), updateDTO);
        }
    }

    public String extractBaseTransactionOrderId(String transactionOrderId) {
        if (transactionOrderId == null || transactionOrderId.isEmpty()) {
            return transactionOrderId;
        }

        String prefix = getPrefix();
        String suffixFormat = getSuffixFormat();
        Integer baseLength = getBaseLength();
        
        String withoutPrefix = transactionOrderId.startsWith(prefix) 
            ? transactionOrderId.substring(prefix.length())
            : transactionOrderId;
        
        String suffixPrefix = suffixFormat.substring(0, 1);
        int suffixIndex = withoutPrefix.indexOf(suffixPrefix);
        if (suffixIndex > 0) {
            String withPadding = withoutPrefix.substring(0, suffixIndex);
            if (withPadding.length() > baseLength) {
                return withPadding.substring(withPadding.length() - baseLength);
            }
            return withPadding;
        }

        return withoutPrefix;
    }

    private boolean isValidUUID(String value) {
        if (value == null) {
            return false;
        }
        try {
            java.util.UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void sendToSapRedemptionsAndPayments(
            UUID uuid,
            TefWebCallbackRequest request,
            InformationPaymentDTO info,
            Headers.Builder headerBuilder, DataSingleDTO dto) {

        String transactionOrderId = getTransactionOrderIdForSap(info, dto);

        InformationPaymentDTO infoForSap = InformationPaymentDTO.builder()
                .uuid(info.getUuid())
                .identifier(info.getIdentifier())
                .transactionOrderId(transactionOrderId)
                .channel(info.getChannel())
                .store(info.getStore())
                .pdv(info.getPdv())
                .multiplePayment(info.getMultiplePayment())
                .mixedPaymentTypes(info.getMixedPaymentTypes())
                .payments(info.getPayments())
                .amount(info.getAmount())
                .paymentStatus(info.getPaymentStatus())
                .build();

        logger.info("Enviando para SAP Redemptions - uuid: {}, transactionOrderId: {}", uuid, transactionOrderId);
        retryService.executeWithRetrySyncVoid(uuid, "SAP Redemptions",
                () -> redemptionsPort.send(uuid, generateRedemptionsService.generateRequest(infoForSap), headerBuilder), request);

        logger.info("Enviando para SAP Payments - uuid: {}, transactionOrderId: {}", uuid, transactionOrderId);
        retryService.executeWithRetrySyncVoid(uuid, "SAP Payments",
                () -> paymentsPort.send(uuid, generatePaymentsService.generateRequest(request, infoForSap), headerBuilder), request);
    }

    public void sendToSapBillingPayments(
            UUID uuid,
            TefWebCallbackRequest request,
            InformationPaymentDTO info,
            Headers.Builder headerBuilder, DataSingleDTO dto) {

        String transactionOrderId = getTransactionOrderIdForSap(info, dto);

        InformationPaymentDTO infoForSap = InformationPaymentDTO.builder()
                .uuid(info.getUuid())
                .identifier(info.getIdentifier())
                .transactionOrderId(transactionOrderId)
                .channel(info.getChannel())
                .store(info.getStore())
                .pdv(info.getPdv())
                .multiplePayment(info.getMultiplePayment())
                .mixedPaymentTypes(info.getMixedPaymentTypes())
                .payments(info.getPayments())
                .amount(info.getAmount())
                .paymentStatus(info.getPaymentStatus())
                .build();

        logger.info("Enviando para SAP Billing Payments - uuid: {}, transactionOrderId: {}", uuid, transactionOrderId);
        retryService.executeWithRetrySyncVoid(uuid, "SAP Billing Payments",
                () -> billingPaymentsPort.send(uuid, generateBillingPaymentsService.generateRequest(request, infoForSap), headerBuilder), request);
    }

    public void sendChannelNotification(
            UUID uuid,
            TefWebCallbackRequest request,
            Headers.Builder headerBuilder,
            InformationPaymentDTO info) throws Exception {

        String transactionOrderId = info.getTransactionOrderId();
        String ompTransactionId = request.getOmpTransactionId();

        if (ompTransactionId == null && request.getSales() != null && !request.getSales().isEmpty()
                && request.getSales().get(0).getOrder() != null) {
            ompTransactionId = request.getSales().get(0).getOrder().getOmpTransactionId();
        }

        OmphubTransactionNotificationRequest notificationRequest;
        if (Boolean.TRUE.equals(info.getMultiplePayment())) {
            notificationRequest = callbackTefWebService.generateConsolidatedRequest(request, transactionOrderId, info);
        } else {
            notificationRequest = callbackTefWebService.generateRequest(request, transactionOrderId);
        }
        
        logger.info("Enviando notificacao para o canal - uuid: {}, ompTransactionId: {}", uuid, ompTransactionId);
        retryService.executeWithRetrySyncVoid(uuid, "Channel Notification",
                () -> transactionsPort.send(uuid, notificationRequest, headerBuilder), request);
    }

    public boolean hasBillingProducts(DataSingleDTO dto) {
        if (dto == null || dto.getFraudAnalysisData() == null
                || dto.getFraudAnalysisData().getComplementaryData() == null
                || dto.getFraudAnalysisData().getComplementaryData().getProducts() == null) {
            return false;
        }

        List<ProductDTO> products = dto.getFraudAnalysisData().getComplementaryData().getProducts();
        return products.stream().anyMatch(p -> "T30".equals(p.getCode()))
                || products.stream().anyMatch(p -> "T3A".equals(p.getCode()));
    }

    public boolean hasSalesOrderId(DataSingleDTO dto, String identifier) {
        return (dto != null && dto.getPayment() != null && dto.getPayment().getSalesOrderId() != null
                && !dto.getPayment().getSalesOrderId().isEmpty())
                || (dto != null && dto.getPayment() != null && identifier != null);
    }

    private String getTransactionOrderIdForSap(InformationPaymentDTO info, DataSingleDTO dto) {
         if (Boolean.TRUE.equals(info.getMultiplePayment()) && info.getTransactionOrderId() != null) {
            return extractBaseTransactionOrderId(info.getTransactionOrderId());
        }
        return dto.getPayment().getSalesOrderId();
    }

    private String getPrefix() {
        if (multiplePaymentConfig != null 
            && multiplePaymentConfig.getTransactionOrderId() != null 
            && multiplePaymentConfig.getTransactionOrderId().getPrefix() != null) {
            return multiplePaymentConfig.getTransactionOrderId().getPrefix();
        }
        return "SV";
    }

    private String getSuffixFormat() {
        if (multiplePaymentConfig != null 
            && multiplePaymentConfig.getTransactionOrderId() != null 
            && multiplePaymentConfig.getTransactionOrderId().getSuffixFormat() != null) {
            return multiplePaymentConfig.getTransactionOrderId().getSuffixFormat();
        }
        return "H%d";
    }

    private Integer getBaseLength() {
        if (multiplePaymentConfig != null 
            && multiplePaymentConfig.getTransactionOrderId() != null 
            && multiplePaymentConfig.getTransactionOrderId().getBaseLength() != null) {
            return multiplePaymentConfig.getTransactionOrderId().getBaseLength();
        }
        return 10;
    }
}
