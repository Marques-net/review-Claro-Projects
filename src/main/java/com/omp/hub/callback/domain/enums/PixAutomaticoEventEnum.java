package com.omp.hub.callback.domain.enums;

public enum PixAutomaticoEventEnum {

    OPTIN("Confirmação Optin"),
    OPTOUT("Confirmação Optout"),
    AGENDAMENTO("Confirmação do Agendamento da Recorrência Mensal"),
    PAGAMENTO("Pagamento PIX Automático Executado"),
    //PAGAMENTO_AVULSO("Pagamento PIX Executado"),
    ALTERACAO("Alteração na forma de pagamento"),
    COBRANCA("Nova cobrança"),
    FALHA_AGENDAMENTO("Falha no agendamento PIX Automático"),
    INCENTIVO_ADESAO("Incentivo à adesão PIX Automático");

    private final String description;

    PixAutomaticoEventEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PixAutomaticoEventEnum fromString(String type) {
        if (type == null) return null;
        
        try {
            return PixAutomaticoEventEnum.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
