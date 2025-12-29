package com.omp.hub.callback.domain.enums;

public enum PaymentTypeEnum {

    PIX ("Pagamento PIX"),
    PIX_AT_J1 ("Adesão PIX Automático"),
    PIX_AT_J2 ("Adesão PIX Automático"),
    PIX_AT_J3 ("Adesão e 1° pagamento PIX Automático"),
    PIX_AT_J4 ("Adesão e 1° pagamento PIX Automático"),
    CCRED ("Pagamento com Cartão de Crédito"),
    TEFWEB ("Pagamento com POS"),
    CASH ("Pagamento em dinheiro"),
    LINK ("Pagamento por link de pagamento"),
    UNDEFINED ("Pagamento não definido");

    private String description;

    PaymentTypeEnum(String description){
        this.description = description;
    }

    public String getDescription() { return this.description; }

}