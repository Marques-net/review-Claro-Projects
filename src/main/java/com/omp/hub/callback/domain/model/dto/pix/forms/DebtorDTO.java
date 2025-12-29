package com.omp.hub.callback.domain.model.dto.pix.forms;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DebtorDTO {

    private String email;
    private String telephone;
    private String address;
    private String city;
    private String uf;
    private String zipCode;
    private String name;
    private String cpf;
    private String cnpj;
}
