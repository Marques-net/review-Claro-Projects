package com.omp.hub.callback.application.utils.apigee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApigeeResponse<T> {

    private String apiVersion;
    private String transactionId;
    private T data;
    private ErrorDTO error;
}
