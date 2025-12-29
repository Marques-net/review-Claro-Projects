package com.omp.hub.callback.domain.model.dto.customer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class SubscriberDTOTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void subscriberDTO_ShouldSerializeAndDeserialize() throws Exception {
        // Given
        AccountDTO account = AccountDTO.builder()
                .mobileBan("BAN-123")
                .build();
        
        CustomerMobileDataDTO customerData = CustomerMobileDataDTO.builder()
                .account(account)
                .build();

        SubscriberDTO subscriber = SubscriberDTO.builder()
                .msisdn("5511999999999")
                .name("Test User")
                .customer(customerData)
                .build();

        // When
        String json = mapper.writeValueAsString(subscriber);
        SubscriberDTO deserialized = mapper.readValue(json, SubscriberDTO.class);

        // Then
        assertNotNull(deserialized);
        assertEquals("5511999999999", deserialized.getMsisdn());
        assertEquals("Test User", deserialized.getName());
        assertNotNull(deserialized.getCustomer());
        assertNotNull(deserialized.getCustomer().getAccount());
        assertEquals("BAN-123", deserialized.getCustomer().getAccount().getMobileBan());
    }

    @Test
    void subscriberDTO_WithNullFields_ShouldNotIncludeInJson() throws Exception {
        // Given
        SubscriberDTO subscriber = SubscriberDTO.builder()
                .msisdn("5511999999999")
                .build();

        // When
        String json = mapper.writeValueAsString(subscriber);

        // Then
        assertFalse(json.contains("\"name\""));
        assertFalse(json.contains("\"customer\""));
    }

    @Test
    void subscriberDTO_AllArgsConstructor_ShouldWork() {
        // Given
        CustomerMobileDataDTO customerData = new CustomerMobileDataDTO();
        
        // When
        SubscriberDTO subscriber = new SubscriberDTO("5511999999999", "Test", customerData);

        // Then
        assertEquals("5511999999999", subscriber.getMsisdn());
        assertEquals("Test", subscriber.getName());
        assertEquals(customerData, subscriber.getCustomer());
    }

    @Test
    void subscriberDTO_NoArgsConstructor_ShouldWork() {
        // When
        SubscriberDTO subscriber = new SubscriberDTO();
        subscriber.setMsisdn("5511999999999");
        subscriber.setName("Test");

        // Then
        assertEquals("5511999999999", subscriber.getMsisdn());
        assertEquals("Test", subscriber.getName());
    }
}
