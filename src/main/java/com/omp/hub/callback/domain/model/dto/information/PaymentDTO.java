package com.omp.hub.callback.domain.model.dto.information;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.omp.hub.callback.domain.enums.PaymentStatusEnum;
import com.omp.hub.callback.domain.enums.PaymentTypeEnum;
import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDTO {

    private String transactionOrderId;
    private Integer paymentOrder;
    private PaymentTypeEnum type;
    private Object journey;
    private Object callback;
    private Object pixAuto;
    private PaymentStatusEnum paymentStatus;
    private BigDecimal value;
    private Instant createdAt;
    private Long createdAtTimestamp;
    private Instant updatedAt;
    private Long updatedAtTimestamp;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ErrorResponse error;
    
    // Índice da transação no array de transactions do callback TefWeb
    // Usado para múltiplas transações no mesmo Sale
    @Builder.Default
    private Integer transactionIndex = 0;
}
