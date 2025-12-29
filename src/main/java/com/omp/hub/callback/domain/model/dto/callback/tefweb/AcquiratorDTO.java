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
public class AcquiratorDTO {

    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'code' é obrigatório")
    private String code;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'description' é obrigatório")
    private String description;
}
