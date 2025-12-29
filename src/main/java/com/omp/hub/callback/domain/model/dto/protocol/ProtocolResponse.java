package com.omp.hub.callback.domain.model.dto.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolResponse {
    // Classe mínima para compatibilidade
    private String protocol;
}
