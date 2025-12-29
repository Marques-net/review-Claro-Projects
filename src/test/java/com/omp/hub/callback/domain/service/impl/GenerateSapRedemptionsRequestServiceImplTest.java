package com.omp.hub.callback.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.DataSingleDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.PaymentSingleDTO;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.SapRedemptionsRequest;
import com.omp.hub.callback.domain.service.generate.impl.GenerateSapRedemptionsRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateSapRedemptionsRequestServiceImplTest {

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private GenerateSapRedemptionsRequestServiceImpl service;

    private InformationPaymentDTO informationPayment;
    private DataSingleDTO dataSingle;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "atvSimplLoja", "123");

        informationPayment = new InformationPaymentDTO();
        informationPayment.setUuid(UUID.randomUUID());
        informationPayment.setIdentifier("TEST-123");
        informationPayment.setStore("001");
        informationPayment.setChannel("test-channel");

        PaymentSingleDTO payment = PaymentSingleDTO.builder()
                .salesOrderId("SALES-123")
                .build();

        dataSingle = new DataSingleDTO();
        dataSingle.setPayment(payment);

        PaymentDTO paymentInfo = PaymentDTO.builder()
                .journey("{\"payment\":{\"salesOrderId\":\"SALES-123\"}}")
                .build();
        informationPayment.setPayments(List.of(paymentInfo));
    }

    @Test
    void generateRequest_WithValidData_ShouldReturnSapRedemptionsRequest() throws JsonProcessingException {
        // Given
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingle);
        lenient().when(mapper.writeValueAsString(any())).thenReturn("{}");

        // When
        SapRedemptionsRequest result = service.generateRequest(informationPayment);

        // Then
        assertNotNull(result);
        assertNotNull(result.getData());
        assertNotNull(result.getData().getOrder());
        assertEquals("SALES-123", result.getData().getOrder().getId());
        assertEquals("001", result.getData().getOrder().getCompanyId());
        assertEquals("001", result.getData().getOrder().getBusinessLocationId());
        assertEquals("R", result.getData().getOrder().getType());
        assertNotNull(result.getData().getOrder().getPosInfo());
        assertEquals("4000", result.getData().getOrder().getPosInfo().getComponentNumber());
    }

    @Test
    void generateRequest_WithAtivacaoSimplificadaChannel_ShouldUseAtvSimplLoja() throws JsonProcessingException {
        // Given
        informationPayment.setStore("DEFAULT");
        informationPayment.setChannel("ativacaosimplificada");
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingle);
        lenient().when(mapper.writeValueAsString(any())).thenReturn("{}");

        // When
        SapRedemptionsRequest result = service.generateRequest(informationPayment);

        // Then
        assertEquals("123", result.getData().getOrder().getBusinessLocationId());
    }

    @Test
    void generateRequest_WithSolarChannel_ShouldUseAtvSimplLoja() throws JsonProcessingException {
        // Given
        informationPayment.setStore(null);
        informationPayment.setChannel("solar");
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingle);
        lenient().when(mapper.writeValueAsString(any())).thenReturn("{}");

        // When
        SapRedemptionsRequest result = service.generateRequest(informationPayment);

        // Then
        assertEquals("123", result.getData().getOrder().getBusinessLocationId());
    }

    @Test
    void generateRequest_WithNullStore_AndOtherChannel_ShouldUseNullStore() throws JsonProcessingException {
        // Given
        informationPayment.setStore(null);
        informationPayment.setChannel("other-channel");
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingle);
        lenient().when(mapper.writeValueAsString(any())).thenReturn("{}");

        // When
        SapRedemptionsRequest result = service.generateRequest(informationPayment);

        // Then
        assertNull(result.getData().getOrder().getBusinessLocationId());
    }

    @Test
    void generateRequest_WhenJsonProcessingFails_ShouldThrowBusinessException() throws JsonProcessingException {
        // Given
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.generateRequest(informationPayment));
        assertEquals("Erro convert Json", exception.getError().getMessage());
        assertEquals("ERROR_CONVERT_JSON", exception.getError().getErrorCode());
    }

    @Test
    void generateRequest_WithNullPayments_ShouldThrowBusinessException() {
        // Given
        informationPayment.setPayments(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.generateRequest(informationPayment));
        assertEquals("Objeto não encontrado", exception.getError().getMessage());
        assertEquals("ERROR_OBJECT_NOT_FOUND", exception.getError().getErrorCode());
    }

    @Test
    void generateRequest_WithEmptyPayments_ShouldThrowBusinessException() {
        // Given
        informationPayment.setPayments(List.of());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.generateRequest(informationPayment));
        assertEquals("Objeto não encontrado", exception.getError().getMessage());
    }

    @Test
    void generateRequest_WithNullSalesOrderId_ShouldUseIdentifier() throws JsonProcessingException {
        // Given
        dataSingle.getPayment().setSalesOrderId(null);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingle);
        lenient().when(mapper.writeValueAsString(any())).thenReturn("{}");

        // When
        SapRedemptionsRequest result = service.generateRequest(informationPayment);

        // Then
        assertEquals(informationPayment.getIdentifier(), result.getData().getOrder().getId());
    }

    @Test
    void generateRequest_WithNullPayment_ShouldUseIdentifier() throws JsonProcessingException {
        // Given
        dataSingle.setPayment(null);
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingle);
        lenient().when(mapper.writeValueAsString(any())).thenReturn("{}");

        // When
        SapRedemptionsRequest result = service.generateRequest(informationPayment);

        // Then
        assertEquals(informationPayment.getIdentifier(), result.getData().getOrder().getId());
    }

    @Test
    void generateRequest_WithNullJourney_ShouldThrowBusinessException() {
        // Given
        PaymentDTO paymentInfo = PaymentDTO.builder()
                .journey(null)
                .build();
        informationPayment.setPayments(List.of(paymentInfo));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.generateRequest(informationPayment));
        assertEquals("Objeto não encontrado", exception.getError().getMessage());
    }

    @Test
    void generateRequest_WithWrappedJsonJourney_ShouldUnwrapAndParse() throws JsonProcessingException {
        // Given
        String wrappedJourney = "\"\\\"payment\\\":{\\\"salesOrderId\\\":\\\"SALES-123\\\"}\"";
        PaymentDTO paymentInfo = PaymentDTO.builder()
                .journey(wrappedJourney)
                .build();
        informationPayment.setPayments(List.of(paymentInfo));
        
        when(mapper.readValue(eq(wrappedJourney), eq(String.class))).thenReturn("{\"payment\":{\"salesOrderId\":\"SALES-123\"}}");
        when(mapper.readValue(eq("{\"payment\":{\"salesOrderId\":\"SALES-123\"}}"), eq(DataSingleDTO.class))).thenReturn(dataSingle);
        lenient().when(mapper.writeValueAsString(any())).thenReturn("{}");

        // When
        SapRedemptionsRequest result = service.generateRequest(informationPayment);

        // Then
        assertNotNull(result);
        assertEquals("SALES-123", result.getData().getOrder().getId());
    }

    @Test
    void generateRequest_WithSalesOrderIdWithLeadingZeros_ShouldRemoveLeadingZeros() throws JsonProcessingException {
        // Given
        PaymentSingleDTO payment = PaymentSingleDTO.builder()
                .salesOrderId("SV009178457785H1")
                .build();
        dataSingle.setPayment(payment);
        
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingle);
        lenient().when(mapper.writeValueAsString(any())).thenReturn("{}");

        // When
        SapRedemptionsRequest result = service.generateRequest(informationPayment);

        // Then
        assertNotNull(result);
        assertEquals("SV009178457785H1", result.getData().getOrder().getId());
    }

    @Test
    void generateRequest_WithSalesOrderIdWithMultipleLeadingZeros_ShouldRemoveUntil10Digits() throws JsonProcessingException {
        // Given
        PaymentSingleDTO payment = PaymentSingleDTO.builder()
                .salesOrderId("SV0000000000012345H1")
                .build();
        dataSingle.setPayment(payment);
        
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingle);
        lenient().when(mapper.writeValueAsString(any())).thenReturn("{}");

        // When
        SapRedemptionsRequest result = service.generateRequest(informationPayment);

        // Then
        assertNotNull(result);
        assertEquals("SV0000000000012345H1", result.getData().getOrder().getId());
    }

    @Test
    void generateRequest_WithUuidSalesOrderId_ShouldKeepOriginal() throws JsonProcessingException {
        // Given
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        PaymentSingleDTO payment = PaymentSingleDTO.builder()
                .salesOrderId(uuid)
                .build();
        dataSingle.setPayment(payment);
        
        when(mapper.readValue(anyString(), eq(DataSingleDTO.class))).thenReturn(dataSingle);
        lenient().when(mapper.writeValueAsString(any())).thenReturn("{}");

        // When
        SapRedemptionsRequest result = service.generateRequest(informationPayment);

        // Then
        assertNotNull(result);
        assertEquals(uuid, result.getData().getOrder().getId());
    }
}
