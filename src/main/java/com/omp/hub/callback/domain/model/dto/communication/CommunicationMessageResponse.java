package com.omp.hub.callback.domain.model.dto.communication;

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
public class CommunicationMessageResponse {

    private String apiVersion;
    private String transactionId;
    private String startTimestamp;
    private String endTimestamp;
    private CommunicationResponseDataDTO data;
    private CommunicationErrorDTO error;
}
