package com.omp.hub.callback.domain.model.dto.callback.tefweb;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesDTO {

    @JsonProperty(required = true)
    @NotNull(message = "O campo 'order' é obrigatório")
    @Valid
    private OrderDTO order;
    
    @JsonProperty(required = true)
    @NotNull(message = "O campo 'equipment' é obrigatório")
    @Valid
    private EquipmentDTO equipment;
    
    @JsonProperty(required = true)
    @NotEmpty(message = "O campo 'transactions' é obrigatório e deve conter ao menos um item")
    @Valid
    private List<TransactionsDTO> transactions;
}
