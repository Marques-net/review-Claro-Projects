package com.omp.hub.callback.domain.model.dto.pix.forms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Calendar {
    @JsonProperty("initialDate")
    private String initialDate;

    @JsonProperty("finalDate")
    private String finalDate;

    @JsonProperty("requestExpirationDate")
    private String requestExpirationDate;

    @JsonProperty("frequencyType")
    private String frequencyType;

}
