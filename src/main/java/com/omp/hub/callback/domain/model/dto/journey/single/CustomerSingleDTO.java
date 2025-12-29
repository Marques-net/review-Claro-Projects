package com.omp.hub.callback.domain.model.dto.journey.single;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerSingleDTO {
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
    private ContractSingleDTO contract;
}
