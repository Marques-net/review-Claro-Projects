package com.omp.hub.callback.application.utils.apigee;

import com.omp.hub.callback.application.utils.apigee.dto.ApigeeTokenDTO;
import okhttp3.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ApigeeHeaderServiceImpl implements ApigeeHeaderService{

    @Autowired
    private AccessTokenPort port;

    public Headers.Builder generateHeaderApigee(UUID uuid) {

        ApigeeTokenDTO accessToken = port.getAccessToken(uuid);

        return new Headers.Builder()
                .add("x-client-auth", "Bearer " + accessToken.getAccess_token())
                .add("Content-Type", "application/json");
    }
}
