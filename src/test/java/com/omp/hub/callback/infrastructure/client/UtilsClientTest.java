package com.omp.hub.callback.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.omp.hub.callback.domain.enums.PaymentStatusEnum;
import com.omp.hub.callback.domain.enums.PaymentTypeEnum;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.DataDTO;
import com.omp.hub.callback.domain.model.dto.journey.HeadersDTO;

@ExtendWith(MockitoExtension.class)
class UtilsClientTest {

    @InjectMocks
    private UtilsClient utilsClient;

    private UUID uuid;
    private DataDTO dataDTO;
    private HeadersDTO headersDTO;
    private String identifier;

    @BeforeEach
    void setUp() {
        uuid = UUID.randomUUID();
        identifier = "TEST_IDENTIFIER";
        
        dataDTO = new DataDTO();
        
        headersDTO = new HeadersDTO();
    }

    @Test
    void getStorePdv_WithStoreAndSalesPoint_ShouldReturnConcatenatedValue() {
        // Arrange
        headersDTO.setStore("STORE01");
        headersDTO.setSalesPoint("PDV01");

        // Act
        String result = utilsClient.getStorePdv(headersDTO);

        // Assert
        assertEquals("STORE01_PDV01", result);
    }

    @Test
    void getStorePdv_WithNullStore_ShouldReturnDefault() {
        // Arrange
        headersDTO.setStore(null);
        headersDTO.setSalesPoint("PDV01");

        // Act
        String result = utilsClient.getStorePdv(headersDTO);

        // Assert
        assertEquals("DEFAULT", result);
    }

    @Test
    void getStorePdv_WithNullSalesPoint_ShouldReturnDefault() {
        // Arrange
        headersDTO.setStore("STORE01");
        headersDTO.setSalesPoint(null);

        // Act
        String result = utilsClient.getStorePdv(headersDTO);

        // Assert
        assertEquals("DEFAULT", result);
    }

    @Test
    void getStorePdv_WithBothNull_ShouldReturnDefault() {
        // Arrange
        headersDTO.setStore(null);
        headersDTO.setSalesPoint(null);

        // Act
        String result = utilsClient.getStorePdv(headersDTO);

        // Assert
        assertEquals("DEFAULT", result);
    }

    @Test
    void getStorePdv_WithEmptyStrings_ShouldReturnConcatenated() {
        // Arrange
        headersDTO.setStore("");
        headersDTO.setSalesPoint("");

        // Act
        String result = utilsClient.getStorePdv(headersDTO);

        // Assert
        assertEquals("_", result);
    }

    @Test
    void generateInfoPayment_WithCompleteHeaders_ShouldReturnCompleteInformationPaymentDTO() {
        // Arrange
        headersDTO.setChannel("MOBILE");
        headersDTO.setStore("STORE01");
        headersDTO.setSalesPoint("PDV01");

        // Act
        InformationPaymentDTO result = utilsClient.generateInfoPayment(uuid, dataDTO, headersDTO, identifier);

        // Assert
        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        assertEquals(identifier, result.getIdentifier());
        assertEquals("MOBILE", result.getChannel());
        assertEquals("STORE01", result.getStore());
        assertEquals("PDV01", result.getPdv());
        assertEquals(PaymentStatusEnum.PENDING, result.getPaymentStatus());
        
        assertNotNull(result.getPayments());
        assertEquals(1, result.getPayments().size());
        assertEquals(PaymentTypeEnum.UNDEFINED, result.getPayments().get(0).getType());
        assertEquals(dataDTO, result.getPayments().get(0).getJourney());
        assertEquals("{}", result.getPayments().get(0).getCallback());
        assertEquals(PaymentStatusEnum.PENDING, result.getPayments().get(0).getPaymentStatus());
    }

