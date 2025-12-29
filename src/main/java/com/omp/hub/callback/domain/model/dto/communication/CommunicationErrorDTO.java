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
public class CommunicationErrorDTO {

    private String httpCode;
    private String errorCode;
    private String message;
    private String detailedMessage;
}
