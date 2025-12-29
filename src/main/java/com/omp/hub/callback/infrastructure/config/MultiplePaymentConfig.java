package com.omp.hub.callback.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "multiplepayment")
public class MultiplePaymentConfig {

    private TransactionOrderIdConfig transactionOrderId;

    @Data
    public static class TransactionOrderIdConfig {
        private String prefix;
        private String suffixFormat;
        private Integer baseLength;
        private Integer maxLength;
    }
}
