package com.omp.hub.callback.application.utils.apigee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApigeeTokenDTO {
    private String refresh_token_expires_in;
    private String api_product_list;
    private List<String> api_product_list_json;
    private String organization_name;
    // private DeveloperDTO developer;
    @JsonProperty("developer.email")
    private String developerEmail;
    private String token_type;
    private String issued_at;
    private String client_id;
    private String access_token;
    private String application_name;
    private String scope;
    private String expires_in;
    private String refresh_count;
    private String status;

}
