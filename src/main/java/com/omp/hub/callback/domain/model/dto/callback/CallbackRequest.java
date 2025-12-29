package com.omp.hub.callback.domain.model.dto.callback;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallbackRequest<T> {

    @NotNull(message = "O campo 'data' é obrigatório")
    private T data;
}
