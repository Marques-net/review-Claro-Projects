package com.omp.hub.callback.application.utils.apigee;

import java.util.UUID;

import com.omp.hub.callback.application.utils.apigee.dto.ApigeeTokenDTO;

public interface AccessTokenPort {

    ApigeeTokenDTO getAccessToken(UUID uuid);
}
