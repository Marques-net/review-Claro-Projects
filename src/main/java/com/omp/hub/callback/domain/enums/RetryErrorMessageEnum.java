package com.omp.hub.callback.domain.enums;

import lombok.Getter;

@Getter
public enum RetryErrorMessageEnum {
    
    SAP_REDEMPTIONS("Erro ao processar resgate SAP. Por favor, tente novamente", "SAP Redemptions"),
    SAP_PAYMENTS("Erro ao processar pagamento SAP. Por favor, tente novamente", "SAP Payments"),
    SAP_BILLING("Erro ao processar cobrança SAP. Por favor, tente novamente", "SAP Billing"),
    CHANNEL_NOTIFICATION("Erro ao notificar canal. Por favor, tente novamente", "Channel Notification"),
    GENERIC("Erro ao processar pagamento. Por favor, tente novamente", null);

    private final String message;
    private final String operationName;

    RetryErrorMessageEnum(String message, String operationName) {
        this.message = message;
        this.operationName = operationName;
    }

    public static String getMessageByOperationName(String operationName) {
        if (operationName == null) {
            return GENERIC.message;
        }
        
        for (RetryErrorMessageEnum error : values()) {
            if (error.operationName != null && operationName.contains(error.operationName)) {
                return error.message;
            }
        }
        
        return GENERIC.message;
    }
}
