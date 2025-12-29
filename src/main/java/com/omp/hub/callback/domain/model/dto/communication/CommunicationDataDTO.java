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
public class CommunicationDataDTO {

    private String layout;
    private String customization;
    private String validator;
    private String templateData;
    private String destination;
    private String channel; // "1" para SMS, "2" para Email
    private String project;
    private String campaign;
    private String mobileClient;
    private String templateCode;
    private String message;
}
