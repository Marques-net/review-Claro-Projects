package com.omp.hub.callback.application.utils.apigee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDTO {

    private Integer httpCode;
    private String errorCode;
    private String message;

    @JsonProperty("detailedMessage")
    @JsonIgnore // Impede que o Lombok gere getter para este campo
    private JsonNode detailedMessageNode;

    private LinkErrorDTO link;

    /**
     * Retorna detailedMessage como String, independente se veio como String ou
     * Array
     */
    @JsonIgnore
    public String getDetailedMessage() {
        if (detailedMessageNode == null) {
            return null;
        }

        if (detailedMessageNode.isTextual()) {
            // Caso seja uma string simples
            return detailedMessageNode.asText();
        } else if (detailedMessageNode.isArray()) {
            // Caso seja um array, junta os elementos com quebra de linha
            StringBuilder sb = new StringBuilder();
            for (JsonNode node : detailedMessageNode) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(node.asText());
            }
            return sb.toString();
        } else {
            // Para outros tipos, converte para string
            return detailedMessageNode.toString();
        }
    }

    /**
     * Setter personalizado para aceitar tanto String quanto JsonNode
     */
    @JsonProperty("detailedMessage")
    public void setDetailedMessageNode(JsonNode detailedMessageNode) {
        this.detailedMessageNode = detailedMessageNode;
    }
}
