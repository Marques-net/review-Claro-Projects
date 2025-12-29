package com.omp.hub.callback.domain.model.dto.customer.residential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResidentialCustomerResponse {

    private String apiVersion;
    private String transactionId;
    private ResidentialCustomerData data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResidentialCustomerData {
        private Integer pageNumber;
        private Integer pageSize;
        private Integer totalPages;
        private Integer totalRecords;
        private List<ResidentialContract> contracts;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResidentialContract {
        private String contractCode;
        private ResidentialCustomer customer;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResidentialCustomer {
        private String documentNumber;
        private String emailAddress;
        private String name;
        private String phoneNumber;
    }
}
