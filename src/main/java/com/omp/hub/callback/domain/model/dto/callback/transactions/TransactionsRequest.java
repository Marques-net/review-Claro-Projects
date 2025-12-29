package com.omp.hub.callback.domain.model.dto.callback.transactions;

import com.omp.hub.callback.domain.model.dto.callback.CallbackDTO;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionsRequest implements CallbackDTO {

    private String ompTransactionId;
    
    private String callbackTarget;
    
    private String targetSystem;
    
    private Integer flowType;
    
    @Valid
    private EventDTO event;

}
