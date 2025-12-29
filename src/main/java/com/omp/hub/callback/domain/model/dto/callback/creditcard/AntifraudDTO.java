package com.omp.hub.callback.domain.model.dto.callback.creditcard;

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
public class AntifraudDTO {

    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'statusCode' é obrigatório")
    private String statusCode;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'decision' é obrigatório")
    private String decision;
    
    private String timeChangeStatus;
}
