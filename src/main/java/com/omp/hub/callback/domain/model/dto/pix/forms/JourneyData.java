package com.omp.hub.callback.domain.model.dto.pix.forms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.omp.hub.callback.domain.enums.JourneyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JourneyData {
    @JsonProperty("txId")
    private String txId;
    
    @JsonProperty("journeyType")
    private JourneyType journeyType;

    @JsonProperty("pixKeyId")
    private String pixKeyId;

    @JsonProperty("value")
    private String value;

    @JsonProperty("expiration")
    private Integer expiration;
}
