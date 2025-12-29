package com.omp.hub.callback.domain.service.impl.notification.impl;

import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.service.impl.notification.PixEventMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PixEventMappingServiceImpl implements PixEventMappingService {

    private static final Logger logger = LoggerFactory.getLogger(PixEventMappingServiceImpl.class);

    private static final java.util.Set<String> NOTIFIABLE_EVENTS = java.util.Set.of(
            "CHANGE_PAYMENT_METHOD", "ALTERACAO",
            "CHARGE", "COBRANCA",
            "OPT_IN",
            "OPT_OUT",
            "PAYMENT", "PAGAMENTO"
    );
        //Eventos removidos do NOTIFIABLE_EVENTS
        //"PAYMENT", "PAGAMENTO"
        //"AGENDAMENTO"

    @org.springframework.beans.factory.annotation.Value("${client.communication.templates.pagamento}")
    private String templateCodePayment;

    @org.springframework.beans.factory.annotation.Value("${client.communication.templates.alteracao}")
    private String templateCodeAlteracao;

    @org.springframework.beans.factory.annotation.Value("${client.communication.templates.cobranca}")
    private String templateCodeCobranca;

    @org.springframework.beans.factory.annotation.Value("${client.communication.templates.falha_agendamento}")
    private String templateCodeFalhaAgendamento;

    @org.springframework.beans.factory.annotation.Value("${client.communication.templates.incentivo_adesao}")
    private String templateCodeIncentivoAdesao;

    @Override
    public PixAutomaticoEventEnum mapPaymentTypeToEvent(String txId, String paymentType) {
        if (paymentType == null) {
            logger.warn("TxId: {} - Tipo de pagamento é nulo", txId);
            return null;
        }

        PixAutomaticoEventEnum eventType = switch (paymentType.toUpperCase()) {
            case "PIX_AUTOMATICO", "OPTIN", "ADESAO" -> PixAutomaticoEventEnum.OPTIN;
            case "PIX_AUTOMATICO_OPTOUT", "OPTOUT", "CANCELAMENTO" -> PixAutomaticoEventEnum.OPTOUT;
            case "PIX_AUTOMATICO_AGENDAMENTO", "AGENDAMENTO", "RECORRENCIA" -> PixAutomaticoEventEnum.AGENDAMENTO;
            case "PIX_AUTOMATICO_PAGAMENTO", "PAGAMENTO", "EXECUTADO" -> PixAutomaticoEventEnum.PAGAMENTO;
            //case "PIX" -> PixAutomaticoEventEnum.PAGAMENTO_AVULSO;
            default -> {
                logger.warn("TxId: {} - Tipo de pagamento não mapeado para evento PIX Automático: {}", txId, paymentType);
                yield null;
            }
        };

        if (eventType != null) {
            logger.debug("TxId: {} - Mapeando paymentType '{}' para evento PIX Automático: {}", txId, paymentType, eventType.getDescription());
        }

        return eventType;
    }

    @Override
    public boolean isPixAutomaticoEvent(String txId, String paymentType) {
        boolean isPixAutomatico = mapPaymentTypeToEvent(txId, paymentType) != null;
        logger.debug("TxId: {} - Verificando se '{}' é evento PIX Automático: {}", txId, paymentType, isPixAutomatico);
        return isPixAutomatico;
    }

    @Override
    public boolean shouldNotify(String txId, String eventType) {
        boolean shouldNotify = eventType != null && NOTIFIABLE_EVENTS.contains(eventType.toUpperCase());
        logger.debug("TxId: {} - Verificando se evento '{}' deve gerar notificação: {}", txId, eventType, shouldNotify);
        return shouldNotify;
    }

    @Override
    public PixAutomaticoEventEnum mapEventTypeToEnum(String txId, String eventType) {
        if (eventType == null) {
            logger.warn("TxId: {} - Tipo de evento é nulo, retornando null", txId);
            return null;
        }

        // TODO: Possiveis Futuros Ajustes
        PixAutomaticoEventEnum mappedEnum = switch (eventType.toUpperCase()) {
            case "CHANGE_PAYMENT_METHOD" -> PixAutomaticoEventEnum.ALTERACAO;
            case "CHARGE" -> PixAutomaticoEventEnum.COBRANCA;
            case "PAYMENT" -> PixAutomaticoEventEnum.PAGAMENTO;
            default -> PixAutomaticoEventEnum.fromString(eventType.toUpperCase());
        };

        logger.debug("TxId: {} - Mapeando evento '{}' para enum: {}", txId, eventType, mappedEnum);
        return mappedEnum;
    }

    @Override
    public PixAutomaticoEventEnum mapEventTypeToEnum(String txId, String eventType, String status, String paymentMethod) {
        if (eventType == null) {
            logger.warn("TxId: {} - Tipo de evento é nulo, retornando null", txId);
            return null;
        }

        PixAutomaticoEventEnum mappedEnum = switch (eventType.toUpperCase()) {
            case "CHANGE_PAYMENT_METHOD" -> mapChangePaymentMethodEvent(status, paymentMethod);
            case "CHARGE" -> mapChargeEvent(status, paymentMethod);
            case "PAYMENT" -> PixAutomaticoEventEnum.PAGAMENTO;
            default -> PixAutomaticoEventEnum.fromString(eventType.toUpperCase());
        };

        logger.debug("TxId: {} - Mapeando evento '{}' com status '{}' e paymentMethod '{}' para enum: {}",
                txId, eventType, status, paymentMethod, mappedEnum);
        return mappedEnum;
    }

    @Override
    public PixAutomaticoEventEnum mapEventTypeToEnum(String txId, String eventType, String status, String paymentMethod, String recurrenceId) {
        if (eventType == null) {
            logger.warn("TxId: {} - Tipo de evento é nulo, retornando null", txId);
            return null;
        }

        PixAutomaticoEventEnum mappedEnum = switch (eventType.toUpperCase()) {
            case "CHANGE_PAYMENT_METHOD" -> mapChangePaymentMethodEvent(status, paymentMethod, recurrenceId);
            case "CHARGE" -> mapChargeEvent(status, paymentMethod);
            case "PAYMENT" -> PixAutomaticoEventEnum.PAGAMENTO;
            default -> PixAutomaticoEventEnum.fromString(eventType.toUpperCase());
        };

        logger.debug("TxId: {} - Mapeando evento '{}' com status '{}', paymentMethod '{}' e recurrenceId '{}' para enum: {}",
                txId, eventType, status, paymentMethod, recurrenceId, mappedEnum);
        return mappedEnum;
    }

    private PixAutomaticoEventEnum mapChargeEvent(String status, String paymentMethod) {
        if (status == null) {
            return PixAutomaticoEventEnum.COBRANCA;
        }

        return switch (status.toUpperCase()) {
            case "CRIADA", "CREATED", "ATIVA", "ACTIVE" -> {

                //Removido para nao enviar notificacao
                
                //if (paymentMethod != null && paymentMethod.toUpperCase().contains("PIX_AUTOMATICO")) {
                //    yield PixAutomaticoEventEnum.AGENDAMENTO;
                //}
                //yield PixAutomaticoEventEnum.OPTIN;
                yield null; 
            }
            case "PAGA", "PAID", "CONCLUIDA", "CONCLUIDO" -> {

                if (paymentMethod != null && paymentMethod.toUpperCase().contains("PIX_AUTOMATICO")) {
                   yield PixAutomaticoEventEnum.PAGAMENTO;
                }
                yield PixAutomaticoEventEnum.OPTIN;
            }
            case "CANCELADA", "EXPIRADA", "REJEITADA" -> {
                if (paymentMethod != null && paymentMethod.toUpperCase().contains("PIX_AUTOMATICO")) {
                    yield PixAutomaticoEventEnum.FALHA_AGENDAMENTO;
                }
                yield PixAutomaticoEventEnum.OPTIN;
            }
            default -> PixAutomaticoEventEnum.OPTIN;
        };
    }

    private PixAutomaticoEventEnum mapChangePaymentMethodEvent(String status, String paymentMethod) {
        if (status != null && status.toUpperCase().equals("CANCELADA")) {
            return PixAutomaticoEventEnum.OPTOUT;
        }
        if (status != null && status.toUpperCase().equals("CRIADA")) {
            return PixAutomaticoEventEnum.INCENTIVO_ADESAO;
        }
        if (status != null && status.toUpperCase().equals("APROVADA")) {
            return PixAutomaticoEventEnum.OPTIN;
        }
        return PixAutomaticoEventEnum.ALTERACAO;
    }

    private PixAutomaticoEventEnum mapChangePaymentMethodEvent(String status, String paymentMethod, String recurrenceId) {
        if (status != null && status.toUpperCase().equals("CANCELADA")) {
            return PixAutomaticoEventEnum.OPTOUT;
        }
        if (status != null && status.toUpperCase().equals("CRIADA")) {
            return PixAutomaticoEventEnum.INCENTIVO_ADESAO;
        }
        if (status != null && status.toUpperCase().equals("APROVADA")) {
            return PixAutomaticoEventEnum.OPTIN;
        }
        if (recurrenceId != null && !recurrenceId.trim().isEmpty()) {
            return PixAutomaticoEventEnum.INCENTIVO_ADESAO;
        }

        return PixAutomaticoEventEnum.ALTERACAO;
    }

    @Override
    public String getTemplateCodeForEventType(String txId, PixAutomaticoEventEnum eventType) {
        if (eventType == null) {
            logger.warn("TxId: {} - Tipo de evento enum é nulo, retornando template padrão", txId);
            return templateCodePayment;
        }

        // TODO: Possiveis Futuros Ajustes
        String templateCode = switch (eventType) {
//            case PAGAMENTO, PAGAMENTO_AVULSO -> templateCodePayment;
            case PAGAMENTO -> templateCodePayment;
            case ALTERACAO -> templateCodeAlteracao;
            case COBRANCA -> templateCodeCobranca;
            case OPTIN, OPTOUT -> templateCodeAlteracao;
            case AGENDAMENTO -> templateCodeCobranca;
            case FALHA_AGENDAMENTO -> templateCodeFalhaAgendamento;
            case INCENTIVO_ADESAO -> templateCodeIncentivoAdesao;
        };

        logger.debug("TxId: {} - Template code para evento '{}': {}", txId, eventType, templateCode);
        return templateCode;
    }
}

