package com.omp.hub.callback.domain.model.dto.transactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EletronicTransactionTransDataDTO {

    private EletronicAcquiratorTransDTO acquirator;
    private String flag;
    private String flagCode;
    private String cardBin;
    private String cardEmbossing;
    private String modality;
    private String transactionCoupon;
    private String idSitef;
    private String cancellationNsu;
    private String hostNsu;
    private String sitefNsu;
}
