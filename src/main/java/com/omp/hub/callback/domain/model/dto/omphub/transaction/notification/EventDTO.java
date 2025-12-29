package com.omp.hub.callback.domain.model.dto.omphub.transaction.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDTO {

    private String issueDate;
    private String transactionOrderId;
    private String type;
    private List<Object> payment;
    private Object targetPaymentMethod;
    private String status;
}
