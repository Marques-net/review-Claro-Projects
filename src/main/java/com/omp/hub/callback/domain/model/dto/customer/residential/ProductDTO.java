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
public class ProductDTO {

    private String productId;
    private String type;
    private String family;
    private String speed;
    private String plan;
    private String description;
    private String businessSegment;
    private String installStatus;
    private String installDate;
}
