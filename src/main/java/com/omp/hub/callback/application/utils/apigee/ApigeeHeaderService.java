package com.omp.hub.callback.application.utils.apigee;

import okhttp3.Headers;

import java.util.UUID;

public interface ApigeeHeaderService {

    Headers.Builder generateHeaderApigee(UUID uuid);
}
