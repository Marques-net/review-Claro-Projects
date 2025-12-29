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
public class Recipient {
    @JsonProperty("branch")
    private String branch;

    @JsonProperty("account")
    private String account;

    @JsonProperty("bankId")
    private String bankId;

    @JsonProperty("participantIspb")
    private String participantIspb;

    @JsonProperty("document")
    private Document document;
}
