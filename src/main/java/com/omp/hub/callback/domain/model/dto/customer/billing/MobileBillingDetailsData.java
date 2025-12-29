package com.omp.hub.callback.domain.model.dto.customer.billing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileBillingDetailsData {

    @JsonProperty("personType")
    private String personType;
    
    @JsonProperty("customerSubtype")
    private String customerSubtype;
    
    @JsonProperty("customer")
    private MobileBillingDetailsCustomer customer;
}