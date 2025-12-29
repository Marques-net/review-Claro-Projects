package com.omp.hub.callback.domain.service.validation.impl;

import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.DataDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.DetailDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.PosInfoDTO;
import com.omp.hub.callback.domain.model.dto.sap.billing.payments.SapBillingPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.payments.CustomerDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.ItemDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.OrderReceiptDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsRequest;
import com.omp.hub.callback.domain.model.dto.sap.redemptions.SapRedemptionsRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SapRequestValidationServiceImplTest {

    private SapRequestValidationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SapRequestValidationServiceImpl();
    }

    @Test
    void validateRedemptionsRequest_WithValidRequest_ShouldNotThrowException() {
        SapRedemptionsRequest request = createValidRedemptionsRequest();

        assertDoesNotThrow(() -> service.validateRedemptionsRequest(request));
    }

    @Test
    void validateRedemptionsRequest_WithNullRequest_ShouldThrowBusinessException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateRedemptionsRequest(null));

        assertEquals("Request inválido", exception.getError().getMessage());
        assertEquals("INVALID_REQUEST", exception.getError().getErrorCode());
        assertEquals("Request não pode ser nulo", exception.getError().getDetails());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getError().getStatus());
    }

    @Test
    void validateRedemptionsRequest_WithNullData_ShouldThrowBusinessException() {
        SapRedemptionsRequest request = new SapRedemptionsRequest();
        request.setData(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateRedemptionsRequest(request));

        assertEquals("Campo obrigatório ausente", exception.getError().getMessage());
        assertEquals("MISSING_FIELD", exception.getError().getErrorCode());
        assertEquals("Campo 'data' é obrigatório para Redemptions", exception.getError().getDetails());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getError().getStatus());
    }

    @Test
    void validateRedemptionsRequest_WithNullOrder_ShouldThrowBusinessException() {
        SapRedemptionsRequest request = new SapRedemptionsRequest();
        request.setData(new com.omp.hub.callback.domain.model.dto.sap.redemptions.DataDTO());
        request.getData().setOrder(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateRedemptionsRequest(request));

        assertEquals("Campo obrigatório ausente", exception.getError().getMessage());
        assertEquals("MISSING_FIELD", exception.getError().getErrorCode());
        assertEquals("Campo 'data.order' é obrigatório para Redemptions", exception.getError().getDetails());
    }

    @Test
    void validateRedemptionsRequest_WithNullOrderId_ShouldThrowBusinessException() {
        SapRedemptionsRequest request = createValidRedemptionsRequest();
        request.getData().getOrder().setId(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateRedemptionsRequest(request));

        assertEquals("Campo 'data.order.id' é obrigatório para Redemptions", exception.getError().getDetails());
    }

    @Test
    void validateRedemptionsRequest_WithEmptyOrderId_ShouldThrowBusinessException() {
        SapRedemptionsRequest request = createValidRedemptionsRequest();
        request.getData().getOrder().setId("   ");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateRedemptionsRequest(request));

        assertEquals("Campo 'data.order.id' é obrigatório para Redemptions", exception.getError().getDetails());
    }

    @Test
    void validateRedemptionsRequest_WithNullCompanyId_ShouldThrowBusinessException() {
        SapRedemptionsRequest request = createValidRedemptionsRequest();
        request.getData().getOrder().setCompanyId(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateRedemptionsRequest(request));

        assertEquals("Campo 'data.order.companyId' é obrigatório para Redemptions", exception.getError().getDetails());
    }

    @Test
    void validateRedemptionsRequest_WithEmptyCompanyId_ShouldThrowBusinessException() {
        SapRedemptionsRequest request = createValidRedemptionsRequest();
        request.getData().getOrder().setCompanyId("");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateRedemptionsRequest(request));

        assertEquals("Campo 'data.order.companyId' é obrigatório para Redemptions", exception.getError().getDetails());
    }

    @Test
    void validateRedemptionsRequest_WithNullBusinessLocationId_ShouldThrowBusinessException() {
        SapRedemptionsRequest request = createValidRedemptionsRequest();
        request.getData().getOrder().setBusinessLocationId(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateRedemptionsRequest(request));

        assertEquals("Campo 'data.order.businessLocationId' é obrigatório para Redemptions", exception.getError().getDetails());
    }

    @Test
    void validateRedemptionsRequest_WithNullType_ShouldThrowBusinessException() {
        SapRedemptionsRequest request = createValidRedemptionsRequest();
        request.getData().getOrder().setType(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateRedemptionsRequest(request));

        assertEquals("Campo 'data.order.type' é obrigatório para Redemptions", exception.getError().getDetails());
    }

    @Test
    void validateRedemptionsRequest_WithNullPosInfo_ShouldThrowBusinessException() {
        SapRedemptionsRequest request = createValidRedemptionsRequest();
        request.getData().getOrder().setPosInfo(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateRedemptionsRequest(request));

        assertEquals("Campo 'data.order.posInfo' é obrigatório para Redemptions", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithValidRequest_ShouldNotThrowException() {
        SapPaymentsRequest request = createValidPaymentsRequest();

        assertDoesNotThrow(() -> service.validatePaymentsRequest(request));
    }

    @Test
    void validatePaymentsRequest_WithNullRequest_ShouldThrowBusinessException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(null));

        assertEquals("Request inválido", exception.getError().getMessage());
        assertEquals("INVALID_REQUEST", exception.getError().getErrorCode());
        assertEquals("Request não pode ser nulo", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullData_ShouldThrowBusinessException() {
        SapPaymentsRequest request = new SapPaymentsRequest();
        request.setData(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullOrder_ShouldThrowBusinessException() {
        SapPaymentsRequest request = new SapPaymentsRequest();
        request.setData(new com.omp.hub.callback.domain.model.dto.sap.payments.DataDTO());
        request.getData().setOrder(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullOrderId_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setId(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.id' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullCompanyId_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setCompanyId(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.companyId' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullBusinessLocationId_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setBusinessLocationId(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.businessLocationId' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullPosInfo_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setPosInfo(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.posInfo' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullSalesCategory_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setSalesCategory(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.salesCategory' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullSalesType_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setSalesType(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.salesType' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullSalesDate_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setSalesDate(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.salesDate' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullSalesTime_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setSalesTime(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.salesTime' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullTotalAmountReceived_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setTotalAmountReceived(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.totalAmountReceived' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullCustomer_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setCustomer(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.customer' é obrigatório para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullItems_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setItems(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.items' é obrigatório e não pode ser vazio para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithEmptyItems_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setItems(new ArrayList<>());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.items' é obrigatório e não pode ser vazio para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithNullOrderReceipts_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setOrderReceipts(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.orderReceipts' é obrigatório e não pode ser vazio para Payments", exception.getError().getDetails());
    }

    @Test
    void validatePaymentsRequest_WithEmptyOrderReceipts_ShouldThrowBusinessException() {
        SapPaymentsRequest request = createValidPaymentsRequest();
        request.getData().getOrder().setOrderReceipts(new ArrayList<>());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validatePaymentsRequest(request));

        assertEquals("Campo 'data.order.orderReceipts' é obrigatório e não pode ser vazio para Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithValidRequest_ShouldNotThrowException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();

        assertDoesNotThrow(() -> service.validateBillingPaymentsRequest(request));
    }

    @Test
    void validateBillingPaymentsRequest_WithNullRequest_ShouldThrowBusinessException() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(null));

        assertEquals("Request inválido", exception.getError().getMessage());
        assertEquals("INVALID_REQUEST", exception.getError().getErrorCode());
        assertEquals("Request não pode ser nulo", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithNullData_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = new SapBillingPaymentsRequest();
        request.setData(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data' é obrigatório para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithNullPayment_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = new SapBillingPaymentsRequest();
        request.setData(DataDTO.builder().payment(null).build());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment' é obrigatório para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithNullCompany_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();
        request.getData().getPayment().setCompany(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment.company' é obrigatório para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithEmptyCompany_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();
        request.getData().getPayment().setCompany("  ");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment.company' é obrigatório para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithNullBusinessLocation_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();
        request.getData().getPayment().setBusinessLocation(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment.businessLocation' é obrigatório para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithNullIdentification_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();
        request.getData().getPayment().setIdentification(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment.identification' é obrigatório para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithNullPosInfo_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();
        request.getData().getPayment().setPosInfo(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment.posInfo' é obrigatório para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithNullCustomerName_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();
        request.getData().getPayment().setCustomerName(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment.customerName' é obrigatório para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithNullDate_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();
        request.getData().getPayment().setDate(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment.date' é obrigatório para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithNullValue_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();
        request.getData().getPayment().setValue(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment.value' é obrigatório para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithNullUsername_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();
        request.getData().getPayment().setUsername(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment.username' é obrigatório para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithNullDetails_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();
        request.getData().getPayment().setDetails(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment.details' é obrigatório e não pode ser vazio para Billing Payments", exception.getError().getDetails());
    }

    @Test
    void validateBillingPaymentsRequest_WithEmptyDetails_ShouldThrowBusinessException() {
        SapBillingPaymentsRequest request = createValidBillingPaymentsRequest();
        request.getData().getPayment().setDetails(new ArrayList<>());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateBillingPaymentsRequest(request));

        assertEquals("Campo 'data.payment.details' é obrigatório e não pode ser vazio para Billing Payments", exception.getError().getDetails());
    }

    private SapRedemptionsRequest createValidRedemptionsRequest() {
        SapRedemptionsRequest request = new SapRedemptionsRequest();
        com.omp.hub.callback.domain.model.dto.sap.redemptions.DataDTO data = new com.omp.hub.callback.domain.model.dto.sap.redemptions.DataDTO();
        com.omp.hub.callback.domain.model.dto.sap.redemptions.OrderDTO order = new com.omp.hub.callback.domain.model.dto.sap.redemptions.OrderDTO();
        
        order.setId("ORD123");
        order.setCompanyId("COMP456");
        order.setBusinessLocationId("LOC789");
        order.setType("REDEMPTION");
        order.setPosInfo(new com.omp.hub.callback.domain.model.dto.sap.redemptions.PosInfoDTO());
        
        data.setOrder(order);
        request.setData(data);
        
        return request;
    }

    private SapPaymentsRequest createValidPaymentsRequest() {
        SapPaymentsRequest request = new SapPaymentsRequest();
        com.omp.hub.callback.domain.model.dto.sap.payments.DataDTO data = new com.omp.hub.callback.domain.model.dto.sap.payments.DataDTO();
        com.omp.hub.callback.domain.model.dto.sap.payments.OrderDTO order = new com.omp.hub.callback.domain.model.dto.sap.payments.OrderDTO();
        
        order.setId("ORD123");
        order.setCompanyId("COMP456");
        order.setBusinessLocationId("LOC789");
        order.setPosInfo(com.omp.hub.callback.domain.model.dto.sap.payments.PosInfoDTO.builder()
                .componentNumber("123")
                .version("1.0")
                .transactionId("TX123")
                .taxCouponNumber("TC123")
                .build());
        order.setSalesCategory("CATEGORY1");
        order.setSalesType("TYPE1");
        order.setSalesDate("2024-01-01");
        order.setSalesTime("10:30:00");
        order.setTotalAmountReceived("100.00");
        order.setCustomer(new CustomerDTO());
        
        List<ItemDTO> items = new ArrayList<>();
        items.add(new ItemDTO());
        order.setItems(items);
        
        List<OrderReceiptDTO> receipts = new ArrayList<>();
        receipts.add(new OrderReceiptDTO());
        order.setOrderReceipts(receipts);
        
        data.setOrder(order);
        request.setData(data);
        
        return request;
    }

    private SapBillingPaymentsRequest createValidBillingPaymentsRequest() {
        SapBillingPaymentsRequest request = new SapBillingPaymentsRequest();
        
        PaymentDTO payment = new PaymentDTO();
        payment.setCompany("COMP123");
        payment.setBusinessLocation("LOC456");
        payment.setIdentification("ID789");
        payment.setPosInfo(new PosInfoDTO());
        payment.setCustomerName("John Doe");
        payment.setDate("2024-01-01");
        payment.setValue("100.00");
        payment.setUsername("user123");
        
        List<DetailDTO> details = new ArrayList<>();
        details.add(new DetailDTO());
        payment.setDetails(details);
        
        DataDTO data = DataDTO.builder()
                .payment(payment)
                .build();
        
        request.setData(data);
        
        return request;
    }
}
