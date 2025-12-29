package com.omp.hub.callback.domain.model.dto.claro;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerContractsSubscribersContract {
    
    @JsonProperty("cpfcnpj")
    private String cpfcnpj;
    
    @JsonProperty("docType")
    private String docType;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("companyName")
    private String companyName;
    
    @JsonProperty("partyRoleId")
    private String partyRoleId;
    
    @JsonProperty("operatorCityCode")
    private String operatorCityCode;
    
    @JsonProperty("operatorCode")
    private String operatorCode;
    
    @JsonProperty("operatorPartyId")
    private String operatorPartyId;
    
    @JsonProperty("customerAccountId")
    private String customerAccountId;
    
    @JsonProperty("contractId")
    private String contractId;
    
    @JsonProperty("contractIdEbt")
    private String contractIdEbt;
    
    @JsonProperty("creationDate")
    private String creationDate;
    
    @JsonProperty("accountType")
    private String accountType;
    
    @JsonProperty("statusCode")
    private String statusCode;
    
    @JsonProperty("statusCodeEbt")
    private String statusCodeEbt;
    
    @JsonProperty("statusDate")
    private String statusDate;
    
    @JsonProperty("statusDateEbt")
    private String statusDateEbt;
    
    @JsonProperty("statusQtdeDays")
    private String statusQtdeDays;
    
    @JsonProperty("statusQtdeDaysEbt")
    private String statusQtdeDaysEbt;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("statusEbt")
    private String statusEbt;
    
    @JsonProperty("financialStatus")
    private String financialStatus;
    
    @JsonProperty("personTypeCode")
    private String personTypeCode;
    
    @JsonProperty("personType")
    private String personType;
    
    @JsonProperty("segmentCode")
    private String segmentCode;
    
    @JsonProperty("segment")
    private String segment;
    
    @JsonProperty("birthDate")
    private String birthDate;
    
    @JsonProperty("openingDate")
    private String openingDate;
    
    @JsonProperty("emailAddress")
    private String emailAddress;
    
    @JsonProperty("paymentDayDue")
    private String paymentDayDue;
    
    @JsonProperty("paymentMethodCode")
    private String paymentMethodCode;
    
    @JsonProperty("paymentMethod")
    private String paymentMethod;
    
    @JsonProperty("profileCode")
    private String profileCode;
    
    @JsonProperty("profileName")
    private String profileName;
    
    @JsonProperty("installAddressId")
    private String installAddressId;
    
    @JsonProperty("postCode")
    private String postCode;
    
    @JsonProperty("propertyHpId")
    private String propertyHpId;
    
    @JsonProperty("propertyCode")
    private String propertyCode;
    
    @JsonProperty("networkNodeCode")
    private String networkNodeCode;
    
    @JsonProperty("isEncoded")
    private Boolean isEncoded;
    
    @JsonProperty("networkCellCode")
    private String networkCellCode;
    
    @JsonProperty("isJuridicalSegment")
    private Boolean isJuridicalSegment;
    
    @JsonProperty("hasAnalogicalPayTV")
    private Boolean hasAnalogicalPayTV;
    
    @JsonProperty("hasDigitalPayTV")
    private Boolean hasDigitalPayTV;
    
    @JsonProperty("hasInternet")
    private Boolean hasInternet;
    
    @JsonProperty("hasNetFone")
    private Boolean hasNetFone;
    
    @JsonProperty("hasCloud")
    private Boolean hasCloud;
    
    @JsonProperty("isAllowedPPVPurchase")
    private Boolean isAllowedPPVPurchase;
    
    @JsonProperty("cdBase")
    private String cdBase;
    
    @JsonProperty("phones")
    private List<CustomerContractsSubscribersPhone> phones;
}