package com.omp.hub.callback.domain.model.dto.hub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HubNotificationResponse {

    private String status;
    private String message;
    private String transactionId;
    private HubErrorDTO error;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HubErrorDTO {
        private String code;
        private String message;
        private String description;
    }
}
