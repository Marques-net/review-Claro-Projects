package com.omp.hub.callback.domain.model.dto.dynamodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodDTO {

    private String channel;
    private String store;
    private String pdv;
    private List<String> single;
    private List<String> recurring;

}
