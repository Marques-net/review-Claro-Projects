package com.omp.hub.callback.domain.service.aws;

public interface AwsParametersStoreService {

    String getParameterByArn(String parameterArn);

}
