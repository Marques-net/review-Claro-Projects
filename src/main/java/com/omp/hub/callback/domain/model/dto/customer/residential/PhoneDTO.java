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
public class PhoneDTO {

    private String telephoneNumber;
    private String areaCode;
    private String internationalCode;
    private String contactMediumRole;
}