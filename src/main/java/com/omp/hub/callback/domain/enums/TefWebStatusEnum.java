package com.omp.hub.callback.domain.enums;

import lombok.Getter;

@Getter
public enum TefWebStatusEnum {
    ESTORNADA("5", "Transação estornada com sucesso"),
    AGUARDANDO_ESTORNO("6", "Aguardando conclusão do estorno"),
    ESTORNO_PARCIAL("9", "Estorno parcial realizado");

    private final String code;
    private final String description;

    TefWebStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TefWebStatusEnum fromCode(String code) {
        for (TefWebStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static boolean isCancellationStatus(String code) {
        return fromCode(code) != null;
    }
}
