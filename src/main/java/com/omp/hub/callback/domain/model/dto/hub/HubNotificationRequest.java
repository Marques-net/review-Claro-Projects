package com.omp.hub.callback.domain.model.dto.hub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HubNotificationRequest {

    private String txId;
    private String eventType;
    private String status;
    private String paymentType;
    private String paymentDate;
    private String value;
    private String endToEndId;
    private String customerIdentifier;
    private String notificationChannel;
}
