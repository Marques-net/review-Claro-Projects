package com.omp.hub.callback.domain.model.dto.sap.redemptions;

import com.omp.hub.callback.domain.model.dto.sap.payments.IdentificationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {

    private List<IdentificationDTO> identifications;
    private String name;
    private AddressDTO address;
}
