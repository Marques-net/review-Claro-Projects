package com.omp.hub.callback.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    private String streetName;
    private String streetNr;
    private String informationStreetNr;
    private String complement;
    private String informationComplement;
    private String city;
    private String stateOrProvince;
    private String postCode;
    private String country;
}
