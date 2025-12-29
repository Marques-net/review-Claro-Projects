package com.omp.hub.callback.domain.model.dto.journey.recurring;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.omp.hub.callback.domain.model.dto.journey.DataDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataRecurringDTO extends DataDTO {
    private CustomerRecurringDTO customer;
    private RecurringDTO recurring;
    private AutomaticPixDataDTO automaticPixData;
}
