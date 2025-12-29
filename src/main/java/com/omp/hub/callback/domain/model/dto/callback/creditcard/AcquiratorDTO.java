package com.omp.hub.callback.domain.model.dto.callback.creditcard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class AcquiratorDTO {

    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'nsu' é obrigatório")
    private String nsu;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'authorizationCode' é obrigatório")
    private String authorizationCode;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'acquiratorCode' é obrigatório")
    private String acquiratorCode;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'transactionId' é obrigatório")
    private String transactionId;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'responseCode' é obrigatório")
    private String responseCode;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'responseDescription' é obrigatório")
    private String responseDescription;
    
    private String merchantAdviceCode;

}
