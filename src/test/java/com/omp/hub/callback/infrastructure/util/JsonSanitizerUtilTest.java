package com.omp.hub.callback.infrastructure.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class JsonSanitizerUtilTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Deve remover campo cash com string malformada representando null")
    void shouldRemoveMalformedCashFieldWithEscapedNull() {
        // Given
        String malformedJson = """
            {
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
                                "sales": []
                            }
                        }
                    ]
                }
            }
            """;

        // When
        String sanitized = JsonSanitizerUtil.sanitizeCallbackJson(malformedJson, mapper);

        // Then
        assertNotNull(sanitized);
        assertFalse(sanitized.contains("\"cash\""), "Campo 'cash' malformado deveria ter sido removido");
        assertTrue(sanitized.contains("\"tefweb\""), "Campo 'tefweb' válido deveria ter sido mantido");
    }

    @Test
    @DisplayName("Deve manter JSON válido sem alterações")
    void shouldKeepValidJsonUnchanged() {
        // Given
        String validJson = """
            {
                "callbackTarget": "AtivacaoSimplificadaOmp",
                "event": {
                    "transactionOrderId": "0012765016",
                    "type": "PAYMENT",
                    "payment": [
                        {
                            "tefweb": {
                                "sales": []
                            }
                        }
                    ]
                }
            }
            """;

        // When
        String sanitized = JsonSanitizerUtil.sanitizeCallbackJson(validJson, mapper);

        // Then
        assertNotNull(sanitized);
        assertTrue(sanitized.contains("\"tefweb\""));
        assertTrue(sanitized.contains("\"callbackTarget\""));
    }

    @Test
    @DisplayName("Deve manter campo cash quando for um objeto válido")
    void shouldKeepValidCashObject() {
        // Given
        String validJson = """
            {
                "callbackTarget": "AtivacaoSimplificadaOmp",
                "event": {
                    "payment": [
                        {
                            "cash": {
                                "order": {
                                    "value": "100.00"
                                }
                            }
                        }
                    ]
                }
            }
            """;

        // When
        String sanitized = JsonSanitizerUtil.sanitizeCallbackJson(validJson, mapper);

        // Then
        assertNotNull(sanitized);
        assertTrue(sanitized.contains("\"cash\""), "Campo 'cash' válido deveria ter sido mantido");
        assertTrue(sanitized.contains("\"order\""));
    }

    @Test
    @DisplayName("Deve retornar JSON original em caso de erro de parsing")
    void shouldReturnOriginalJsonOnParsingError() {
        // Given
        String invalidJson = "{ invalid json }";

        // When
        String result = JsonSanitizerUtil.sanitizeCallbackJson(invalidJson, mapper);

        // Then
        assertEquals(invalidJson, result, "Deve retornar o JSON original quando houver erro de parsing");
    }

    @Test
    @DisplayName("Deve remover múltiplos campos malformados no mesmo array")
    void shouldRemoveMultipleMalformedFieldsInSameArray() {
        // Given
        String malformedJson = """
            {
                "event": {
                    "payment": [
                        {
                            "cash": "\\"\\\\\\\\\\\\\\\\null\\\\\\\\\\\\\\\\\\"",
                            "pix": "\\"\\\\\\\\\\\\\\\\null\\\\\\\\\\\\\\\\\\""
                        }
                    ]
                }
            }
            """;

        // When
        String sanitized = JsonSanitizerUtil.sanitizeCallbackJson(malformedJson, mapper);

        // Then
        assertNotNull(sanitized);
        assertFalse(sanitized.contains("\"cash\""), "Campo 'cash' malformado deveria ter sido removido");
        assertFalse(sanitized.contains("\"pix\""), "Campo 'pix' malformado deveria ter sido removido");
    }
}
