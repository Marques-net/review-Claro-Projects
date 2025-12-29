package com.omp.hub.callback.domain.model.dto.journey.single;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CashDataDTOTest {

    @Test
    void testCashDataDTOCreation() {
        // Given
        String orderId = "ORD123";
        String orderDate = "2024-01-15";
        String fiscalCouponCode = "FCCC001";

        // When
        CashDataDTO cashData = new CashDataDTO();
        cashData.setFiscalCouponCode(fiscalCouponCode);

        // Then
        assertThat(cashData).isNotNull();
        assertThat(cashData.getFiscalCouponCode()).isEqualTo(fiscalCouponCode);
    }

    @Test
    void testCashDataDTODefaultValues() {
        // When
        CashDataDTO cashData = new CashDataDTO();

        // Then
        assertThat(cashData).isNotNull();
        assertThat(cashData.getFiscalCouponCode()).isNull();
    }

    @Test
    void testCashDataDTOSetters() {
        // Given
        CashDataDTO cashData = new CashDataDTO();

        // When
        cashData.setFiscalCouponCode("FCCC002");

        // Then

        assertThat(cashData.getFiscalCouponCode()).isEqualTo("FCCC002");
    }

    @Test
    void testCashDataDTOEquality() {
        // Given
        CashDataDTO cashData1 = new CashDataDTO();
        cashData1.setFiscalCouponCode("FCCC003");

        CashDataDTO cashData2 = new CashDataDTO();
        cashData2.setFiscalCouponCode("FCCC003");

        // Then
        assertThat(cashData1).isEqualTo(cashData2);
    }

    @Test
    void testCashDataDTOToString() {
        // Given
        CashDataDTO cashData = new CashDataDTO();
        cashData.setFiscalCouponCode("FCCC004");

        // When
        String toString = cashData.toString();

        // Then
        assertThat(toString).isNotEmpty();
        assertThat(toString).contains("CashDataDTO");
    }
}
