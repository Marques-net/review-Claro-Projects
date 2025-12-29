package com.omp.hub.callback.domain.ports.client;

import java.util.UUID;

import com.omp.hub.callback.domain.model.dto.sap.redemptions.SapRedemptionsRequest;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.SapRedemptionsResponse;

import okhttp3.Headers;

public interface SapRedemptionsPort {

    SapRedemptionsResponse send(UUID uuid, SapRedemptionsRequest request, Headers.Builder builder);
}
