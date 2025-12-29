package com.omp.hub.callback.domain.service.check.impl;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;
import com.omp.hub.callback.domain.service.check.CheckTypeObjectService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CheckTypeObjectServiceImpl implements CheckTypeObjectService {

    private final ObjectMapper objectMapper;

    public CheckTypeObjectServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public <T> Boolean isValid(String request, Class<T> nameClass) {
        try {
            JsonNode node = objectMapper.readTree(request);
            
            if (nameClass == TransactionsRequest.class) {
                return hasRequiredFields(node,  "event");
            }
            
            if (nameClass == TefWebCallbackRequest.class) {
                return hasRequiredFields(node, "service", "paymentType", "sales");
            }
            
            if (nameClass == CreditCardCallbackRequest.class) {
                return hasRequiredFields(node, "payment");
            }
            
            if (nameClass == PixCallbackRequest.class) {
                return hasRequiredFields(node, "txId") || hasRequiredFields(node, "identifier");
            }
            
            return false;
        } catch (Exception e) {
            log.debug("Erro ao validar tipo {}: {}", nameClass.getSimpleName(), e.getMessage());
            return false;
        }
    }

    private boolean hasRequiredFields(JsonNode node, String... fields) {
        for (String field : fields) {
            if (!node.has(field) || node.get(field).isNull()) {
                return false;
            }
        }
        return true;
    }
}
