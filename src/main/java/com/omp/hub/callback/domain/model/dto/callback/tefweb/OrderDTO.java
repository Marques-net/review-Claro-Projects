package com.omp.hub.callback.domain.model.dto.callback.tefweb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class OrderDTO {

    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'customerName' é obrigatório")
    private String customerName;

    private String ompTransactionId;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'customerDocument' é obrigatório")
    private String customerDocument;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'indexer' é obrigatório")
    private String indexer;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'totalValue' é obrigatório")
    private String totalValue;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'receiptNumber' é obrigatório")
    private String receiptNumber;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'valueToPay' é obrigatório")
    private String valueToPay;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'issueDate' é obrigatório")
    private String issueDate;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'storeCode' é obrigatório")
    private String storeCode;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'customerCode' é obrigatório")
    private String customerCode;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'salesPointClient' é obrigatório")
    private String salesPointClient;
    
    @JsonProperty(required = true)
    @NotEmpty(message = "O campo 'orders' é obrigatório e deve conter ao menos um item")
    @Valid
    private List<OrdersDTO> orders;
}
