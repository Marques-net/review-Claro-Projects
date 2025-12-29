package com.omp.hub.callback.domain.model.dto.information;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.omp.hub.callback.domain.enums.PaymentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InformationPaymentDTO {

    private UUID uuid;
    private String identifier;
    private String transactionOrderId;
    private String channel;
    private String store;
    private String pdv;
    private Boolean multiplePayment;
    private List<String> mixedPaymentTypes;
    private List<PaymentDTO> payments;
    private BigDecimal amount;
    private PaymentStatusEnum paymentStatus;
    private Instant createdAt;
    private Long createdAtTimestamp;
    private Instant updatedAt;
    private Long updatedAtTimestamp;
}
