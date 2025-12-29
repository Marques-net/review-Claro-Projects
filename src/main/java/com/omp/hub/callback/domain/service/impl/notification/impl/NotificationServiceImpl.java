package com.omp.hub.callback.domain.service.impl.notification.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;

import com.omp.hub.callback.domain.model.dto.communication.CommunicationDataDTO;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageRequest;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageResponse;
import com.omp.hub.callback.domain.ports.client.CommunicationPort;
import com.omp.hub.callback.domain.service.impl.notification.NotificationService;

import lombok.RequiredArgsConstructor;
import okhttp3.Headers;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    // Constantes de configuração PIX Automático
    private static final String PROJECT = "Comunic cadastro/movimentações - Pix Automático";
    private static final String SMS_CHANNEL = "1";
    private static final String EMAIL_CHANNEL = "2";

    @Value("${client.communication.campaign:OS74747}")
    private String campaign;

    @Value("${client.communication.templates.optin}")
    private String templateCodeOptin;

    @Value("${client.communication.templates.optout}")
    private String templateCodeOptout;

    @Value("${client.communication.templates.agendamento}")
    private String templateCodeAgendamento;

    @Value("${client.communication.templates.pagamento}")
    private String templateCodePagamento;

    @Value("${client.communication.templates.incentivo_adesao}")
    private String templateCodeIncentivoAdesao;

    @Value("${client.communication.messages.optin}")
    private String messageOptin;

    @Value("${client.communication.messages.optout}")
    private String messageOptout;

    @Value("${client.communication.messages.agendamento}")
    private String messageAgendamento;

    @Value("${client.communication.messages.pagamento}")
    private String messagePagamento;

    @Value("${client.communication.messages.pagamento_avulso}")
    private String messagePagamentoAvulso;

    @Value("${client.communication.messages.alteracao}")
    private String messageAlteracao;

    @Value("${client.communication.messages.cobranca}")
    private String messageCobranca;

    @Value("${client.communication.templates.falha_agendamento}")
    private String templateCodeFalhaAgendamento;

    @Value("${client.communication.messages.falha_agendamento}")
    private String messageFalhaAgendamento;

    @Value("${client.communication.messages.incentivo_adesao}")
    private String messageIncentivoAdesao;

    private final CommunicationPort communicationPort;

    @Autowired
    private ApigeeHeaderService apigeeHeaderService;

    @Override
    public void sendPixAutomaticoNotificationWithCustomerData(UUID uuid, String txId, PixAutomaticoEventEnum eventType,
            String name, String msisdn, String email) {
        logger.info("TxId: {} - Enviando notificação PIX Automático com dados do cliente - evento: {}, nome: {}, msisdn: {}, email: {}",
                txId, eventType.getDescription(), name, msisdn, email);

        try {

            //BLOQUEIO DO RTDM
            if (eventType == PixAutomaticoEventEnum.AGENDAMENTO ||
                //eventType == PixAutomaticoEventEnum.PAGAMENTO ||
                eventType == PixAutomaticoEventEnum.ALTERACAO ||
                eventType == PixAutomaticoEventEnum.COBRANCA) {

                logger.info("TxId: {} - Evento {} bloqueado para RTDM.",
                        txId, eventType.getDescription());
                return;
            }

            if (msisdn != null && !msisdn.isEmpty() && email != null && !email.isEmpty()) {
                sendMobileSmsNotification(uuid, txId, eventType, name, msisdn);
                sendMobileEmailNotification(uuid, txId, eventType, name, email);
            }
            else if (msisdn != null && !msisdn.isEmpty()) {
                sendMobileSmsNotification(uuid, txId, eventType, name, msisdn);
            }
            else if (email != null && !email.isEmpty()) {
                sendMobileEmailNotification(uuid, txId, eventType, name, email);
            }
        } catch (Exception e) {
            logger.error("TxId: {} - Erro ao enviar notificação PIX Automático com dados do cliente - erro: {}",
                    txId, e.getMessage(), e);
        }
    }

    private void sendMobileSmsNotification(UUID uuid, String txId, PixAutomaticoEventEnum eventType, String name, String msisdn) {
        sendSmsNotification(uuid, txId, getTemplateCodeForEvent(eventType), getMessageForEvent(eventType), name, msisdn, "PIX Automático");
    }

    private void sendMobileEmailNotification(UUID uuid, String txId, PixAutomaticoEventEnum eventType, String name, String email) {
        sendEmailNotification(uuid, txId, getTemplateCodeForEvent(eventType), getMessageForEvent(eventType), name, email, "PIX Automático");
    }

    private void sendSmsNotification(UUID uuid, String txId, String templateCode, String message, 
                                   String name, String msisdn, String eventContext) {
        try {
            String templateData = msisdn + ";" + name;

            CommunicationMessageRequest smsRequest = CommunicationMessageRequest.builder()
                .data(CommunicationDataDTO.builder()
                    .layout("")
                    .customization("")
                    .validator("")
                    .templateData(templateData)
                    .destination(msisdn)
                    .channel(SMS_CHANNEL)
                    .project(PROJECT)
                    .campaign(campaign)
                    .mobileClient("")
                    .templateCode(templateCode)
                    .message(message)
                    .build())
                .build();

            Headers.Builder builder = apigeeHeaderService.generateHeaderApigee(uuid);

            CommunicationMessageResponse smsResponse = communicationPort.sendMessage(uuid, smsRequest, builder);

            if (smsResponse != null && smsResponse.getError() == null) {
                logger.info("TxId: {} - SMS {} enviado com sucesso - telefone: {}", txId, eventContext, msisdn);
            } else {
                logger.error("TxId: {} - Erro ao enviar SMS {} - erro: {}", txId, eventContext,
                        smsResponse != null && smsResponse.getError() != null
                                ? smsResponse.getError().getMessage()
                                : "Erro desconhecido");
            }

        } catch (Exception e) {
            logger.error("TxId: {} - Erro ao enviar SMS {} - erro: {}", txId, eventContext, e.getMessage(), e);
        }
    }

    private void sendEmailNotification(UUID uuid, String txId, String templateCode, String message, 
                                     String name, String email, String eventContext) {
        try {
            String templateData = email + ";" + name;

            CommunicationMessageRequest emailRequest = CommunicationMessageRequest.builder()
                    .data(CommunicationDataDTO.builder()
                            .layout("")
                            .customization("")
                            .validator("")
                            .templateData(templateData)
                            .destination(email)
                            .channel(EMAIL_CHANNEL)
                            .project(PROJECT)
                            .campaign(campaign)
                            .mobileClient("")
                            .templateCode(templateCode)
                            .message(message)
                            .build())
                    .build();

            Headers.Builder builder = apigeeHeaderService.generateHeaderApigee(uuid);

            CommunicationMessageResponse emailResponse = communicationPort.sendMessage(uuid, emailRequest, builder);

            if (emailResponse != null && emailResponse.getError() == null) {
                logger.info("TxId: {} - Email {} enviado com sucesso - email: {}", txId, eventContext, email);
            } else {
                logger.error("TxId: {} - Erro ao enviar email {} - erro: {}", txId, eventContext,
                        emailResponse != null && emailResponse.getError() != null
                                ? emailResponse.getError().getMessage()
                                : "Erro desconhecido");
            }

        } catch (Exception e) {
            logger.error("TxId: {} - Erro ao enviar email {} - erro: {}", txId, eventContext, e.getMessage(), e);
        }
    }



    private String getTemplateCodeForEvent(PixAutomaticoEventEnum eventType) {
        return switch (eventType) {
            case OPTIN -> templateCodeOptin;
            case OPTOUT -> templateCodeOptout;
            case AGENDAMENTO -> templateCodeAgendamento;
            case PAGAMENTO -> templateCodePagamento;
            //case PAGAMENTO_AVULSO -> templateCodePagamento;
            case ALTERACAO -> templateCodeOptin;
            case COBRANCA -> templateCodeAgendamento;
            case FALHA_AGENDAMENTO -> templateCodeFalhaAgendamento;
            case INCENTIVO_ADESAO -> templateCodeIncentivoAdesao;
        };
    }

    private String getMessageForEvent(PixAutomaticoEventEnum eventType) {
        return switch (eventType) {
            case OPTIN -> messageOptin;
            case OPTOUT -> messageOptout;
            case AGENDAMENTO -> messageAgendamento;
            case PAGAMENTO -> messagePagamento;
            //case PAGAMENTO_AVULSO -> messagePagamentoAvulso;
            case ALTERACAO -> messageAlteracao;
            case COBRANCA -> messageCobranca;
            case FALHA_AGENDAMENTO -> messageFalhaAgendamento;
            case INCENTIVO_ADESAO -> messageIncentivoAdesao;
        };
    }
}
