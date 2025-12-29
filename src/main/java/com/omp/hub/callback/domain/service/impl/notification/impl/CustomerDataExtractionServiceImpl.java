package com.omp.hub.callback.domain.service.impl.notification.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.customer.CustomerMobileResponse;
import com.omp.hub.callback.domain.model.dto.customer.ExtractedCustomerDataDTO;
import com.omp.hub.callback.domain.model.dto.customer.SubscriberDTO;
import com.omp.hub.callback.domain.model.dto.customer.billing.MobileBillingDetailsResponse;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.information.PaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.recurring.DataRecurringDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.ContractSingleDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.CustomerSingleDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.DataSingleDTO;
import com.omp.hub.callback.domain.model.dto.pix.forms.PixAutoRequest;
import com.omp.hub.callback.domain.ports.client.CustomerContractsSubscribersPort;
import com.omp.hub.callback.domain.ports.client.CustomerMobilePort;
import com.omp.hub.callback.domain.ports.client.InformationPaymentPort;
import com.omp.hub.callback.domain.ports.client.MobileBillingDetailsPort;
import com.omp.hub.callback.domain.service.impl.notification.CustomerDataExtractionService;
import com.omp.hub.callback.domain.service.impl.notification.util.CustomerContactExtractorUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerDataExtractionServiceImpl implements CustomerDataExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerDataExtractionServiceImpl.class);

    private final InformationPaymentPort paymentPort;
    private final CustomerMobilePort customerMobilePort;
    private final MobileBillingDetailsPort mobileBillingDetailsPort;
    private final CustomerContractsSubscribersPort customerContractsSubscribersPort;
    private final CustomerContactExtractorUtil customerContactExtractorUtil;
    private final ObjectMapper objectMapper;
    private final InformationPaymentPort informationPort;

    @Override
    public ExtractedCustomerDataDTO extractCustomerDataFromPaymentInfo(UUID uuid, String identifier) {
        logger.info("TxId: {} - Extraindo dados do cliente", identifier);

        try {

            if (identifier != null) {
//                InformationPaymentDTO paymentInfo = paymentPort.getInfo(uuid, identifier);
                InformationPaymentDTO paymentInfo = informationPort.sendFindByIdentifier(identifier);

                logger.info("PAYMENT: {}", objectMapper.writeValueAsString(paymentInfo));

                if (paymentInfo.getPayments() != null && !paymentInfo.getPayments().isEmpty()) {
                    PaymentDTO payment = paymentInfo.getPayments().get(0);

                    if (payment.getPixAuto() != null) {

                        String data = (String) payment.getPixAuto();
                        PixAutoRequest pixAutoRequest = objectMapper.readValue(data, PixAutoRequest.class);

                        String name = "";
                        String cpf = "";
                        String cnpj = "";
                        String mobileBan = "";
                        String contractNumber = "";
                        String operatorCode = "";
                        String cityCode = "";

                        if (pixAutoRequest.getContract() != null  && pixAutoRequest.getContract().getDebtor() != null) {
                            if (pixAutoRequest.getContract().getDebtor().getName() != null)
                                name = pixAutoRequest.getContract().getDebtor().getName();
                            if (pixAutoRequest.getContract().getDebtor().getDocument() != null
                                && pixAutoRequest.getContract().getDebtor().getDocument().getType() != null
                                && "CPF".equals(pixAutoRequest.getContract().getDebtor().getDocument().getType())) {
                                cpf = pixAutoRequest.getContract().getDebtor().getDocument().getNumber();
                            }
                            if (pixAutoRequest.getContract().getDebtor().getDocument() != null
                                    && pixAutoRequest.getContract().getDebtor().getDocument().getType() != null
                                    && "CNPJ".equals(pixAutoRequest.getContract().getDebtor().getDocument().getType())) {
                                cnpj = pixAutoRequest.getContract().getDebtor().getDocument().getNumber();
                            }
                            if (pixAutoRequest.getContract().getMobileBan() != null)
                                mobileBan = pixAutoRequest.getContract().getMobileBan();
                            if (pixAutoRequest.getContract().getContractNumber() != null) {
                                contractNumber = pixAutoRequest.getContract().getContractNumber();
                                operatorCode = pixAutoRequest.getContract().getOperatorCode();
                                cityCode = pixAutoRequest.getContract().getCityCode();
                            }
                        }
                        else if (pixAutoRequest.getDebtor() != null) {

                            if (pixAutoRequest.getDebtor().getName() != null)
                                name = pixAutoRequest.getDebtor().getName();
                            if (pixAutoRequest.getDebtor().getCpf() != null)
                                cpf = pixAutoRequest.getDebtor().getCpf();
                            if (pixAutoRequest.getDebtor().getCnpj() != null)
                                cnpj = pixAutoRequest.getDebtor().getCnpj();
                        }

                        CustomerSingleDTO customer = CustomerSingleDTO.builder()
                            .name(name)
                            .cpf(cpf)
                            .cnpj(cnpj)
                            .contract(ContractSingleDTO.builder()
                                .mobileBan(mobileBan)
                                .contractNumber(contractNumber)
                                .cityCode(cityCode)
                                .operatorCode(operatorCode)
                                .build())
                            .build();

                        return buildExtractedData(customer, customer.getContract());
                    }
                    else if (payment.getJourney() != null) {
                        String data = (String) payment.getJourney();

                        logger.debug("TxId: {} - Extraindo dados do cliente. Data recebida", identifier);

                        // Tenta deserializar como DataSingleDTO primeiro
                        try {
                            DataSingleDTO dto = objectMapper.readValue(data, DataSingleDTO.class);
                            if (dto != null && dto.getCustomer() != null) {
                                return buildExtractedData(dto.getCustomer(), dto.getCustomer().getContract());
                            }
                        } catch (Exception e) {
                            logger.debug("Não foi possível deserializar como DataSingleDTO: {}", e.getMessage());
                        }

                        // Tenta deserializar como DataRecurringDTO
                        try {
                            DataRecurringDTO dto = objectMapper.readValue(data, DataRecurringDTO.class);
                            if (dto != null && dto.getCustomer() != null) {
                                return buildExtractedData(dto.getCustomer(), dto.getCustomer().getContract());
                            }
                        } catch (Exception e) {
                            logger.debug("Não foi possível deserializar como DataRecurringDTO: {}", e.getMessage());
                        }

                        throw new BusinessException(
                                "Erro ao deserializar dados do cliente",
                                "PAYMENT_INFO_ERROR",
                                String.format("Não foi possível deserializar os dados do cliente. Data: %s", data),
                                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }

                throw new BusinessException("PaymentInfo não possui payments ou a lista está vazia",
                        "PAYMENT_INFO_EMPTY",
                        "Erro ao extrair dados do cliente dos dados do pagamento",
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("TxId: {} - Erro ao extrair dados do cliente dos dados do pagamento: {}", identifier, e.getMessage(), e);
            throw new BusinessException("Erro ao extrair dados do cliente dos dados do pagamento: " + e.getMessage(),
                    "PAYMENT_INFO_ERROR",
                    e.getMessage(),
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return null;
    }

    @Override
    public ExtractedCustomerDataDTO enrichCustomerData(UUID uuid, ExtractedCustomerDataDTO customerData) {
        if (customerData == null || !customerData.hasCompleteData()) {
            logger.warn("Dados do cliente são nulos ou incompletos para enriquecimento");
            return customerData;
        }

        String segment = customerData.getSegment() != null ? customerData.getSegment() : null;
        String document = customerData.getCpf() != null ? customerData.getCpf() : customerData.getCnpj();
        
        logger.info("Enriquecendo dados do cliente - Segmento: {}", segment);

        if ("CLARO_RESIDENCIAL".equals(segment) ||
                (customerData.getContractNumber() != null && !customerData.getContractNumber().isBlank()
                        && customerData.getOperatorCode() != null && !customerData.getOperatorCode().isBlank()
                        && customerData.getCityCode() != null && !customerData.getCityCode().isBlank())) {
            return enrichResidentialCustomerData(uuid, customerData, document);
        }
        else if ("CLARO_MOVEL".equals(segment) || (customerData.getMobileBan() != null
                && !customerData.getMobileBan().isBlank())){
            return enrichMobileCustomerData(uuid, customerData, document);
        }


        logger.warn("Segmento '{}' não suportado para enriquecimento", segment);
        return customerData;
    }

    private ExtractedCustomerDataDTO buildExtractedData(Object customer, Object contract) {
        try {
            String name = getFieldValue(customer, "name");
            String cpf = getFieldValue(customer, "cpf");
            String cnpj = getFieldValue(customer, "cnpj");
            String segment = getFieldValue(customer, "segment");
            
            String mobileBan = null;
            String contractNumber = null;
            String operatorCode = null;
            String cityCode = null;

            if (contract != null) {
                mobileBan = getFieldValue(contract, "mobileBan");
                contractNumber = getFieldValue(contract, "contractNumber");
                operatorCode = getFieldValue(contract, "operatorCode");
                cityCode = getFieldValue(contract, "cityCode");
            }

            ExtractedCustomerDataDTO extractedData = ExtractedCustomerDataDTO.builder()
                    .name(name)
                    .cpf(cpf)
                    .cnpj(cnpj)
                    .segment(segment)
                    .mobileBan(mobileBan)
                    .contractNumber(contractNumber)
                    .operatorCode(operatorCode)
                    .cityCode(cityCode)
                    .build();

            logger.info("Dados do cliente extraídos com sucesso: {}",
                    extractedData.hasCompleteData() ? "completos" : "incompletos");
            
            return extractedData;
        } catch (Exception e) {
            logger.error("Erro ao construir dados extraídos: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao construir dados do cliente", "CUSTOMER_DATA_BUILD_ERROR", 
                    e.getMessage(), org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ExtractedCustomerDataDTO enrichMobileCustomerData(UUID uuid, ExtractedCustomerDataDTO customerData, String document) {
        logger.info("Enriquecendo dados do cliente móvel - mobileBan: {}", customerData.getMobileBan());
        
        String email = null;
        String msisdn = null;
        boolean criteriosAtendidos = false;

        try {
            if (customerData.hasValidMobileBan()) {
                MobileBillingDetailsResponse billingResponse = mobileBillingDetailsPort.getCustomerBillingDetailsByMobileBan(uuid, customerData.getMobileBan());
                
                if (billingResponse != null && billingResponse.getData() != null && billingResponse.getData().getCustomer() != null) {
                    email = billingResponse.getData().getCustomer().getContactEmail();
                    logger.debug("Email obtido via billing details: {}", email != null ? "***@***.***" : "null");
                }
            }
        } catch (Exception e) {
            logger.warn("Erro ao buscar email via billing details para cliente móvel - mobileBan: {}, erro: {}", 
                    customerData.getMobileBan(), e.getMessage());
        }

        try {
            CustomerMobileResponse subscribersResponse = customerMobilePort.send(uuid, document, "ATIVO");
            
            if (subscribersResponse != null && subscribersResponse.getData() != null 
                    && subscribersResponse.getData().getSubscribers() != null 
                    && !subscribersResponse.getData().getSubscribers().isEmpty()) {

                SubscriberDTO subscriber = subscribersResponse.getData().getSubscribers().get(0);
                
                boolean mobileBanCorreto = subscriber.getCustomer() != null 
                        && subscriber.getCustomer().getAccount() != null
                        && customerData.getMobileBan().equals(subscriber.getCustomer().getAccount().getMobileBan());
                boolean nomeCorreto = subscriber.getName() != null 
                        && customerData.getName() != null
                        && subscriber.getName().equalsIgnoreCase(customerData.getName());

                logger.info("Critérios de validação -  MobileBan customer: '{}', MobileBan subscribe: '{}'",
                        customerData.getMobileBan(), subscriber.getCustomer().getAccount().getMobileBan());
                logger.info("Critérios de validação -  Nome customer: '{}', Nome subscribe: '{}'",
                        customerData.getName(), subscriber.getName());
                logger.info("Critérios de validação -  MobileBan correto: {}, Nome correto: {}",
                        mobileBanCorreto, nomeCorreto);
                
                if (mobileBanCorreto && nomeCorreto) {
                    msisdn = subscriber.getMsisdn();
                    criteriosAtendidos = true;
                    logger.info("Critérios atendidos - MSISDN obtido: {}", msisdn != null ? "***" + msisdn.substring(msisdn.length()-4) : "null");
                } else {
                    logger.warn("Critérios NÃO atendidos para cliente móvel - mobileBan: {}", 
                            customerData.getMobileBan());
                }
            }
        } catch (Exception e) {
            logger.warn("Erro ao buscar MSISDN via mobile-subscribers para cliente móvel, erro: {}", 
                    e.getMessage());
        }

        return customerData.toBuilder()
                .email(email)
                .msisdn(msisdn)
                .criteriosAtendidos(criteriosAtendidos)
                .build();
    }

    private ExtractedCustomerDataDTO enrichResidentialCustomerData(UUID uuid, ExtractedCustomerDataDTO customerData, String document) {
        logger.info("Enriquecendo dados do cliente residencial - contractNumber: {}", customerData.getContractNumber());
        
        String email = null;
        String msisdn = null;
        boolean criteriosAtendidos = false;

        try {
            var contractsResponse = customerContractsSubscribersPort.send(uuid, document, customerData);
            
            if (contractsResponse != null && contractsResponse.getData() != null 
                    && contractsResponse.getData().getContracts() != null 
                    && !contractsResponse.getData().getContracts().isEmpty()) {
                
                var contract = contractsResponse.getData().getContracts().get(0);
                
                String status = contract.getStatus();
                String contractId = contract.getContractId();
                String contractFirstName = contract.getFirstName();
                String extractedFirstName = customerData.getName();

                logger.debug("Validando critérios residencial - Status: {}, ContractId: {}, FirstName: {}", 
                        status, contractId, contractFirstName);
                
                boolean statusCorreto = "CONECTADO".equals(status);
                boolean contractCorreto = contractId != null && contractId.equals(customerData.getContractNumber());
                boolean nomeCorreto = contractFirstName != null && extractedFirstName != null
                        && contractFirstName.equalsIgnoreCase(extractedFirstName);

                logger.debug("Critérios de validação - Status correto: {}, Contract correto: {}, Nome correto: {}", 
                         statusCorreto, contractCorreto, nomeCorreto);

                if (statusCorreto && contractCorreto && nomeCorreto) {
                    email = contract.getEmailAddress();
                    msisdn = customerContactExtractorUtil.extractMobilePhoneFromContracts(contractsResponse);
                    
                    criteriosAtendidos = true;
                    logger.info("Critérios residencial atendidos - Email obtido: {}, MSISDN obtido: {}", 
                            email != null ? "***@***.***" : "null", 
                            msisdn != null ? "***" + msisdn.substring(msisdn.length()-4) : "null");
                } else {
                    logger.warn("Critérios residencial NÃO atendidos - contractNumber: {}", 
                            customerData.getContractNumber());
                }
            }
        } catch (Exception e) {
            logger.warn("Erro ao buscar dados via customer-contracts-subscribers para cliente residencial, erro: {}", 
                    e.getMessage());
        }

        return customerData.toBuilder()
                .email(email)
                .msisdn(msisdn)
                .criteriosAtendidos(criteriosAtendidos)
                .build();
    }

    private String getFieldValue(Object obj, String fieldName) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            logger.debug("Campo '{}' não encontrado ou inacessível no objeto", fieldName);
            return null;
        }
    }
}