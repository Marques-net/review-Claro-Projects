package com.omp.hub.callback.domain.model.dto.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.model.dto.customer.CustomerMobileErrorDTO.ErrorDetailDTO;
import com.omp.hub.callback.domain.model.dto.customer.CustomerMobileErrorDTO.LinkDTO;

class CustomerMobileErrorDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void customerMobileErrorDTO_ShouldSerializeAndDeserialize() throws Exception {
        // Arrange
        LinkDTO link = LinkDTO.builder()
                .rel("help")
                .href("https://example.com/error/help")
                .build();

        ErrorDetailDTO errorDetail = ErrorDetailDTO.builder()
                .httpCode("400")
                .errorCode("ERR-001")
                .message("Invalid request")
                .detailedMessage("The mobile number format is invalid")
                .link(link)
                .build();

        CustomerMobileErrorDTO error = CustomerMobileErrorDTO.builder()
                .apiVersion("1.0")
                .transactionId("TXN-123456")
                .error(errorDetail)
                .build();

        // Act
        String json = objectMapper.writeValueAsString(error);
        CustomerMobileErrorDTO deserialized = objectMapper.readValue(json, CustomerMobileErrorDTO.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getApiVersion()).isEqualTo("1.0");
        assertThat(deserialized.getTransactionId()).isEqualTo("TXN-123456");
        assertThat(deserialized.getError()).isNotNull();
        assertThat(deserialized.getError().getHttpCode()).isEqualTo("400");
        assertThat(deserialized.getError().getErrorCode()).isEqualTo("ERR-001");
        assertThat(deserialized.getError().getMessage()).isEqualTo("Invalid request");
        assertThat(deserialized.getError().getLink()).isNotNull();
        assertThat(deserialized.getError().getLink().getRel()).isEqualTo("help");
    }

    @Test
    void customerMobileErrorDTO_WithNullFields_ShouldNotIncludeInJson() throws Exception {
        // Arrange
        ErrorDetailDTO errorDetail = ErrorDetailDTO.builder()
                .httpCode("500")
                .message("Internal error")
                .build();

        CustomerMobileErrorDTO error = CustomerMobileErrorDTO.builder()
                .error(errorDetail)
                .build();

        // Act
        String json = objectMapper.writeValueAsString(error);

        // Assert
        assertThat(json).doesNotContain("apiVersion");
        assertThat(json).doesNotContain("transactionId");
        assertThat(json).doesNotContain("errorCode");
        assertThat(json).doesNotContain("detailedMessage");
        assertThat(json).doesNotContain("link");
        assertThat(json).contains("httpCode");
        assertThat(json).contains("message");
    }

    @Test
    void errorDetailDTO_AllArgsConstructor_ShouldWork() {
        // Arrange
        LinkDTO link = new LinkDTO("error", "https://example.com/error");

        // Act
        ErrorDetailDTO errorDetail = new ErrorDetailDTO(
                "404", "NOT_FOUND", "Resource not found",
                "The requested mobile customer was not found", link);

        // Assert
        assertThat(errorDetail.getHttpCode()).isEqualTo("404");
        assertThat(errorDetail.getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(errorDetail.getMessage()).isEqualTo("Resource not found");
        assertThat(errorDetail.getLink()).isNotNull();
    }

    @Test
    void linkDTO_Builder_ShouldWork() {
        // Act
        LinkDTO link = LinkDTO.builder()
                .rel("self")
                .href("https://api.example.com/customers/123")
                .build();

        // Assert
        assertThat(link.getRel()).isEqualTo("self");
        assertThat(link.getHref()).isEqualTo("https://api.example.com/customers/123");
    }

    @Test
    void customerMobileErrorDTO_NoArgsConstructor_ShouldWork() {
        // Act
        CustomerMobileErrorDTO error = new CustomerMobileErrorDTO();
        error.setApiVersion("2.0");
        error.setTransactionId("TXN-789");

        ErrorDetailDTO errorDetail = new ErrorDetailDTO();
        errorDetail.setHttpCode("403");
        errorDetail.setMessage("Forbidden");

        error.setError(errorDetail);

        // Assert
        assertThat(error.getApiVersion()).isEqualTo("2.0");
        assertThat(error.getTransactionId()).isEqualTo("TXN-789");
        assertThat(error.getError().getHttpCode()).isEqualTo("403");
    }

    @Test
    void errorDetailDTO_WithoutLink_ShouldWork() throws Exception {
        // Arrange
        ErrorDetailDTO errorDetail = ErrorDetailDTO.builder()
                .httpCode("401")
                .errorCode("UNAUTHORIZED")
                .message("Authentication required")
                .build();

        CustomerMobileErrorDTO error = CustomerMobileErrorDTO.builder()
                .apiVersion("1.0")
                .error(errorDetail)
                .build();

        // Act
        String json = objectMapper.writeValueAsString(error);
        CustomerMobileErrorDTO deserialized = objectMapper.readValue(json, CustomerMobileErrorDTO.class);

        // Assert
        assertThat(deserialized.getError().getLink()).isNull();
        assertThat(deserialized.getError().getHttpCode()).isEqualTo("401");
    }
}
