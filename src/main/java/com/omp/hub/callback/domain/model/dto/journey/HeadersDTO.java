package com.omp.hub.callback.domain.model.dto.journey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeadersDTO {
    private String authorization;
    private String contentType;
    private String salesPoint;
    private String store;
    private String channel;
}
