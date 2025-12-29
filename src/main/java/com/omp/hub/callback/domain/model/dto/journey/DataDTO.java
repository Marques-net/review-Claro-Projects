package com.omp.hub.callback.domain.model.dto.journey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataDTO {
    // Classe mínima para compatibilidade
    private Object data;
}
