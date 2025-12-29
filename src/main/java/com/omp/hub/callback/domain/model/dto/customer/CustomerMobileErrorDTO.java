package com.omp.hub.callback.domain.model.dto.customer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerMobileErrorDTO {

    private String apiVersion;
    private String transactionId;
    private ErrorDetailDTO error;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetailDTO {
        private String httpCode;
        private String errorCode;
        private String message;
        private String detailedMessage;
        private LinkDTO link;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LinkDTO {
        private String rel;
        private String href;
    }
}
