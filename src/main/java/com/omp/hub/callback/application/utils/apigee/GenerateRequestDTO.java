package com.omp.hub.callback.application.utils.apigee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.Headers;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenerateRequestDTO<T> {

    private T body;
    private String apiUrl;
    private Headers headers;
    private String httpVerb;

}
