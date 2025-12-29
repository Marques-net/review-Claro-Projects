package com.omp.hub.callback.domain.model.dto.callback.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class OriginPaymentMethodDTO {

    private String id;
    private String recurrenceId;
    private String txId;
    private String status;
    private List<UpdatesDTO> updates;

}
