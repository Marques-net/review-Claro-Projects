package com.omp.hub.callback.domain.model.dto.customer;

import java.util.List;

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
public class MobileSubscriptionDataDTO {
    private List<SubscriberDTO> subscribers;
    private String totalRecords;
}
