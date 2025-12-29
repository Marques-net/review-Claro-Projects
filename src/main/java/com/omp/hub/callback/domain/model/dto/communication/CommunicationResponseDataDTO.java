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
public class CommunicationResponseDataDTO {

    private String rtdmStatus;
    private String rtdmMessage;
    private String statusCode;
    private String status;
    private String message;
    private String trackingNumber;
    private String campaign;
    private String channel;
    private String destination;
    private String sendDate;
}
