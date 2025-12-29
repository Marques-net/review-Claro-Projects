package com.omp.hub.callback.domain.model.dto.customer.residential;

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
public class CustomerResidentialDetailsDTO {

    private String name;
    private String documentNumber;
    private String emailAddress;
    private String birthdayDate;
    private Integer personTypeId;
    private Integer installationAddressId;
}
