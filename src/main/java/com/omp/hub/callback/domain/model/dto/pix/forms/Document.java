package com.omp.hub.callback.domain.model.dto.pix.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Document {
    @JsonProperty("type")
    private String type;

    @JsonProperty("number")
    private String number;
}
