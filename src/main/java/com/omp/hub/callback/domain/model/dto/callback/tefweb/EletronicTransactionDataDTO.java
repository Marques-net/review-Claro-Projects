package com.omp.hub.callback.domain.model.dto.callback.tefweb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class EletronicTransactionDataDTO {

    @JsonProperty(required = true)
    @NotNull(message = "O campo 'acquirator' é obrigatório")
    @Valid
    private AcquiratorDTO acquirator;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'flag' é obrigatório")
    private String flag;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'flagCode' é obrigatório")
    private String flagCode;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'cardBin' é obrigatório")
    private String cardBin;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'cardEmbossing' é obrigatório")
    private String cardEmbossing;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'modality' é obrigatório")
    private String modality;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'transactionCoupon' é obrigatório")
    private String transactionCoupon;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'idSitef' é obrigatório")
    private String idSitef;
    
    private String cancellationNsu;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'hostNsu' é obrigatório")
    private String hostNsu;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'sitefNsu' é obrigatório")
    private String sitefNsu;
}
