package com.omp.hub.callback.domain.enums;

public enum SegmentEnum {

    CLARO_CONTROLE("150", "8196", "67341", "67342", "80752"),
    BANDA_LARGA_CONTA("150", "8198", "66776", "67343", "80753");

    private String reason1; // Solicitação
    private String reason2; // reason type (Conta claro, Conta Controle, Banda Larga Conta)
    private String reason3; // Fatura (Conta)
    private String reason4; // Pagamento
    private String reason5; // Pix

    SegmentEnum(String reason1, String reason2, String reason3, String reason4, String reason5) {

        this.reason1 = reason1;
        this.reason2 = reason2;
        this.reason3 = reason3;
        this.reason4 = reason4;
        this.reason5 = reason5;

    }

    public String getReason1() {
        return this.reason1;
    }

    public String getReason2() {
        return this.reason2;
    }

    public String getReason3() {
        return this.reason3;
    }

    public String getReason4() {
        return this.reason4;
    }

    public String getReason5() {
        return this.reason5;
    }
}