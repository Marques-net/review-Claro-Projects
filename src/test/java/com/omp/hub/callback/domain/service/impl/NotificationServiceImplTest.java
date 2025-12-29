package com.omp.hub.callback.domain.service.impl;

import com.omp.hub.callback.domain.service.impl.notification.impl.NotificationServiceImpl;
import com.omp.hub.callback.application.utils.apigee.ApigeeHeaderService;
import com.omp.hub.callback.domain.enums.PixAutomaticoEventEnum;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationErrorDTO;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageRequest;
import com.omp.hub.callback.domain.model.dto.communication.CommunicationMessageResponse;
import com.omp.hub.callback.domain.ports.client.CommunicationPort;
import okhttp3.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceImplTest {

    @Mock
    private CommunicationPort communicationPort;

    @Mock
    private ApigeeHeaderService apigeeHeaderService;

    @InjectMocks
    private NotificationServiceImpl service;

    @Captor
    private ArgumentCaptor<CommunicationMessageRequest> requestCaptor;

    private UUID uuid;
    private String txId;
    private String name;
    private String msisdn;
    private String email;
    private Headers.Builder headersBuilder;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        txId = "tx-123";
        name = "João Silva";
        msisdn = "5511999999999";
        email = "joao@example.com";
        headersBuilder = new Headers.Builder();

        // Set up configuration values
        ReflectionTestUtils.setField(service, "campaign", "OS74747");
        ReflectionTestUtils.setField(service, "templateCodeOptin", "OPTIN001");
        ReflectionTestUtils.setField(service, "templateCodeOptout", "OPTOUT001");
        ReflectionTestUtils.setField(service, "templateCodeAgendamento", "AGEND001");
        ReflectionTestUtils.setField(service, "templateCodePagamento", "PAG001");
        ReflectionTestUtils.setField(service, "templateCodeFalhaAgendamento", "FALHA001");
        ReflectionTestUtils.setField(service, "templateCodeIncentivoAdesao", "INCENT001");
        ReflectionTestUtils.setField(service, "messageOptin", "Mensagem OPTIN");
        ReflectionTestUtils.setField(service, "messageOptout", "Mensagem OPTOUT");
        ReflectionTestUtils.setField(service, "messageAgendamento", "Mensagem AGENDAMENTO");
        ReflectionTestUtils.setField(service, "messagePagamento", "Mensagem PAGAMENTO");
        ReflectionTestUtils.setField(service, "messagePagamentoAvulso", "Mensagem PAGAMENTO AVULSO");
        ReflectionTestUtils.setField(service, "messageAlteracao", "Mensagem ALTERACAO");
        ReflectionTestUtils.setField(service, "messageCobranca", "Mensagem COBRANCA");
        ReflectionTestUtils.setField(service, "messageFalhaAgendamento", "Mensagem FALHA");
        ReflectionTestUtils.setField(service, "messageIncentivoAdesao", "Mensagem INCENTIVO");

        // Inject the mock manually since @Autowired doesn't work in tests
        ReflectionTestUtils.setField(service, "apigeeHeaderService", apigeeHeaderService);

        lenient().when(apigeeHeaderService.generateHeaderApigee(any(UUID.class))).thenReturn(headersBuilder);
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithValidMsisdnAndEmail_ShouldSendBothNotifications() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder()
                .error(null)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, msisdn, email);

        // Then - should call twice (SMS + Email)
        verify(communicationPort, times(2)).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));

        var requests = requestCaptor.getAllValues();
        // First call is SMS (channel 1)
        assertEquals("1", requests.get(0).getData().getChannel());
        assertEquals(msisdn, requests.get(0).getData().getDestination());
        // Second call is Email (channel 2)
        assertEquals("2", requests.get(1).getData().getChannel());
        assertEquals(email, requests.get(1).getData().getDestination());
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithOnlyMsisdn_ShouldSendOnlySms() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder()
                .error(null)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, msisdn, null);

        // Then
        verify(communicationPort, times(1)).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("1", requestCaptor.getValue().getData().getChannel());
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithOnlyEmail_ShouldSendOnlyEmail() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder()
                .error(null)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, null, email);

        // Then
        verify(communicationPort, times(1)).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("2", requestCaptor.getValue().getData().getChannel());
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithEmptyMsisdn_ShouldSendOnlyEmail() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder()
                .error(null)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, "", email);

        // Then
        verify(communicationPort, times(1)).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("2", requestCaptor.getValue().getData().getChannel());
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithEmptyEmail_ShouldSendOnlySms() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder()
                .error(null)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, msisdn, "");

        // Then
        verify(communicationPort, times(1)).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("1", requestCaptor.getValue().getData().getChannel());
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithNullMsisdnAndEmail_ShouldNotSendAny() {
        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, null, null);

        // Then
        verify(communicationPort, never()).sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class));
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithEmptyMsisdnAndEmail_ShouldNotSendAny() {
        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, "", "");

        // Then
        verify(communicationPort, never()).sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class));
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithCommunicationError_ShouldHandleException() {
        // Given
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenThrow(new RuntimeException("Communication error"));

        // When & Then
        assertDoesNotThrow(() -> service.sendPixAutomaticoNotificationWithCustomerData(
                uuid, txId, PixAutomaticoEventEnum.OPTIN, name, msisdn, email));
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithErrorResponse_ShouldLogError() {
        // Given
        CommunicationErrorDTO error = CommunicationErrorDTO.builder()
                .message("Communication failed")
                .build();

        CommunicationMessageResponse errorResponse = CommunicationMessageResponse.builder()
                .error(error)
                .build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(errorResponse);

        assertDoesNotThrow(() -> {
            service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, msisdn, email);
        });
    }

    @Test
    void sendPixAutomaticoNotificationWithCustomerData_WithNullResponse_ShouldLogError() {
        // Given
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(null);

        assertDoesNotThrow(() -> {
            service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, msisdn, email);
        });
    }

    // Test all event types for template and message selection
    @Test
    void sendPixAutomaticoNotification_WithOptinEvent_ShouldUseOptinTemplateAndMessage() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, msisdn, null);

        // Then
        verify(communicationPort).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("OPTIN001", requestCaptor.getValue().getData().getTemplateCode());
        assertEquals("Mensagem OPTIN", requestCaptor.getValue().getData().getMessage());
    }

    @Test
    void sendPixAutomaticoNotification_WithOptoutEvent_ShouldUseOptoutTemplateAndMessage() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTOUT, name, msisdn, null);

        // Then
        verify(communicationPort).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("OPTOUT001", requestCaptor.getValue().getData().getTemplateCode());
        assertEquals("Mensagem OPTOUT", requestCaptor.getValue().getData().getMessage());
    }

    //@Test
    @Disabled
    void sendPixAutomaticoNotification_WithAgendamentoEvent_ShouldUseAgendamentoTemplateAndMessage() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.AGENDAMENTO, name, msisdn, null);

        // Then
        verify(communicationPort).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("AGEND001", requestCaptor.getValue().getData().getTemplateCode());
        assertEquals("Mensagem AGENDAMENTO", requestCaptor.getValue().getData().getMessage());
    }

    //@Test
    @Disabled
    void sendPixAutomaticoNotification_WithPagamentoEvent_ShouldUsePagamentoTemplateAndMessage() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.PAGAMENTO, name, msisdn, null);

        // Then
        verify(communicationPort).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("PAG001", requestCaptor.getValue().getData().getTemplateCode());
        assertEquals("Mensagem PAGAMENTO", requestCaptor.getValue().getData().getMessage());
    }

    //@Test
    @Disabled
    void sendPixAutomaticoNotification_WithAlteracaoEvent_ShouldUseOptinTemplateAndAlteracaoMessage() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.ALTERACAO, name, msisdn, null);

        // Then
        verify(communicationPort).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("OPTIN001", requestCaptor.getValue().getData().getTemplateCode()); // Uses OPTIN template
        assertEquals("Mensagem ALTERACAO", requestCaptor.getValue().getData().getMessage());
    }

    //@Test
    @Disabled
    void sendPixAutomaticoNotification_WithCobrancaEvent_ShouldUseAgendamentoTemplateAndCobrancaMessage() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.COBRANCA, name, msisdn, null);

        // Then
        verify(communicationPort).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("AGEND001", requestCaptor.getValue().getData().getTemplateCode()); // Uses AGENDAMENTO template
        assertEquals("Mensagem COBRANCA", requestCaptor.getValue().getData().getMessage());
    }

    @Test
    void sendPixAutomaticoNotification_WithFalhaAgendamentoEvent_ShouldUseFalhaTemplateAndMessage() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.FALHA_AGENDAMENTO, name, msisdn, null);

        // Then
        verify(communicationPort).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("FALHA001", requestCaptor.getValue().getData().getTemplateCode());
        assertEquals("Mensagem FALHA", requestCaptor.getValue().getData().getMessage());
    }

    @Test
    void sendPixAutomaticoNotification_WithIncentivoAdesaoEvent_ShouldUseIncentivoTemplateAndMessage() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.INCENTIVO_ADESAO, name, msisdn, null);

        // Then
        verify(communicationPort).sendMessage(any(UUID.class), requestCaptor.capture(), any(Headers.Builder.class));
        assertEquals("INCENT001", requestCaptor.getValue().getData().getTemplateCode());
        assertEquals("Mensagem INCENTIVO", requestCaptor.getValue().getData().getMessage());
    }

    // Parameterized test to verify all event types work without exception
    @ParameterizedTest
    @EnumSource(PixAutomaticoEventEnum.class)
    void sendPixAutomaticoNotification_AllEventTypes_ShouldNotThrowException(PixAutomaticoEventEnum eventType) {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When & Then
        assertDoesNotThrow(() -> service.sendPixAutomaticoNotificationWithCustomerData(
                uuid, txId, eventType, name, msisdn, email));
    }

    @Test
    void sendPixAutomaticoNotification_VerifyRequestStructure() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, msisdn, null);

        // Then
        verify(communicationPort).sendMessage(eq(uuid), requestCaptor.capture(), any(Headers.Builder.class));

        var request = requestCaptor.getValue();
        assertNotNull(request.getData());
        assertEquals("", request.getData().getLayout());
        assertEquals("", request.getData().getCustomization());
        assertEquals("", request.getData().getValidator());
        assertEquals("Comunic cadastro/movimentações - Pix Automático", request.getData().getProject());
        assertEquals("OS74747", request.getData().getCampaign());
        assertEquals("", request.getData().getMobileClient());
        assertEquals(msisdn + ";" + name, request.getData().getTemplateData());
    }

    @Test
    void sendPixAutomaticoNotification_VerifyEmailRequestStructure() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();
        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(successResponse);

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, null, email);

        // Then
        verify(communicationPort).sendMessage(eq(uuid), requestCaptor.capture(), any(Headers.Builder.class));

        var request = requestCaptor.getValue();
        assertEquals("2", request.getData().getChannel());
        assertEquals(email, request.getData().getDestination());
        assertEquals(email + ";" + name, request.getData().getTemplateData());
    }

    @Test
    void sendPixAutomaticoNotification_SmsWithErrorResponse_ShouldContinueToEmail() {
        // Given
        CommunicationErrorDTO error = CommunicationErrorDTO.builder().message("SMS failed").build();
        CommunicationMessageResponse errorResponse = CommunicationMessageResponse.builder().error(error).build();
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenReturn(errorResponse)   // First call (SMS) fails
                .thenReturn(successResponse); // Second call (Email) succeeds

        // When
        service.sendPixAutomaticoNotificationWithCustomerData(uuid, txId, PixAutomaticoEventEnum.OPTIN, name, msisdn, email);

        // Then - both should be called even if first fails
        verify(communicationPort, times(2)).sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class));
    }

    @Test
    void sendPixAutomaticoNotification_SmsThrowsException_ShouldContinueToEmail() {
        // Given
        CommunicationMessageResponse successResponse = CommunicationMessageResponse.builder().error(null).build();

        when(communicationPort.sendMessage(any(UUID.class), any(CommunicationMessageRequest.class), any(Headers.Builder.class)))
                .thenThrow(new RuntimeException("SMS failed"))   // First call (SMS) throws exception
                .thenReturn(successResponse); // Second call (Email) succeeds

        // When - should not throw even though SMS failed
        assertDoesNotThrow(() -> service.sendPixAutomaticoNotificationWithCustomerData(
                uuid, txId, PixAutomaticoEventEnum.OPTIN, name, msisdn, email));
    }
}
