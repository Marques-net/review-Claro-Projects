package com.omp.hub.callback.domain.model.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedCustomerDataDTO {

    private String name;
    private String cpf;
    private String cnpj;
    private String segment;
    private String mobileBan;
    private String operatorCode;
    private String cityCode;
    private String contractNumber;
    private String email;
    private String msisdn;
    private Boolean criteriosAtendidos;

    public Boolean hasCompleteData() {
//        return hasValidName() && hasValidDocument() && hasValidSegment();
        return hasValidName() && hasValidDocument();
    }

    private Boolean hasValidName() {
        return name != null && !name.trim().isEmpty();
    }

    private Boolean hasValidDocument() {
        return hasValidCpf() || hasValidCnpj();
    }

    private Boolean hasValidCpf() {
        return cpf != null && !cpf.trim().isEmpty();
    }

    private Boolean hasValidCnpj() {
        return cnpj != null && !cnpj.trim().isEmpty();
    }

    private Boolean hasValidSegment() {
        return segment != null && !segment.trim().isEmpty();
    }

    public Boolean hasValidMobileBan() {
        return mobileBan != null && !mobileBan.trim().isEmpty();
    }

    public Boolean hasValidContractNumber() {
        return contractNumber != null && !contractNumber.trim().isEmpty();
    }
}