package com.omp.hub.callback.domain.service.aws.impl;

import com.omp.hub.callback.domain.service.aws.AwsParametersStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

@Service
public class AwsParametersStoreServiceImpl implements AwsParametersStoreService {

    private static final Logger logger = LoggerFactory.getLogger(AwsParametersStoreServiceImpl.class);

    @Autowired
    private SsmClient ssmClient;

    public String getParameterByArn(String parameterArn) {
        try {
            logger.info("Buscando parâmetro com ARN: {}", parameterArn);

            GetParameterRequest request = GetParameterRequest.builder()
                    .name(parameterArn)
                    .withDecryption(true)
                    .build();

            GetParameterResponse response = ssmClient.getParameter(request);

            logger.info("Parâmetro encontrado: {}", parameterArn);
            return response.parameter().value();

        } catch (ParameterNotFoundException e) {
            logger.error("Parâmetro não encontrado: {}", parameterArn, e);
            return null;
        } catch (Exception e) {
            logger.error("Erro ao buscar parâmetro: {}", parameterArn, e);
            throw e;
        }
    }

}
