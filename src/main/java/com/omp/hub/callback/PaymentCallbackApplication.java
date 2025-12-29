package com.omp.hub.callback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan({ "com.omp.hub.callback",
        "com.omp.hub.callback.domain",
        "com.omp.hub.callback.application",
        "com.omp.hub.callback.infrastructure" })
public class PaymentCallbackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentCallbackApplication.class, args);
    }
}
