package com.omp.hub.callback.domain.service.impl.notification.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersContract;
import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersData;
import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersPhone;
import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersResponse;

@ExtendWith(MockitoExtension.class)
class CustomerContactExtractorUtilTest {

    @InjectMocks
    private CustomerContactExtractorUtil customerContactExtractorUtil;

    private CustomerContractsSubscribersResponse response;
    private CustomerContractsSubscribersData data;
    private CustomerContractsSubscribersContract contract;
    private CustomerContractsSubscribersPhone mobilePhone;
    private CustomerContractsSubscribersPhone fixedPhone;

    @BeforeEach
    void setUp() {
        // Setup mobile phone
        mobilePhone = new CustomerContractsSubscribersPhone();
        mobilePhone.setContactMediumRole("Mobile Phone");
        mobilePhone.setTelephoneNumber("11999999999");

        // Setup fixed phone
        fixedPhone = new CustomerContractsSubscribersPhone();
        fixedPhone.setContactMediumRole("Fixed Phone");
        fixedPhone.setTelephoneNumber("1133334444");

        // Setup contract
        contract = new CustomerContractsSubscribersContract();
        contract.setFirstName("João");
        contract.setLastName("Silva");
        contract.setPhones(Arrays.asList(mobilePhone, fixedPhone));

        // Setup data
        data = new CustomerContractsSubscribersData();
        data.setContracts(Arrays.asList(contract));

        // Setup response
        response = new CustomerContractsSubscribersResponse();
        response.setData(data);
    }

    @Test
    void extractMobilePhoneFromContracts_WithValidMobilePhone_ShouldReturnMobileNumber() {
        // When
        String result = customerContactExtractorUtil.extractMobilePhoneFromContracts(response);

        // Then
        assertEquals("11999999999", result);
    }

    @Test
    void extractMobilePhoneFromContracts_WithNullResponse_ShouldReturnNull() {
        // When
        String result = customerContactExtractorUtil.extractMobilePhoneFromContracts(null);

        // Then
        assertNull(result);
    }

    @Test
    void extractMobilePhoneFromContracts_WithNullData_ShouldReturnNull() {
        // Given
        response.setData(null);

        // When
        String result = customerContactExtractorUtil.extractMobilePhoneFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractMobilePhoneFromContracts_WithNullContracts_ShouldReturnNull() {
        // Given
        data.setContracts(null);

        // When
        String result = customerContactExtractorUtil.extractMobilePhoneFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractMobilePhoneFromContracts_WithEmptyContracts_ShouldReturnNull() {
        // Given
        data.setContracts(Collections.emptyList());

        // When
        String result = customerContactExtractorUtil.extractMobilePhoneFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractMobilePhoneFromContracts_WithNullPhones_ShouldReturnNull() {
        // Given
        contract.setPhones(null);

        // When
        String result = customerContactExtractorUtil.extractMobilePhoneFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractMobilePhoneFromContracts_WithEmptyPhones_ShouldReturnNull() {
        // Given
        contract.setPhones(Collections.emptyList());

        // When
        String result = customerContactExtractorUtil.extractMobilePhoneFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractMobilePhoneFromContracts_WithOnlyFixedPhone_ShouldReturnNull() {
        // Given
        contract.setPhones(Arrays.asList(fixedPhone));

        // When
        String result = customerContactExtractorUtil.extractMobilePhoneFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractMobilePhoneFromContracts_WithMultipleContracts_ShouldReturnFirstMobilePhone() {
        // Given
        CustomerContractsSubscribersContract secondContract = new CustomerContractsSubscribersContract();
        CustomerContractsSubscribersPhone secondMobilePhone = new CustomerContractsSubscribersPhone();
        secondMobilePhone.setContactMediumRole("Mobile Phone");
        secondMobilePhone.setTelephoneNumber("11888888888");
        secondContract.setPhones(Arrays.asList(secondMobilePhone));

        data.setContracts(Arrays.asList(contract, secondContract));

        // When
        String result = customerContactExtractorUtil.extractMobilePhoneFromContracts(response);

        // Then
        assertEquals("11999999999", result);
    }

    @Test
    void extractNameFromContracts_WithValidName_ShouldReturnFullName() {
        // When
        String result = customerContactExtractorUtil.extractNameFromContracts(response);

        // Then
        assertEquals("João Silva", result);
    }

    @Test
    void extractNameFromContracts_WithNullResponse_ShouldReturnNull() {
        // When
        String result = customerContactExtractorUtil.extractNameFromContracts(null);

        // Then
        assertNull(result);
    }

    @Test
    void extractNameFromContracts_WithNullData_ShouldReturnNull() {
        // Given
        response.setData(null);

        // When
        String result = customerContactExtractorUtil.extractNameFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractNameFromContracts_WithNullContracts_ShouldReturnNull() {
        // Given
        data.setContracts(null);

        // When
        String result = customerContactExtractorUtil.extractNameFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractNameFromContracts_WithEmptyContracts_ShouldReturnNull() {
        // Given
        data.setContracts(Collections.emptyList());

        // When
        String result = customerContactExtractorUtil.extractNameFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractNameFromContracts_WithNullFirstName_ShouldReturnNull() {
        // Given
        contract.setFirstName(null);

        // When
        String result = customerContactExtractorUtil.extractNameFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractNameFromContracts_WithEmptyFirstName_ShouldReturnNull() {
        // Given
        contract.setFirstName("");

        // When
        String result = customerContactExtractorUtil.extractNameFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractNameFromContracts_WithNullLastName_ShouldReturnNull() {
        // Given
        contract.setLastName(null);

        // When
        String result = customerContactExtractorUtil.extractNameFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractNameFromContracts_WithEmptyLastName_ShouldReturnNull() {
        // Given
        contract.setLastName("");

        // When
        String result = customerContactExtractorUtil.extractNameFromContracts(response);

        // Then
        assertNull(result);
    }

    @Test
    void extractNameFromContracts_WithMultipleContracts_ShouldReturnFirstValidName() {
        // Given
        CustomerContractsSubscribersContract invalidContract = new CustomerContractsSubscribersContract();
        invalidContract.setFirstName(null);
        invalidContract.setLastName("Invalid");

        CustomerContractsSubscribersContract validContract = new CustomerContractsSubscribersContract();
        validContract.setFirstName("Maria");
        validContract.setLastName("Santos");

        data.setContracts(Arrays.asList(invalidContract, validContract));

        // When
        String result = customerContactExtractorUtil.extractNameFromContracts(response);

        // Then
        assertEquals("Maria Santos", result);
    }

    @Test
    void extractNameFromContracts_WithWhitespaceNames_ShouldReturnTrimmedName() {
        // Given
        contract.setFirstName("  João  ");
        contract.setLastName("  Silva  ");

        // When
        String result = customerContactExtractorUtil.extractNameFromContracts(response);

        // Then
        assertEquals("  João     Silva  ", result);
    }
}