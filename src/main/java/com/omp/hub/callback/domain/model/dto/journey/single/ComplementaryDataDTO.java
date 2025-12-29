package com.omp.hub.callback.domain.model.dto.journey.single;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComplementaryDataDTO {

    private String purchaserName;
    private String cpf;
    private String cnpj;
    private String phoneNumber;
    private String currency;
    private String email;
    private DeliveryAddressDTO deliveryAddress;
    private List<ProductDTO> products;
    private List<MerchantDefinedDataDTO> merchantDefinedData;
}
