package com.omp.hub.callback.domain.service.validation.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.SapRedemptionsRequest;
import com.omp.hub.callback.domain.service.validation.SapRequestValidationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SapRequestValidationServiceImpl implements SapRequestValidationService {

    @Override
    public void validateRedemptionsRequest(SapRedemptionsRequest request) {
        if (request == null) {
            throw new BusinessException("Request inválido", "INVALID_REQUEST", 
                    "Request não pode ser nulo", HttpStatus.BAD_REQUEST);
        }

        if (request.getData() == null) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data' é obrigatório para Redemptions", HttpStatus.BAD_REQUEST);
        }

        if (request.getData().getOrder() == null) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order' é obrigatório para Redemptions", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getId())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.id' é obrigatório para Redemptions", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getCompanyId())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.companyId' é obrigatório para Redemptions", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getBusinessLocationId())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.businessLocationId' é obrigatório para Redemptions", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getType())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.type' é obrigatório para Redemptions", HttpStatus.BAD_REQUEST);
        }

        if (request.getData().getOrder().getPosInfo() == null) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.posInfo' é obrigatório para Redemptions", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void validatePaymentsRequest(SapPaymentsRequest request) {
        if (request == null) {
            throw new BusinessException("Request inválido", "INVALID_REQUEST", 
                    "Request não pode ser nulo", HttpStatus.BAD_REQUEST);
        }

        if (request.getData() == null) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (request.getData().getOrder() == null) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getId())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.id' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getCompanyId())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.companyId' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getBusinessLocationId())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.businessLocationId' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (request.getData().getOrder().getPosInfo() == null) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.posInfo' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getSalesCategory())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.salesCategory' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getSalesType())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.salesType' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getSalesDate())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.salesDate' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getSalesTime())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.salesTime' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getOrder().getTotalAmountReceived())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.totalAmountReceived' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (request.getData().getOrder().getCustomer() == null) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.customer' é obrigatório para Payments", HttpStatus.BAD_REQUEST);
        }

        if (request.getData().getOrder().getItems() == null || request.getData().getOrder().getItems().isEmpty()) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.items' é obrigatório e não pode ser vazio para Payments", HttpStatus.BAD_REQUEST);
        }

        if (request.getData().getOrder().getOrderReceipts() == null || request.getData().getOrder().getOrderReceipts().isEmpty()) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.order.orderReceipts' é obrigatório e não pode ser vazio para Payments", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void validateBillingPaymentsRequest(SapBillingPaymentsRequest request) {
        if (request == null) {
            throw new BusinessException("Request inválido", "INVALID_REQUEST", 
                    "Request não pode ser nulo", HttpStatus.BAD_REQUEST);
        }

        if (request.getData() == null) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data' é obrigatório para Billing Payments", HttpStatus.BAD_REQUEST);
        }

        if (request.getData().getPayment() == null) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.payment' é obrigatório para Billing Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getPayment().getCompany())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.payment.company' é obrigatório para Billing Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getPayment().getBusinessLocation())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.payment.businessLocation' é obrigatório para Billing Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getPayment().getIdentification())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.payment.identification' é obrigatório para Billing Payments", HttpStatus.BAD_REQUEST);
        }

        if (request.getData().getPayment().getPosInfo() == null) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.payment.posInfo' é obrigatório para Billing Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getPayment().getCustomerName())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.payment.customerName' é obrigatório para Billing Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getPayment().getDate())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.payment.date' é obrigatório para Billing Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getPayment().getValue())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.payment.value' é obrigatório para Billing Payments", HttpStatus.BAD_REQUEST);
        }

        if (isNullOrEmpty(request.getData().getPayment().getUsername())) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.payment.username' é obrigatório para Billing Payments", HttpStatus.BAD_REQUEST);
        }

        if (request.getData().getPayment().getDetails() == null || request.getData().getPayment().getDetails().isEmpty()) {
            throw new BusinessException("Campo obrigatório ausente", "MISSING_FIELD", 
                    "Campo 'data.payment.details' é obrigatório e não pode ser vazio para Billing Payments", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
