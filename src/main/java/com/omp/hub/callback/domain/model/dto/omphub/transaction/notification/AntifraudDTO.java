package com.omp.hub.callback.domain.model.dto.omphub.transaction.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AntifraudDTO {

    private String statusCode;
    private String decision;
    private String timeChangeStatus;
}
