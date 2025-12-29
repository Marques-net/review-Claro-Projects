package com.omp.hub.callback.infrastructure.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class JsonSanitizerUtil {

    public static String sanitizeCallbackJson(String jsonString, ObjectMapper mapper) {
        try {
            JsonNode rootNode = mapper.readTree(jsonString);
            
            // Procurar por event.payment[] e sanitizar cada item
            if (rootNode.has("event") && rootNode.get("event").has("payment")) {
                JsonNode paymentArray = rootNode.get("event").get("payment");
                
                if (paymentArray.isArray()) {
                    for (JsonNode paymentItem : paymentArray) {
                        if (paymentItem.isObject()) {
                            ObjectNode paymentObj = (ObjectNode) paymentItem;
                            sanitizePaymentItem(paymentObj);
                        }
                    }
                }
            }
            
            return mapper.writeValueAsString(rootNode);
            
        } catch (Exception e) {
            log.error("Erro ao sanitizar JSON: {}", e.getMessage());
            // Em caso de erro, retorna o JSON original
            return jsonString;
        }
    }
    
    private static void sanitizePaymentItem(ObjectNode paymentObj) {
        // Lista de campos que podem conter strings malformadas representando null
        String[] fieldsToCheck = {"cash", "pix", "tefweb", "card"};
        
        for (String field : fieldsToCheck) {
            if (paymentObj.has(field)) {
                JsonNode fieldNode = paymentObj.get(field);
                
                // Se o campo for uma string, verificar se é uma representação malformada de null
                if (fieldNode.isTextual()) {
                    String value = fieldNode.asText();
                    
                    // Detectar strings com múltiplos escapes que representam null
                    if (isEscapedNullString(value)) {
                        log.warn("Campo '{}' contém string malformada representando null. Removendo campo.", field);
                        paymentObj.remove(field);
                    } else {
                        log.warn("Campo '{}' é uma string mas deveria ser um objeto. Valor: {}", field, value);
                        // Opcional: você pode decidir remover ou converter
                        paymentObj.remove(field);
                    }
                }
            }
        }
    }
    
    private static boolean isEscapedNullString(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        
        // Verificar se contém múltiplas barras invertidas e aspas indicando escapes aninhados
        // Exemplo: "\"\\\"\\\\\\\"\\\\\\\\\\\\\\\"null\\\\\\\\\\\\\\\"\\\\\\\"\\\"\""
        boolean hasMultipleEscapes = value.contains("\\\\") && value.contains("\\\"");
        boolean containsNull = value.contains("null");
        
        // Também verificar se praticamente toda a string é composta por escapes
        long escapeCount = value.chars().filter(ch -> ch == '\\' || ch == '\"').count();
        boolean mostlyEscapes = escapeCount > (value.length() * 0.5); // Mais de 50% são escapes
        
        // Considerar string malformada se:
        // 1. Contém múltiplos escapes E contém "null", OU
        // 2. É majoritariamente composta por escapes (indicando string corrompida)
        return (hasMultipleEscapes && containsNull) || mostlyEscapes;
    }
}
