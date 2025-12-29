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
public class MobileBillingDetailsCustomer {

    @JsonProperty("name")
    private String name;
    
    @JsonProperty("birthDate")
    private String birthDate;
    
    @JsonProperty("motherName")
    private String motherName;
    
    @JsonProperty("gender")
    private String gender;
    
    @JsonProperty("cpf")
    private String cpf;
    
    @JsonProperty("passportCountry")
    private String passportCountry;
    
    @JsonProperty("contactName")
    private String contactName;
    
    @JsonProperty("contactEmail")
    private String contactEmail;
    
    @JsonProperty("occupation")
    private String occupation;
    
    @JsonProperty("scholarity")
    private String scholarity;
    
    @JsonProperty("contributorIndicator")
    private Boolean contributorIndicator;
    
    @JsonProperty("fomeZeroContributorIndicator")
    private Boolean fomeZeroContributorIndicator;
    
    @JsonProperty("fundoPobrezaContributorIndicator")
    private Boolean fundoPobrezaContributorIndicator;
    
    @JsonProperty("ownHomeIndicator")
    private Boolean ownHomeIndicator;
}