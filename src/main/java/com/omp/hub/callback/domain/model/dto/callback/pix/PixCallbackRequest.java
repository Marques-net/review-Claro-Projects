package com.omp.hub.callback.domain.model.dto.callback.pix;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.omp.hub.callback.domain.model.dto.callback.CallbackDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PixCallbackRequest implements CallbackDTO {

    private String ompTransactionId;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'service' é obrigatório")
    private String service;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'paymentType' é obrigatório")
    private String paymentType;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'paymentDate' é obrigatório")
    private String paymentDate;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'value' é obrigatório")
    private String value;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'endToEndId' é obrigatório")
    private String endToEndId;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'txId' é obrigatório")
    private String txId;
    
    private String orderId;

    @JsonProperty("pix")
    private void unpackNested(java.util.Map<String, Object> pix) {
        if (pix != null) {
            if (pix.get("paymentDate") != null) this.paymentDate = String.valueOf(pix.get("paymentDate"));
            if (pix.get("value") != null) this.value = String.valueOf(pix.get("value"));
            else if (pix.get("amount") != null) this.value = String.valueOf(pix.get("amount"));
            if (pix.get("endToEndId") != null) this.endToEndId = String.valueOf(pix.get("endToEndId"));
            if (pix.get("txId") != null) this.txId = String.valueOf(pix.get("txId"));
            if (pix.get("paymentType") != null) this.paymentType = String.valueOf(pix.get("paymentType"));
        }
    }

}
