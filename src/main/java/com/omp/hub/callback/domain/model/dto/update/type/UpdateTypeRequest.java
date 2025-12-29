package com.omp.hub.callback.domain.model.dto.update.type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTypeRequest {
    // Classe mínima para compatibilidade
    private String identifier;
    private String type;
}
