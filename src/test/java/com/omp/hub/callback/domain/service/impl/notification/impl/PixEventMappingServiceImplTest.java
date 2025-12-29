package com.omp.hub.callback.domain.service.impl.notification.impl;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;

@ExtendWith(MockitoExtension.class)
class PixEventMappingServiceImplTest {

    @InjectMocks
    private PixEventMappingServiceImpl pixEventMappingService;

    private final String txId = "test-tx-123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pixEventMappingService, "templateCodePayment", "PAYMENT_TEMPLATE");
        ReflectionTestUtils.setField(pixEventMappingService, "templateCodeAlteracao", "ALTERACAO_TEMPLATE");
        ReflectionTestUtils.setField(pixEventMappingService, "templateCodeCobranca", "COBRANCA_TEMPLATE");
        ReflectionTestUtils.setField(pixEventMappingService, "templateCodeFalhaAgendamento", "FALHA_AGENDAMENTO_TEMPLATE");
        ReflectionTestUtils.setField(pixEventMappingService, "templateCodeIncentivoAdesao", "INCENTIVO_ADESAO_TEMPLATE");
    }

    @Test
    void mapPaymentTypeToEvent_ShouldMapOptinVariations() {
        // When & Then
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "PIX_AUTOMATICO"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "OPTIN"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "ADESAO"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
    }

    @Test
    void mapPaymentTypeToEvent_ShouldMapOptoutVariations() {
        // When & Then
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "PIX_AUTOMATICO_OPTOUT"))
                .isEqualTo(PixAutomaticoEventEnum.OPTOUT);
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "OPTOUT"))
                .isEqualTo(PixAutomaticoEventEnum.OPTOUT);
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "CANCELAMENTO"))
                .isEqualTo(PixAutomaticoEventEnum.OPTOUT);
    }

    @Test
    void mapPaymentTypeToEvent_ShouldMapAgendamentoVariations() {
        // When & Then
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "PIX_AUTOMATICO_AGENDAMENTO"))
                .isEqualTo(PixAutomaticoEventEnum.AGENDAMENTO);
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "AGENDAMENTO"))
                .isEqualTo(PixAutomaticoEventEnum.AGENDAMENTO);
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "RECORRENCIA"))
                .isEqualTo(PixAutomaticoEventEnum.AGENDAMENTO);
    }

    @Test
    void mapPaymentTypeToEvent_ShouldMapPagamentoVariations() {
        // When & Then
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "PIX_AUTOMATICO_PAGAMENTO"))
                .isEqualTo(PixAutomaticoEventEnum.PAGAMENTO);
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "PAGAMENTO"))
                .isEqualTo(PixAutomaticoEventEnum.PAGAMENTO);
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "EXECUTADO"))
                .isEqualTo(PixAutomaticoEventEnum.PAGAMENTO);
    }

    @Test
    void mapPaymentTypeToEvent_ShouldReturnNullForPix() {
        // When & Then - PIX não é mais mapeado como evento PIX Automático
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "PIX"))
                .isNull();
    }

    @Test
    void mapPaymentTypeToEvent_ShouldReturnNullForUnknownType() {
        // When & Then
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "UNKNOWN_TYPE"))
                .isNull();
    }

    @Test
    void mapPaymentTypeToEvent_ShouldReturnNullForNullType() {
        // When & Then
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, null))
                .isNull();
    }

    @Test
    void mapPaymentTypeToEvent_ShouldBeCaseInsensitive() {
        // When & Then
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "pix_automatico"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
        assertThat(pixEventMappingService.mapPaymentTypeToEvent(txId, "Optin"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
    }

    @Test
    void isPixAutomaticoEvent_ShouldReturnTrueForValidEvents() {
        // When & Then
        assertThat(pixEventMappingService.isPixAutomaticoEvent(txId, "PIX_AUTOMATICO")).isTrue();
        assertThat(pixEventMappingService.isPixAutomaticoEvent(txId, "OPTIN")).isTrue();
        assertThat(pixEventMappingService.isPixAutomaticoEvent(txId, "PAGAMENTO")).isTrue();
    }

    @Test
    void isPixAutomaticoEvent_ShouldReturnFalseForInvalidEvents() {
        // When & Then
        assertThat(pixEventMappingService.isPixAutomaticoEvent(txId, "UNKNOWN_TYPE")).isFalse();
        assertThat(pixEventMappingService.isPixAutomaticoEvent(txId, null)).isFalse();
    }

        @Disabled
    @Test
    void shouldNotify_ShouldReturnTrueForNotifiableEvents() {
        // When & Then
        assertThat(pixEventMappingService.shouldNotify(txId, "PAYMENT")).isTrue();
        assertThat(pixEventMappingService.shouldNotify(txId, "PAGAMENTO")).isTrue();
        assertThat(pixEventMappingService.shouldNotify(txId, "CHANGE_PAYMENT_METHOD")).isTrue();
        assertThat(pixEventMappingService.shouldNotify(txId, "ALTERACAO")).isTrue();
        assertThat(pixEventMappingService.shouldNotify(txId, "CHARGE")).isTrue();
        assertThat(pixEventMappingService.shouldNotify(txId, "COBRANCA")).isTrue();
        assertThat(pixEventMappingService.shouldNotify(txId, "OPT_IN")).isTrue();
        assertThat(pixEventMappingService.shouldNotify(txId, "OPT_OUT")).isTrue();
        assertThat(pixEventMappingService.shouldNotify(txId, "AGENDAMENTO")).isTrue();
    }

    @Test
    void shouldNotify_ShouldReturnFalseForNonNotifiableEvents() {
        // When & Then
        assertThat(pixEventMappingService.shouldNotify(txId, "NON_NOTIFIABLE")).isFalse();
        assertThat(pixEventMappingService.shouldNotify(txId, null)).isFalse();
    }

        @Disabled
    @Test
    void shouldNotify_ShouldBeCaseInsensitive() {
        // When & Then
        assertThat(pixEventMappingService.shouldNotify(txId, "payment")).isTrue();
        assertThat(pixEventMappingService.shouldNotify(txId, "Payment")).isTrue();
    }

    @Test
    void mapEventTypeToEnum_ShouldMapBasicEvents() {
        // When & Then
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHANGE_PAYMENT_METHOD"))
                .isEqualTo(PixAutomaticoEventEnum.ALTERACAO);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE"))
                .isEqualTo(PixAutomaticoEventEnum.COBRANCA);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "PAYMENT"))
                .isEqualTo(PixAutomaticoEventEnum.PAGAMENTO);
    }

    @Test
    void mapEventTypeToEnum_ShouldReturnNullForNullEventType() {
        // When & Then
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, null))
                .isNull();
    }

        @Disabled
    @Test
    void mapEventTypeToEnum_WithStatusAndPaymentMethod_ShouldMapChargeEvents() {
        // When & Then - CRIADA/CREATED status
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "CRIADA", "PIX_AUTOMATICO"))
                .isEqualTo(PixAutomaticoEventEnum.AGENDAMENTO);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "CREATED", "REGULAR"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);

        // CONCLUIDA/PAID status
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "CONCLUIDA", "PIX_AUTOMATICO"))
                .isEqualTo(PixAutomaticoEventEnum.PAGAMENTO);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "PAID", "REGULAR"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);

        // CANCELADA status
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "CANCELADA", "PIX_AUTOMATICO"))
                .isEqualTo(PixAutomaticoEventEnum.FALHA_AGENDAMENTO);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "EXPIRADA", "REGULAR"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);

        // ATIVA status
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "ATIVA", "ANY"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);

        // FALHA status
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "FALHA", "ANY"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
    }

    @Test
    void mapEventTypeToEnum_WithStatusAndPaymentMethod_ShouldMapChangePaymentMethodEvents() {
        // When & Then
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHANGE_PAYMENT_METHOD", "CANCELADA", "ANY"))
                .isEqualTo(PixAutomaticoEventEnum.OPTOUT);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHANGE_PAYMENT_METHOD", "ATIVA", "ANY"))
                .isEqualTo(PixAutomaticoEventEnum.ALTERACAO);
    }

    @Test
    void mapEventTypeToEnum_WithRecurrenceId_ShouldMapChangePaymentMethodEvents() {
        // When & Then - With recurrenceId
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHANGE_PAYMENT_METHOD", "ATIVA", "ANY", "recurrence-123"))
                .isEqualTo(PixAutomaticoEventEnum.INCENTIVO_ADESAO);

        // Without recurrenceId
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHANGE_PAYMENT_METHOD", "ATIVA", "ANY", null))
                .isEqualTo(PixAutomaticoEventEnum.ALTERACAO);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHANGE_PAYMENT_METHOD", "ATIVA", "ANY", ""))
                .isEqualTo(PixAutomaticoEventEnum.ALTERACAO);

        // CANCELADA status should still return OPTOUT regardless of recurrenceId
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHANGE_PAYMENT_METHOD", "CANCELADA", "ANY", "recurrence-123"))
                .isEqualTo(PixAutomaticoEventEnum.OPTOUT);
    }

    @Test
    void getTemplateCodeForEventType_ShouldReturnCorrectTemplates() {
        // When & Then
        assertThat(pixEventMappingService.getTemplateCodeForEventType(txId, PixAutomaticoEventEnum.PAGAMENTO))
                .isEqualTo("PAYMENT_TEMPLATE");
        assertThat(pixEventMappingService.getTemplateCodeForEventType(txId, PixAutomaticoEventEnum.ALTERACAO))
                .isEqualTo("ALTERACAO_TEMPLATE");
        assertThat(pixEventMappingService.getTemplateCodeForEventType(txId, PixAutomaticoEventEnum.COBRANCA))
                .isEqualTo("COBRANCA_TEMPLATE");
        assertThat(pixEventMappingService.getTemplateCodeForEventType(txId, PixAutomaticoEventEnum.OPTIN))
                .isEqualTo("ALTERACAO_TEMPLATE");
        assertThat(pixEventMappingService.getTemplateCodeForEventType(txId, PixAutomaticoEventEnum.OPTOUT))
                .isEqualTo("ALTERACAO_TEMPLATE");
        assertThat(pixEventMappingService.getTemplateCodeForEventType(txId, PixAutomaticoEventEnum.AGENDAMENTO))
                .isEqualTo("COBRANCA_TEMPLATE");
        assertThat(pixEventMappingService.getTemplateCodeForEventType(txId, PixAutomaticoEventEnum.FALHA_AGENDAMENTO))
                .isEqualTo("FALHA_AGENDAMENTO_TEMPLATE");
        assertThat(pixEventMappingService.getTemplateCodeForEventType(txId, PixAutomaticoEventEnum.INCENTIVO_ADESAO))
                .isEqualTo("INCENTIVO_ADESAO_TEMPLATE");
    }

    @Test
    void getTemplateCodeForEventType_ShouldReturnDefaultForNull() {
        // When & Then
        assertThat(pixEventMappingService.getTemplateCodeForEventType(txId, null))
                .isEqualTo("PAYMENT_TEMPLATE");
    }

    @Test
    void mapChargeEvent_ShouldHandleNullStatus() {
        // When & Then
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", null, "ANY"))
                .isEqualTo(PixAutomaticoEventEnum.COBRANCA);
    }

        @Disabled
    @Test
    void mapChargeEvent_ShouldHandleAllStatusVariations() {
        // When & Then - Test all status variations  
        // CRIADA e CREATED status mapeiam para AGENDAMENTO quando PIX_AUTOMATICO
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "CRIADA", "PIX_AUTOMATICO"))
                .isEqualTo(PixAutomaticoEventEnum.AGENDAMENTO);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "CREATED", "PIX_AUTOMATICO"))
                .isEqualTo(PixAutomaticoEventEnum.AGENDAMENTO);
        
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "CONCLUIDO", "PIX_AUTOMATICO"))
                .isEqualTo(PixAutomaticoEventEnum.PAGAMENTO);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "PAGA", "PIX_AUTOMATICO"))
                .isEqualTo(PixAutomaticoEventEnum.PAGAMENTO);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "EXECUTADA", "PIX_AUTOMATICO"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "EXECUTED", "PIX_AUTOMATICO"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
        
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "REJEITADA", "PIX_AUTOMATICO"))
                .isEqualTo(PixAutomaticoEventEnum.FALHA_AGENDAMENTO);
        
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "FAILED", "ANY"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "ERRO", "ANY"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "ERROR", "ANY"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
        
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "ACTIVE", "ANY"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
        
        assertThat(pixEventMappingService.mapEventTypeToEnum(txId, "CHARGE", "UNKNOWN_STATUS", "ANY"))
                .isEqualTo(PixAutomaticoEventEnum.OPTIN);
    }
}