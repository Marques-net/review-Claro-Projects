package com.omp.hub.callback.domain.model.dto.callback.tefweb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class EquipmentDTO {

    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'serialNumber' é obrigatório")
    private String serialNumber;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'salesPointClient' é obrigatório")
    private String salesPointClient;
}
