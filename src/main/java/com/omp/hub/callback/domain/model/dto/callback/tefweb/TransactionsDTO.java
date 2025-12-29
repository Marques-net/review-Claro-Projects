package com.omp.hub.callback.domain.model.dto.callback.tefweb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class TransactionsDTO {

    @JsonProperty(required = true)
    @NotNull(message = "O campo 'transactionData' é obrigatório")
    @Valid
    private TransactionDataDTO transactionData;
    
    @JsonProperty(required = true)
    @NotNull(message = "O campo 'eletronicTransactionData' é obrigatório")
    @Valid
    private EletronicTransactionDataDTO eletronicTransactionData;
}
