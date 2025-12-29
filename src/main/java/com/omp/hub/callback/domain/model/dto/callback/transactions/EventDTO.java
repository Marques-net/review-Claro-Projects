package com.omp.hub.callback.domain.model.dto.callback.transactions;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {

    private String type;
    
    private List<PaymentDTO> payment;
    private CustomerDTO customer;
    private String additionalInfo;
    private OriginPaymentMethodDTO originPaymentMethod;
    private TargetPaymentMethodDTO targetPaymentMethod;
    
    private String paymentMethod;
    
    private String recurrenceId;
    
    private String txId;
    
    private String status;
    
    @Valid
    private List<UpdatesDTO> updates;
    
    private List<AttemptsDTO> attempts;
    
    @Valid
    private ActivationDTO activation;
    
    private AddonDTO addon;

}
