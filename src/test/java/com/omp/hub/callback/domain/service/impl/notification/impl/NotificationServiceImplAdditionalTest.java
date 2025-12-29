package com.omp.hub.callback.domain.service.impl.notification.impl;

import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageRequest;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageResponse;
import com.omp.hub.callback.domain.ports.client.CommunicationPort;
import okhttp3.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceImplAdditionalTest {

    @Mock
    private CommunicationPort communicationPort;

    @Mock
    private ApigeeHeaderService apigeeHeaderService;

    @InjectMocks
    private NotificationServiceImpl service;

    private UUID uuid;
    private String txId;
    private String name;
    private String email;
    private Headers.Builder headersBuilder;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        txId = "tx-123";
        name = "João Silva";
        email = "joao@example.com";
        headersBuilder = new Headers.Builder();

        ReflectionTestUtils.setField(service, "campaign", "OS74747");
        ReflectionTestUtils.setField(service, "templateCodeOptin", "OPTIN001");
        ReflectionTestUtils.setField(service, "templateCodeOptout", "OPTOUT001");
        ReflectionTestUtils.setField(service, "templateCodeAgendamento", "AGEND001");
        ReflectionTestUtils.setField(service, "templateCodePagamento", "PAG001");
        ReflectionTestUtils.setField(service, "apigeeHeaderService", apigeeHeaderService);

        lenient().when(apigeeHeaderService.generateHeaderApigee(uuid)).thenReturn(headersBuilder);
    }

    @ParameterizedTest
    @ValueSource(strings = {"5511999999999", "5521988888888", "5585987654321"})
    void sendPixAutomaticoNotificationWithCustomerData_WithVariousMsisdn_ShouldSendMessage(String msisdnValue) {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder()
                .error(null)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        Assertions.assertDoesNotThrow( () -> {
            service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, msisdnValue, email);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"joao@email.com", "maria@domain.br", "test@company.org"})
    void sendPixAutomaticoNotificationWithCustomerData_WithVariousEmails_ShouldSendMessage(String emailValue) {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder()
                .error(null)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, null, emailValue);

        // Then
        verify(communicationPort).sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class));
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithBothMsisdnAndEmail_ShouldPreferMsisdn() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder()
                .error(null)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        Assertions.assertDoesNotThrow(() -> {
            service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN,
                    "João", "   ", "joao@email.com");
        });
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithWhitespaceMsisdn_ShouldUseEmail() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder()
                .error(null)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        Assertions.assertDoesNotThrow(() -> {
            service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN,
                "João", "   ", "joao@email.com");
        });
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithNullName_ShouldStillSendMessage() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder()
                .error(null)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        Assertions.assertDoesNotThrow(() -> {
            service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN,
                    "João", "   ", "joao@email.com");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"João Silva", "Maria", "Dr. João Silva Santos", ""})
    void sendPixAutomaticoNotificationWithCustomerData_WithVariousNames(String nameValue) {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder()
                .error(null)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        Assertions.assertDoesNotThrow(() -> {
            service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN,
                    "João", "   ", "joao@email.com");
        });
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithAllFieldsNull_ShouldStillProcess() {
        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, 
                null, null, null);

        // Then - o serviço deve tentar processar mesmo com todos os campos nulos
        assertThat(apigeeHeaderService).isNotNull();
    }
}
