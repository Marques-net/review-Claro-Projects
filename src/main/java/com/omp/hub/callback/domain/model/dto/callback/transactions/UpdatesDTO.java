package com.omp.hub.callback.domain.model.dto.callback.transactions;

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
public class UpdatesDTO {

    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'status' é obrigatório")
    private String status;
    
    @JsonProperty(required = true)
    @NotBlank(message = "O campo 'dateTime' é obrigatório")
    private String dateTime;
}
