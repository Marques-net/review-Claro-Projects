package com.omp.hub.callback.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class CallbackControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldProcessCallbackWithMalformedCashField() throws Exception {
        // Given - JSON com campo cash malformado (string com múltiplos escapes)
        String callbackJson = """
            {
                "data": {
                    "callbackTarget": "AtivacaoSimplificadaOmp",
                    "event": {
                        "transactionOrderId": "0012765016",
                        "type": "PAYMENT",
                        "payment": [
                            {
                                "cash": "\\"\\\\\\\\\\\\\\\\null\\\\\\\\\\\\\\\\\\""
                            },
                            {
                                "tefweb": {
                                    "sales": [
                                        {
                                            "order": {
                                                "customerName": "JOAO TESTE",
                                                "ompTransactionId": "fcfcc572-ac58-437e-898b-16910b2717cb",
                                                "customerDocument": "43765478999",
                                                "indexer": "SV000012765016H1",
                                                "totalValue": "140000",
                                                "receiptNumber": "SV000012765016H1",
                                                "valueToPay": "140000",
                                                "issueDate": "22/12/2025 00:00:00",
                                                "storeCode": "1120",
                                                "customerCode": "1120-LB30-SV000012765016H1",
                                                "salesPointClient": "LB30"
                                            },
                                            "equipment": {
                                                "serialNumber": "5201012229009463",
                                                "salesPointClient": "LB30"
                                            },
                                            "transactions": []
                                        }
                                    ]
                                }
                            }
                        ],
                        "status": "PAGO"
                    }
                }
            }
            """;

        // When & Then
        mockMvc.perform(post("/omphub/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnErrorForCompletelyInvalidJson() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/omphub/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().is4xxClientError());
    }
}
