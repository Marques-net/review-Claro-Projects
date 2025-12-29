package com.omp.hub.callback;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaymentCallbackApplicationTest {

    @Test
    void contextLoads() {
        // Test que a aplicação Spring Boot inicializa corretamente
        assertDoesNotThrow(() -> {
            // Context is loaded successfully
        });
    }
}
