package com.omp.hub.callback.domain.model.dto.journey.single;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryAddressDTO {

    private String streetName;
    private String streetNr;
    private String informationStreetNr;
    private String complement;
    private String informationComplement;
    private String city;
    private String uf;
    private String zipCode;
    private String country;
}
