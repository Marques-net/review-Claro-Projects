package com.omp.hub.callback.domain.model.dto.callback.tefweb;

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
public class OrdersDTO {

    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'orderNumber' é obrigatório")
    private String orderNumber;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'orderValue' é obrigatório")
    private String orderValue;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'orderIssueDate' é obrigatório")
    private String orderIssueDate;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'orderStoreCode' é obrigatório")
    private String orderStoreCode;
}
