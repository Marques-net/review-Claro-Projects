package com.omp.hub.callback.domain.service.impl.notification.impl;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.service.impl.notification.CustomerDataExtractionService;
import com.omp.hub.callback.domain.service.impl.notification.NotificationManagerService;
import org.springframework.stereotype.Service;

import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
import com.omp.hub.callback.domain.service.impl.notification.NotificationService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationManagerServiceImpl implements NotificationManagerService {

    private final CustomerDataExtractionService customerDataExtractionService;
    private final NotificationService notificationService;

    @Override
    public boolean processPixAutomaticoNotification(UUID uuid, String identifier, PixAutomaticoEventEnum eventType) {
        log.info("TxId: {} - Processando notificação PIX Automático - evento: {}", 
                 identifier, eventType.getDescription());

        try {
            ExtractedCustomerDataDTO customerData = extractCustomerDataFromTxId(uuid, identifier);


            if (customerData != null && customerData.hasCompleteData()) {
                customerData = customerDataExtractionService.enrichCustomerData(uuid, customerData);

                if (customerData.getEmail() != null || customerData.getMsisdn() != null) {
                    log.info("TxId: {} - Critérios atendidos, enviando notificação de comunicação", identifier);
                    notificationService.sendPixAutomaticoNotificationWithCustomerData(
                            uuid, identifier, eventType, customerData.getName(),
                            customerData.getMsisdn(), customerData.getEmail());
                    return true;
                } else {
                    log.warn("TxId: {} - Critérios NÃO atendidos ou dados de contato ausentes - email: {}, msisdn: {}",
                            identifier, customerData.getEmail() != null ? customerData.getEmail() : "", customerData.getMsisdn() != null ? customerData.getMsisdn() : "");
                    return false;
                }
            } else {
                log.warn("TxId: {} - Dados do cliente não encontrados ou incompletos para notificação PIX Automático", identifier);
                return false;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("TxId: {} - Erro ao processar notificação PIX Automático - erro: {}", identifier, e.getMessage(), e);
            return false;
        }
    }



    @Override
    public ExtractedCustomerDataDTO extractCustomerDataFromTxId(UUID uuid, String identifier) {
        try {
            return customerDataExtractionService.extractCustomerDataFromPaymentInfo(uuid, identifier);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("TxId: {} - Erro ao extrair dados do cliente - erro: {}", identifier, e.getMessage(), e);
            return null;
        }
    }
}