package com.omp.hub.callback.domain.model.dto.callback.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.omp.hub.callback.domain.model.dto.transactions.TefWebDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDTO {

    private String type;
    private String date;
    private String value;
    private String numberInstallments;
    private String pointOfSales;
    private AcquirerDTO acquirer;
    private PixDTO pix;
    private CashDTO cash;
    private TefWebDTO tefweb;
}
