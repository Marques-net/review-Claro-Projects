package com.omp.hub.callback.domain.model.dto.communication;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class CommunicationDataDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void communicationDataDTO_ShouldSerializeAndDeserialize() throws Exception {
        // Arrange
        CommunicationDataDTO data = CommunicationDataDTO.builder()
                .layout("default-layout")
                .customization("custom-style")
                .validator("email-validator")
                .templateData("user@example.com;João Silva")
                .destination("user@example.com")
                .channel("2")
                .project("PIX Automático")
                .campaign("CAMP-2024")
                .mobileClient("mobile-app")
                .templateCode("TMPL-001")
                .message("Bem-vindo ao PIX Automático")
                .build();

        // Act
        String json = objectMapper.writeValueAsString(data);
        CommunicationDataDTO deserialized = objectMapper.readValue(json, CommunicationDataDTO.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getLayout()).isEqualTo("default-layout");
        assertThat(deserialized.getDestination()).isEqualTo("user@example.com");
        assertThat(deserialized.getChannel()).isEqualTo("2");
        assertThat(deserialized.getProject()).isEqualTo("PIX Automático");
        assertThat(deserialized.getMessage()).isEqualTo("Bem-vindo ao PIX Automático");
    }

    @Test
    void communicationDataDTO_WithNullFields_ShouldNotIncludeInJson() throws Exception {
        // Arrange
        CommunicationDataDTO data = CommunicationDataDTO.builder()
                .destination("11999999999")
                .channel("1")
                .project("PIX Automático")
                .build();

        // Act
        String json = objectMapper.writeValueAsString(data);

        // Assert
        assertThat(json).doesNotContain("layout");
        assertThat(json).doesNotContain("customization");
        assertThat(json).doesNotContain("validator");
        assertThat(json).contains("destination");
        assertThat(json).contains("channel");
    }

    @Test
    void communicationDataDTO_ForSms_ShouldUseChannel1() {
        // Act
        CommunicationDataDTO smsData = CommunicationDataDTO.builder()
                .destination("11999999999")
                .channel("1")
                .templateCode("SMS-001")
                .message("Código de verificação: 123456")
                .build();

        // Assert
        assertThat(smsData.getChannel()).isEqualTo("1");
        assertThat(smsData.getDestination()).isEqualTo("11999999999");
    }

    @Test
    void communicationDataDTO_ForEmail_ShouldUseChannel2() {
        // Act
        CommunicationDataDTO emailData = CommunicationDataDTO.builder()
                .destination("user@example.com")
                .channel("2")
                .templateCode("EMAIL-001")
                .message("Bem-vindo!")
                .build();

        // Assert
        assertThat(emailData.getChannel()).isEqualTo("2");
        assertThat(emailData.getDestination()).isEqualTo("user@example.com");
    }

    @Test
    void communicationDataDTO_AllArgsConstructor_ShouldWork() {
        // Act
        CommunicationDataDTO data = new CommunicationDataDTO(
                "layout", "customization", "validator", "templateData",
                "destination", "1", "project", "campaign",
                "mobileClient", "templateCode", "message");

        // Assert
        assertThat(data.getLayout()).isEqualTo("layout");
        assertThat(data.getChannel()).isEqualTo("1");
        assertThat(data.getMessage()).isEqualTo("message");
    }

    @Test
    void communicationDataDTO_NoArgsConstructor_ShouldWork() {
        // Act
        CommunicationDataDTO data = new CommunicationDataDTO();
        data.setDestination("test@example.com");
        data.setChannel("2");
        data.setMessage("Test message");

        // Assert
        assertThat(data.getDestination()).isEqualTo("test@example.com");
        assertThat(data.getChannel()).isEqualTo("2");
        assertThat(data.getMessage()).isEqualTo("Test message");
    }
}
