package com.omp.hub.callback.domain.model.dto.customer.residential;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerContractDTO {

    private String emailAddress;
    private String firstName;
    private String lastName;
    private String status;
    private String contractId;
    private List<PhoneDTO> phones;
}
