package com.omp.hub.callback.domain.model.dto.journey.recurring;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerRecurringDTO {
    private String name;
    private String cpf;
    private String cnpj;
    private String phoneNumber;
    private String segment;
    private String email;
    private String address;
    private String city;
    private String uf;
    private String zipCode;
    private ContractRecurringDTO contract;
}