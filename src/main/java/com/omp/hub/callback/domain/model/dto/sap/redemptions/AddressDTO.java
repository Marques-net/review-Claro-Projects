package com.omp.hub.callback.domain.model.dto.sap.redemptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    private String street;
    private String postalCode;
    private String city;
//    private String neighborhood;
    private String region;
    private String countryCode;
}