    @Test
    void generateInfoPayment_WithNullStoreAndSalesPoint_ShouldUseDefaults() {
        // Arrange
        headersDTO.setChannel("WEB");
        headersDTO.setStore(null);
        headersDTO.setSalesPoint(null);

        // Act
        InformationPaymentDTO result = utilsClient.generateInfoPayment(uuid, dataDTO, headersDTO, identifier);

        // Assert
        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        assertEquals(identifier, result.getIdentifier());
        assertEquals("WEB", result.getChannel());
        assertEquals("DEFAULT", result.getStore());
        assertEquals("DEFAULT", result.getPdv());
        assertEquals(PaymentStatusEnum.PENDING, result.getPaymentStatus());
        
        assertNotNull(result.getPayments());
        assertEquals(1, result.getPayments().size());
        assertEquals(PaymentTypeEnum.UNDEFINED, result.getPayments().get(0).getType());
        assertEquals(dataDTO, result.getPayments().get(0).getJourney());
        assertEquals("{}", result.getPayments().get(0).getCallback());
        assertEquals(PaymentStatusEnum.PENDING, result.getPayments().get(0).getPaymentStatus());
    }

    @Test
    void generateInfoPayment_WithEmptyStoreAndSalesPoint_ShouldUseActualValues() {
        // Arrange
        headersDTO.setChannel("APP");
        headersDTO.setStore("");
        headersDTO.setSalesPoint("");

        // Act
        InformationPaymentDTO result = utilsClient.generateInfoPayment(uuid, dataDTO, headersDTO, identifier);

        // Assert
        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        assertEquals(identifier, result.getIdentifier());
        assertEquals("APP", result.getChannel());
        assertEquals("", result.getStore()); // Empty string is preserved, not replaced with DEFAULT
        assertEquals("", result.getPdv()); // Empty string is preserved, not replaced with DEFAULT
        assertEquals(PaymentStatusEnum.PENDING, result.getPaymentStatus());
    }

    @Test
    void generateInfoPayment_WithNullChannel_ShouldAcceptNull() {
        // Arrange
        headersDTO.setChannel(null);
        headersDTO.setStore("STORE01");
        headersDTO.setSalesPoint("PDV01");

        // Act
        InformationPaymentDTO result = utilsClient.generateInfoPayment(uuid, dataDTO, headersDTO, identifier);

        // Assert
        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        assertEquals(identifier, result.getIdentifier());
        assertEquals(null, result.getChannel());
        assertEquals("STORE01", result.getStore());
        assertEquals("PDV01", result.getPdv());
        assertEquals(PaymentStatusEnum.PENDING, result.getPaymentStatus());
    }

    @Test
    void generateInfoPayment_WithNullDataAndIdentifier_ShouldAcceptNulls() {
        // Arrange
        headersDTO.setChannel("MOBILE");
        headersDTO.setStore("STORE01");
        headersDTO.setSalesPoint("PDV01");

        // Act
        InformationPaymentDTO result = utilsClient.generateInfoPayment(uuid, null, headersDTO, null);

        // Assert
        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        assertEquals(null, result.getIdentifier());
        assertEquals("MOBILE", result.getChannel());
        assertEquals("STORE01", result.getStore());
        assertEquals("PDV01", result.getPdv());
        assertEquals(PaymentStatusEnum.PENDING, result.getPaymentStatus());
        
        assertNotNull(result.getPayments());
        assertEquals(1, result.getPayments().size());
        assertEquals(null, result.getPayments().get(0).getJourney()); // DataDTO is null
    }

    @Test
    void generateInfoPayment_WithComplexScenario_ShouldHandleAllCombinations() {
        // Arrange
        headersDTO.setChannel("KIOSK");
        headersDTO.setStore("MAIN_STORE");
        headersDTO.setSalesPoint("TERMINAL_05");

        // Act
        InformationPaymentDTO result = utilsClient.generateInfoPayment(uuid, dataDTO, headersDTO, "COMPLEX_ID_123");

        // Assert
        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        assertEquals("COMPLEX_ID_123", result.getIdentifier());
        assertEquals("KIOSK", result.getChannel());
        assertEquals("MAIN_STORE", result.getStore());
        assertEquals("TERMINAL_05", result.getPdv());
        assertEquals(PaymentStatusEnum.PENDING, result.getPaymentStatus());
        
        assertNotNull(result.getPayments());
        assertEquals(1, result.getPayments().size());
        assertEquals(PaymentTypeEnum.UNDEFINED, result.getPayments().get(0).getType());
        assertEquals(dataDTO, result.getPayments().get(0).getJourney());
        assertEquals("{}", result.getPayments().get(0).getCallback());
        assertEquals(PaymentStatusEnum.PENDING, result.getPayments().get(0).getPaymentStatus());
    }
}