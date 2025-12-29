package com.omp.hub.callback.domain.service.generate.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.DataSingleDTO;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.DataDTO;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.OrderDTO;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.PosInfoDTO;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.SapRedemptionsRequest;
import com.omp.hub.callback.domain.service.generate.GenerateSapRedemptionsRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class GenerateSapRedemptionsRequestServiceImpl implements GenerateSapRedemptionsRequestService {

    private static final Logger logger = LoggerFactory.getLogger(GenerateSapRedemptionsRequestServiceImpl.class);
    private static final String C_ATIV_SIMP = "ativacaosimplificada";
    private static final String C_SOLAR = "solar";

    @Value("${ativacao.simplificada.loja}")
    private String atvSimplLoja;

    @Autowired
    private ObjectMapper mapper;

    public SapRedemptionsRequest generateRequest(InformationPaymentDTO info) {
        DataSingleDTO dto = null;
        String identifier = info.getIdentifier();

        if (info.getPayments() != null && !info.getPayments().isEmpty() && info.getPayments().get(0) != null && info.getPayments().get(0).getJourney() != null) {
            String data = (String) info.getPayments().get(0).getJourney();
            String journeyTrim = data.trim();

            try {
                if (journeyTrim.startsWith("{")) {
                    dto = mapper.readValue(journeyTrim, DataSingleDTO.class);
                } else {
                    String unwrapped = mapper.readValue(journeyTrim, String.class);
                    dto = mapper.readValue(unwrapped, DataSingleDTO.class);
                }
            }
            catch (JsonProcessingException e) {
                logger.info("TxId: " + identifier + " - ERROR: {}", e.getMessage());
                throw new BusinessException("Erro convert Json", "ERROR_CONVERT_JSON", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Objeto não encontrado")
                    .details("O objeto data não foi encontrado nos dados salvos no DynamoDB")
                    .errorCode("ERROR_OBJECT_NOT_FOUND")
                    .status(HttpStatus.NOT_FOUND.value())
                    .timestamp(Instant.now())
                    .build();

            throw new BusinessException(error);
        }

        String salesOrderId = dto.getPayment() != null && dto.getPayment().getSalesOrderId() != null ? dto.getPayment().getSalesOrderId() : identifier;
        String store = null;

        logger.info("TxId: " + identifier + " - STORE: " + info.getStore());

        try {
            logger.info("TxId: " + identifier + " - INFO: " + mapper.writeValueAsString(info));
        }
        catch (Exception e){

        }

        if (info.getStore() == null || "DEFAULT".equals(info.getStore())) {
            if (C_ATIV_SIMP.equals(info.getChannel()) || C_SOLAR.equals(info.getChannel())) {
                store = atvSimplLoja;
            }
        }
        else {
            store = info.getStore();
        }

        return SapRedemptionsRequest.builder()
                .data(DataDTO.builder()
                        .order(OrderDTO.builder()
                                .id(salesOrderId)
                                .companyId("001")
                                .businessLocationId(store)
                                .type("R")
                                .posInfo(PosInfoDTO.builder()
                                        .componentNumber("4000")
                                        .build())
                                .build())
                        .build())
                .build();
    }
}