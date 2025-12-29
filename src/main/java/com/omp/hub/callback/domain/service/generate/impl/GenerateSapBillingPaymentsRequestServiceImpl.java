package com.omp.hub.callback.domain.service.generate.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.exceptions.ErrorResponse;
import com.omp.hub.callback.domain.model.dto.callback.CallbackDTO;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.DataSingleDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.CardDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.DataDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.DetailDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.PosInfoDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsRequest;
import com.omp.hub.callback.domain.service.generate.GenerateSapBillingPaymentsRequestService;
import com.omp.hub.callback.domain.service.check.CheckTypeObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GenerateSapBillingPaymentsRequestServiceImpl implements GenerateSapBillingPaymentsRequestService {

    private static final String C_ATIV_SIMP = "ativacaosimplificada";
    private static final String C_SOLAR = "solar";

    @Value("${ativacao.simplificada.loja}")
    private String atvSimplLoja;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CheckTypeObjectService validateService;

    public SapBillingPaymentsRequest generateRequest(CallbackDTO request, InformationPaymentDTO info) {

        try {
            String callbackSTR = mapper.writeValueAsString(request);

            String data = info != null && !info.getPayments().isEmpty() && info.getPayments().get(0).getJourney() != null
                    ? (String) info.getPayments().get(0).getJourney()
                    : null;
            DataSingleDTO dto = mapper.readValue(data, DataSingleDTO.class);

            if (validateService.isValid(callbackSTR, TefWebCallbackRequest.class)) {
                TefWebCallbackRequest tefwebCallback = mapper.readValue(callbackSTR, TefWebCallbackRequest.class);

                String identification = null;

                if (dto != null && dto.getFraudAnalysisData() != null && dto.getFraudAnalysisData().getComplementaryData() != null
                    && dto.getFraudAnalysisData().getComplementaryData().getProducts() != null
                    && !dto.getFraudAnalysisData().getComplementaryData().getProducts().isEmpty()) {
                    identification = dto.getFraudAnalysisData().getComplementaryData().getProducts().stream()
                        .filter(p -> "T30".equals(p.getCode()) || "T3A".equals(p.getCode())).findFirst().get().getCode();
                }

                return SapBillingPaymentsRequest.builder()
                        .data(DataDTO.builder()
                                .payment(PaymentDTO.builder()
                                        .company("001")
                                        .businessLocation(this.getStore(info != null ? info : null))
                                        .identification(identification)
                                        .posInfo(PosInfoDTO.builder()
                                                .componentNumber("4000")
                                                .transactionId(this.getAuthorizationId(tefwebCallback))
                                                .build())
                                        .customerName(this.getCustomerName(dto))
                                        .date(this.getDate(tefwebCallback))
                                        .value(this.formatMoney(tefwebCallback))
                                        .username(dto != null && dto.getPayment() != null && dto.getPayment().getCardData() != null
                                                ? dto.getPayment().getSellerId()
                                                : "SYSTEM")
                                        .details(this.getDetails(tefwebCallback))
                                        .build())
                                .build())
                        .build();
            }
            else {
                ErrorResponse error = ErrorResponse.builder()
                        .message("Dados Incorretos")
                        .details("O payload está incorreto")
                        .errorCode(HttpStatus.BAD_REQUEST.toString())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .timestamp(Instant.now())
                        .build();
                throw new BusinessException(error);
            }
        }
        catch (BusinessException e) {
            throw e;
        }
        catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    private String getStore(InformationPaymentDTO info) {
        if (info != null) {
            if (info.getStore() == null || "DEFAULT".equals(info.getStore())) {
                if (C_ATIV_SIMP.equals(info.getChannel()) || C_SOLAR.equals(info.getChannel())) {
                    return atvSimplLoja;
                }
            } else {
                return info.getStore();
            }
        }

        return "";
    }
    private String getCustomerName(DataSingleDTO dto) {

        if (dto != null && dto.getCustomer() != null && dto.getCustomer().getName() != null) {
            return dto.getCustomer().getName();
        }

        return "";
    }

    private String getDate(TefWebCallbackRequest tefwebCallback) {

        String datetime = null;

        if (tefwebCallback.getSales() != null && !tefwebCallback.getSales().isEmpty()
            && tefwebCallback.getSales().get(0) != null
            && tefwebCallback.getSales().get(0).getTransactions() != null
            && !tefwebCallback.getSales().get(0).getTransactions().isEmpty()
            && tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData() != null
            && tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData()
                .getTransactionDate() != null) {

            datetime = tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData()
                    .getTransactionDate();
        }
        LocalDate date = null;

        if (datetime != null && datetime.matches("^[0-9]{2}/[0-9]{2}/[0-9]{4}$")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            date = LocalDate.parse(datetime, formatter);
            return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String formatMoney(TefWebCallbackRequest req) {

        if (req.getSales() != null && !req.getSales().isEmpty() && req.getSales().get(0).getTransactions() != null
                && !req.getSales().get(0).getTransactions().isEmpty()
                && req.getSales().get(0).getTransactions().get(0).getTransactionData() != null
                && req.getSales().get(0).getTransactions().get(0).getTransactionData().getValue() != null) {

            String value = req.getSales().get(0).getTransactions().get(0).getTransactionData().getValue();

            if (value == null || value.isEmpty()) {
                return "0.00";
            }

            try {
                if (value.contains(".")) {
                    BigDecimal valueBD = new BigDecimal(value);
                    return valueBD.setScale(2).toString();
                }

                BigDecimal valueBD = new BigDecimal(value);
                BigDecimal correctValue = valueBD.divide(new BigDecimal("100"));
                return correctValue.setScale(2).toString();
            } catch (NumberFormatException e) {
                return "0.00";
            }
        }

        return "0.00";
    }


    private List<DetailDTO> getDetails(TefWebCallbackRequest tefwebCallback) {

        Integer count = 1;

        DetailDTO detail = DetailDTO.builder()
            .sequenceId(count.toString())
            .value(this.formatMoney(tefwebCallback))
            .paymentMethod(this.getPaymentMethod(tefwebCallback))
            .card(this.getCardDetail(tefwebCallback))
            .build();

        List<DetailDTO> details = new ArrayList<>();
        details.add(detail);
        return details;
    }

    private String getPaymentMethod(TefWebCallbackRequest tefwebCallback) {

        if (tefwebCallback.getSales() != null && !tefwebCallback.getSales().isEmpty()
                && tefwebCallback.getSales().get(0) != null
                && tefwebCallback.getSales().get(0).getTransactions() != null
                && !tefwebCallback.getSales().get(0).getTransactions().isEmpty()
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData() != null
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData().getPaymentType() != null
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData().getPaymentType()
                    .getPaymentType() != null
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData().getPaymentType()
                    .getDetailPaymentType() != null) {

            return tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData().getPaymentType()
                    .getPaymentType()
                    + "_" + tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData()
                    .getPaymentType().getDetailPaymentType();
        }
        else {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Dados Incorretos")
                    .details("O campo PaymentType e/ou DetailPaymentType estão incorreto")
                    .errorCode(HttpStatus.BAD_REQUEST.toString())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .timestamp(Instant.now())
                    .build();
            throw new BusinessException(error);
        }
    }

    private CardDTO getCardDetail(TefWebCallbackRequest tefwebCallback) {

        return CardDTO.builder()
                .cardNumber(this.getCardNumber(tefwebCallback))
                .approvementId(this.getApprovedId(tefwebCallback))
                .authorizationId(this.getAuthorizationId(tefwebCallback))
                .financialTransactionCentralId(this.getAuthorizationId(tefwebCallback))
                .issuerId(this.getIssuerId(tefwebCallback))
                .issuerDescription(this.getIssuerDescription(tefwebCallback))
                .valueAddedNetworkId(this.getValueAddedNetworkId(tefwebCallback))
                .valueAddedNetworkDescription(this.getAddedNetworkDescription(tefwebCallback))
                .build();
    }

    private String getCardNumber(TefWebCallbackRequest tefwebCallback) {

        if (tefwebCallback.getSales() != null && !tefwebCallback.getSales().isEmpty()
            && tefwebCallback.getSales().get(0) != null
            && tefwebCallback.getSales().get(0).getTransactions() != null
            && !tefwebCallback.getSales().get(0).getTransactions().isEmpty()
            && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData() != null
            && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                .getCardBin() != null
            && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                .getCardEmbossing() != null) {

            return  tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                    .getCardBin()
                    + "******" + tefwebCallback.getSales().get(0).getTransactions().get(0)
                    .getEletronicTransactionData().getCardEmbossing();
        }
        else {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Dados Incorretos")
                    .details("O campo CardBin e/ou CardEmbossing estão incorretos")
                    .errorCode(HttpStatus.BAD_REQUEST.toString())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .timestamp(Instant.now())
                    .build();
            throw new BusinessException(error);
        }
    }

    private String getIssuerId(TefWebCallbackRequest tefwebCallback) {

        if (tefwebCallback.getSales() != null && !tefwebCallback.getSales().isEmpty()
                && tefwebCallback.getSales().get(0) != null
                && tefwebCallback.getSales().get(0).getTransactions() != null
                && !tefwebCallback.getSales().get(0).getTransactions().isEmpty()
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData() != null
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                .getFlagCode() != null) {

            return tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                    .getFlagCode();
        }
        else {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Dados Incorretos")
                    .details("O campo FlagCode está incorreto")
                    .errorCode(HttpStatus.BAD_REQUEST.toString())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .timestamp(Instant.now())
                    .build();
            throw new BusinessException(error);
        }
    }

    private String getIssuerDescription(TefWebCallbackRequest tefwebCallback) {

        if (tefwebCallback.getSales() != null && !tefwebCallback.getSales().isEmpty()
                && tefwebCallback.getSales().get(0) != null
                && tefwebCallback.getSales().get(0).getTransactions() != null
                && !tefwebCallback.getSales().get(0).getTransactions().isEmpty()
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData() != null
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                .getFlag() != null) {

            return tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                    .getFlag();
        }
        else {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Dados Incorretos")
                    .details("O campo Flag está incorreto")
                    .errorCode(HttpStatus.BAD_REQUEST.toString())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .timestamp(Instant.now())
                    .build();
            throw new BusinessException(error);
        }
    }

    private String getValueAddedNetworkId(TefWebCallbackRequest tefwebCallback) {

        if (tefwebCallback.getSales() != null && !tefwebCallback.getSales().isEmpty()
                && tefwebCallback.getSales().get(0) != null
                && tefwebCallback.getSales().get(0).getTransactions() != null
                && !tefwebCallback.getSales().get(0).getTransactions().isEmpty()
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData() != null
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                    .getAcquirator() != null
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                    .getAcquirator().getCode() != null) {

            return tefwebCallback.getSales().get(0).getTransactions().get(0)
                    .getEletronicTransactionData().getAcquirator().getCode();
        }
        else {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Dados Incorretos")
                    .details("O campo Code está incorreto")
                    .errorCode(HttpStatus.BAD_REQUEST.toString())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .timestamp(Instant.now())
                    .build();
            throw new BusinessException(error);
        }
    }

    private String getAddedNetworkDescription(TefWebCallbackRequest tefwebCallback) {

        if (tefwebCallback.getSales() != null && !tefwebCallback.getSales().isEmpty()
                && tefwebCallback.getSales().get(0) != null
                && tefwebCallback.getSales().get(0).getTransactions() != null
                && !tefwebCallback.getSales().get(0).getTransactions().isEmpty()
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData() != null
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                    .getAcquirator() != null
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                    .getAcquirator().getDescription() != null) {

            return tefwebCallback.getSales().get(0).getTransactions().get(0)
                    .getEletronicTransactionData().getAcquirator().getDescription();
        }
        else {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Dados Incorretos")
                    .details("O campo Description está incorreto")
                    .errorCode(HttpStatus.BAD_REQUEST.toString())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .timestamp(Instant.now())
                    .build();
            throw new BusinessException(error);
        }
    }

    private String getAuthorizationId(TefWebCallbackRequest tefwebCallback) {

        if (tefwebCallback.getSales() != null && !tefwebCallback.getSales().isEmpty()
                && tefwebCallback.getSales().get(0) != null
                && tefwebCallback.getSales().get(0).getTransactions() != null
                && !tefwebCallback.getSales().get(0).getTransactions().isEmpty()
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData() != null
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                .getHostNsu() != null){

            return tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                    .getHostNsu();
        }
        else {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Dados Incorretos")
                    .details("O campo HostNsu está incorretos")
                    .errorCode(HttpStatus.BAD_REQUEST.toString())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .timestamp(Instant.now())
                    .build();
            throw new BusinessException(error);
        }
    }

    private String getApprovedId(TefWebCallbackRequest tefwebCallback){
        if (tefwebCallback.getSales() != null && !tefwebCallback.getSales().isEmpty()
                && tefwebCallback.getSales().get(0) != null
                && tefwebCallback.getSales().get(0).getTransactions() != null
                && !tefwebCallback.getSales().get(0).getTransactions().isEmpty()
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData() != null
                && tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                .getIdSitef() != null){

            return tefwebCallback.getSales().get(0).getTransactions().get(0)
                    .getEletronicTransactionData().getIdSitef();
        }
        else {
            ErrorResponse error = ErrorResponse.builder()
                    .message("Dados Incorretos")
                    .details("O campo IdSitef está incorretos")
                    .errorCode(HttpStatus.BAD_REQUEST.toString())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .timestamp(Instant.now())
                    .build();
            throw new BusinessException(error);
        }
    }
}