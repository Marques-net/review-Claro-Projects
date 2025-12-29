package com.omp.hub.callback.domain.service.impl.notification.util;

import java.util.List;

import org.springframework.stereotype.Component;

import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersContract;
import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersPhone;
import com.omp.hub.callback.domain.model.dto.claro.CustomerContractsSubscribersResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomerContactExtractorUtil {

    private static final String MOBILE_PHONE_ROLE = "Mobile Phone";

    public String extractMobilePhoneFromContracts(CustomerContractsSubscribersResponse response) {
        if (response == null || response.getData() == null || response.getData().getContracts() == null) {
            log.warn("Response ou contratos são nulos na busca de telefone móvel");
            return null;
        }

        List<CustomerContractsSubscribersContract> contracts = response.getData().getContracts();
        
        for (CustomerContractsSubscribersContract contract : contracts) {
            if (contract.getPhones() != null) {
                for (CustomerContractsSubscribersPhone phone : contract.getPhones()) {
                    if (MOBILE_PHONE_ROLE.equals(phone.getContactMediumRole())) {
                        String mobilePhone = phone.getTelephoneNumber();
                        log.debug("Telefone móvel encontrado: {}", mobilePhone);
                        return mobilePhone;
                    }
                }
            }
        }

        log.warn("Nenhum telefone móvel encontrado nos contratos");
        return null;
    }

    public String extractNameFromContracts(CustomerContractsSubscribersResponse contractsResponse) {
        if (contractsResponse == null || contractsResponse.getData() == null || contractsResponse.getData().getContracts() == null) {
            log.warn("Response ou contratos são nulos na busca de nome");
            return null;
        }

        List<CustomerContractsSubscribersContract> contracts = contractsResponse.getData().getContracts();
        
        for (CustomerContractsSubscribersContract contract : contracts) {
            if ((contract.getFirstName() != null && !contract.getFirstName().isEmpty()) && (contract.getLastName() != null && !contract.getLastName().isEmpty())) {
                String name = contract.getFirstName() + " " + contract.getLastName();
                log.debug("Nome encontrado: {}", name);
                return name;
            }
        }

        log.warn("Nenhum nome encontrado nos contratos");
        return null;
        
    }
}