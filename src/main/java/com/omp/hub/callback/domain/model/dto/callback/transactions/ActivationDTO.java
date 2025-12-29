package com.omp.hub.callback.domain.model.dto.callback.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class ActivationDTO {

    @JsonProperty(required = true)
    @NotNull(message = "O campo 'journeyData' é obrigatório")
    @Valid
    private JourneyDataDTO journeyData;
}
