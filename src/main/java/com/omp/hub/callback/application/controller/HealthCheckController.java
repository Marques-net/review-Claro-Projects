package com.omp.hub.callback.application.controller;

import com.omp.hub.callback.application.service.SqsHealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health/check")
@RequiredArgsConstructor
public class HealthCheckController {

    private final SqsHealthCheckService sqsHealthCheckService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "omp-hub-payment-callback-ms");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sqs")
    public ResponseEntity<Map<String, Object>> sqsHealthCheck() {
        Map<String, Object> sqsStatus = sqsHealthCheckService.checkSqsHealth();
        boolean isHealthy = "UP".equals(sqsStatus.get("status"));
        
        HttpStatus httpStatus = isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(sqsStatus);
    }

    @GetMapping("/dlq")
    public ResponseEntity<Map<String, Object>> dlqHealthCheck() {
        Map<String, Object> dlqStatus = sqsHealthCheckService.checkDlqHealth();
        String status = (String) dlqStatus.get("status");
        
        HttpStatus httpStatus;
        if ("UP".equals(status)) {
            httpStatus = HttpStatus.OK;
        } else if ("WARNING".equals(status)) {
            httpStatus = HttpStatus.OK;
        } else {
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
        }
        
        return ResponseEntity.status(httpStatus).body(dlqStatus);
    }
}
