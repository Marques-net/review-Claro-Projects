package com.omp.hub.callback.domain.service.generate.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.CallbackDTO;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.information.InformationPaymentDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.DataSingleDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.PaymentDiscountDTO;
import com.omp.hub.callback.domain.model.dto.journey.single.ProductDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.CardDetailsDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.CustomerDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.DataDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.IdentificationDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.ItemDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.OrderDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.OrderReceiptDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.OthersDetailsDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.PosInfoDTO;
import com.omp.hub.callback.domain.model.dto.sap.payments.SapPaymentsRequest;
import com.omp.hub.callback.domain.service.generate.GenerateSapPaymentsRequestService;
import com.omp.hub.callback.domain.service.check.CheckTypeObjectService;
import com.omp.hub.callback.domain.enums.PaymentStatusEnum;
import com.omp.hub.callback.domain.enums.PaymentTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class GenerateSapPaymentRequestServiceImpl implements GenerateSapPaymentsRequestService {

    private static final Logger logger = LoggerFactory.getLogger(GenerateSapPaymentRequestServiceImpl.class);
    private static final String C_ATIV_SIMP = "ativacaosimplificada";
    private static final String C_SOLAR = "solar";

    @Value("${ativacao.simplificada.loja}")
    private String atvSimplLoja;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CheckTypeObjectService validateService;

    String identifier;

    public SapPaymentsRequest generateRequest(CallbackDTO request, InformationPaymentDTO info) {


        identifier = info.getIdentifier();
        UUID uuid = info.getUuid();
        DataSingleDTO dto = null;
        String callbackSTR = null;
        String date = null;
        String time = null;
        String value = null;
        String type = null;
        String cardNumber = null;
        String issuerCode = null;
        String issuerDescription = null;
        String valueAddedNetworkId = null;
        String valueAddedNetworkDescription = null;
        String store = null;
        String installments = null;
        String nsu = null;
        String transactionCupon = null;

        try {
            callbackSTR = mapper.writeValueAsString(request);

            if (info.getPayments() == null || info.getPayments().isEmpty()) {
                throw new BusinessException("Lista de pagamentos não pode ser nula ou vazia", "INVALID_PAYMENTS", "payments is null or empty", HttpStatus.BAD_REQUEST);
            }

            String data = info.getPayments().get(0).getJourney() != null
                    ? (String) info.getPayments().get(0).getJourney()
                    : null;
            dto = mapper.readValue(data, DataSingleDTO.class);

            TefWebCallbackRequest tefwebCallback = null;
            CreditCardCallbackRequest creditCallback = null;

            if (info.getStore() == null || "DEFAULT".equals(info.getStore())) {
                if (C_ATIV_SIMP.equals(info.getChannel()) || C_SOLAR.equals(info.getChannel())) {
                    store = atvSimplLoja;
                }
            }
            else {
                store = info.getStore();
            }

            if (validateService.isValid(callbackSTR, CreditCardCallbackRequest.class)) {
                creditCallback = mapper.readValue(callbackSTR, CreditCardCallbackRequest.class);

                logger.info("Identifier: " + identifier + " - CreditCardCallback: " + creditCallback);

                date = this.getDate(creditCallback.getOrderDate());
                time = this.getTime(creditCallback.getOrderDate());
                value = creditCallback.getValue().toString();
                type = "CARTAO_CREDITO";

                List<String> listCardNumber = Arrays.stream(creditCallback.getCard().split("-")).toList();
                cardNumber = listCardNumber.get(0) + "999999" + listCardNumber.get(1);
                issuerCode = "";
                issuerDescription = creditCallback.getFlag();
                valueAddedNetworkId = "";
                valueAddedNetworkDescription = "";
            } else if (validateService.isValid(callbackSTR, TefWebCallbackRequest.class)) {
                tefwebCallback = mapper.readValue(callbackSTR, TefWebCallbackRequest.class);

                logger.info("Identifier: " + identifier + " - TefWebCallback: " + tefwebCallback);

                date = this.getDate(tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData()
                        .getTransactionDate());
                time = this.getTime(
                        tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData().getHour());
                value = tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData().getValue();
                type = tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData().getPaymentType()
                        .getPaymentType()
                        + "_" + tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData()
                                .getPaymentType().getDetailPaymentType();
                cardNumber = tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                        .getCardBin()
                        + "999999" + tefwebCallback.getSales().get(0).getTransactions().get(0)
                                .getEletronicTransactionData().getCardEmbossing();
                issuerCode = tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                        .getFlagCode();
                issuerDescription = tefwebCallback.getSales().get(0).getTransactions().get(0)
                        .getEletronicTransactionData().getFlag();
                valueAddedNetworkId = tefwebCallback.getSales().get(0).getTransactions().get(0)
                        .getEletronicTransactionData().getAcquirator().getCode();
                valueAddedNetworkDescription = tefwebCallback.getSales().get(0).getTransactions().get(0)
                        .getEletronicTransactionData().getAcquirator().getDescription();
                installments = tefwebCallback.getSales().get(0).getTransactions().get(0).getTransactionData()
                        .getPaymentType().getNumberInstallmentsPayment();
                nsu = tefwebCallback.getSales().get(0).getTransactions().get(0).getEletronicTransactionData()
                        .getHostNsu();
                transactionCupon = tefwebCallback.getSales().get(0).getTransactions().get(0)
                        .getEletronicTransactionData().getIdSitef();

                // Usar também o valor total da order se disponível
                String orderTotalValue = tefwebCallback.getSales().get(0).getOrder().getTotalValue();
                if (orderTotalValue != null && !orderTotalValue.isEmpty()) {
                    // Converter de centavos (164900) para decimal (1649.00)
                    try {
                        BigDecimal totalValueBD = new BigDecimal(orderTotalValue);
                        BigDecimal convertedValue = totalValueBD.divide(new BigDecimal("100"));
                        value = convertedValue.setScale(2).toString();
                    } catch (NumberFormatException e) {
                        logger.warn("Identifier: " + identifier + " - Erro ao converter valor total da order: {}", orderTotalValue, e);
                        // Manter o valor original da transação
                    }
                }
            }

            // Garantir que campos obrigatórios não sejam null apenas se realmente estiverem
            // vazios
            if (date == null || date.isEmpty()) {
                date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                logger.warn("Identifier: " + identifier + " - Campo salesDate estava null, usando data atual: {}", date);
            }
            if (time == null || time.isEmpty()) {
                time = LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
                logger.warn("Identifier: " + identifier + " - Campo salesTime estava null, usando hora atual: {}", time);
            }
        } catch (JsonProcessingException e) {
            logger.info("Idenditier: " + identifier + " - ERROR: {}", e.getMessage());
            throw new BusinessException("Erro convert Json", "ERROR_CONVERT_JSON", e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return SapPaymentsRequest.builder()
                .data(DataDTO.builder()
                        .order(OrderDTO.builder()
                                .id(dto.getPayment().getSalesOrderId() != null ? dto.getPayment().getSalesOrderId() : identifier)
                                .companyId("001")
                                .businessLocationId(store)
                                .posInfo(PosInfoDTO.builder()
                                        .componentNumber("4000")
                                        .taxCouponNumber(dto.getPayment().getSalesOrderId() != null ? dto.getPayment().getSalesOrderId() : identifier)
                                        .build())
                                .salesCategory("3")
                                .salesType("1")
                                .salesDate(date)
                                .salesTime(time)
                                .totalAmountReceived(this.getTotalAmountReceived(uuid, info, dto))
                                .userName(dto != null && dto.getPayment() != null && dto.getPayment().getCardData() != null
                                        ? dto.getPayment().getSellerId()
                                        : "SYSTEM")
                                .customer(CustomerDTO.builder()
                                        .id(dto.getCustomer() != null ? dto.getCustomer().getCpf() : null)
                                        .identifications(
                                                Arrays.asList(
                                                        IdentificationDTO.builder()
                                                                .type(dto.getCustomer() != null && dto.getCustomer().getCpf() != null ? "CPF"
                                                                        : "CNPJ")
                                                                .id(dto.getCustomer() != null ? dto.getCustomer().getCpf() : null)
                                                                .build()))
                                        .name(dto.getCustomer() != null ? dto.getCustomer().getName() : null) // name - journey
                                        .build())
                                .sefazFlag("S")
                                .sefazCustomerIdentificationId(dto.getCustomer() != null ? dto.getCustomer().getCpf() : null)
                                .items(this.getItems(uuid, dto))
                                .orderReceipts(this.getConsolidatedOrderReceipts(
                                        uuid,
                                        info,
                                        request,
                                        dto))
                                .build())
                        .build())
                .build();
    }

    private String getTime(String dateTime) {
        LocalTime date = null;

        if (dateTime.matches("^[0-9]{2}:[0-9]{2}:[0-9]{2}$")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            date = LocalTime.parse(dateTime, formatter);
            return date.format(DateTimeFormatter.ofPattern("HHmmss"));
        }

        return null;

    }

    private String getDate(String dateTime) {

        LocalDate date = null;

        if (dateTime.matches("^[0-9]{2}/[0-9]{2}/[0-9]{4}$")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            date = LocalDate.parse(dateTime, formatter);
            return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        return null;
    }

    private List<ItemDTO> getItems(UUID uuid, DataSingleDTO dto) {

        Integer count = 1;
        List<ItemDTO> listItems = new ArrayList<>();

        // Verificar se os dados necessários existem
        if (dto == null || dto.getFraudAnalysisData() == null 
                || dto.getFraudAnalysisData().getComplementaryData() == null
                || dto.getFraudAnalysisData().getComplementaryData().getProducts() == null
                || dto.getFraudAnalysisData().getComplementaryData().getProducts().isEmpty()) {
            logger.warn("Identifier: " + identifier + " - Dados de produtos não disponíveis, retornando lista vazia");
            return listItems;
        }

        List<ProductDTO> list = dto.getFraudAnalysisData().getComplementaryData().getProducts();

        for (ProductDTO p : list) {
            try {
                BigDecimal amount = new BigDecimal(p.getAmount());
                BigDecimal unitValue = new BigDecimal(p.getValue());
                BigDecimal total = amount.multiply(unitValue);

                listItems.add(ItemDTO.builder()
                        .id(count.toString())
                        .materialId(p.getSku())
                        .quantity(p.getAmount())
                        .unitAmount(this.formatMoney(uuid, p.getValue()))
                        .totalAmount(this.formatMoney(uuid, total.toString()))
                        .discountAmount(this.formatMoney(uuid, p.getDiscountValue()))
                        .totalDiscountAmount(this.formatMoney(uuid, p.getTotalDiscountValue()))
                        .serialNumber(p.getSerialNumber())
                        .build());
                count++;
            } catch (NumberFormatException e) {
                logger.error("Identifier: " + identifier + " - Erro ao processar produto: {}", p, e);
                // Continuar com o próximo produto em caso de erro
            }
        }

        return listItems;
    }

    private List<OrderReceiptDTO> getOrderReceipt(UUID uuid, String value, String type, String cardNumber, String issuerCode,
            String issuerDescription, String valueAddedNetworkId, String valueAddedNetworkDescription,
            String installments, String nsu, String transactionCupom, DataSingleDTO dto) {

        List<OrderReceiptDTO> listReceipt = new ArrayList<>();

        Integer count = 1;

        listReceipt.add(OrderReceiptDTO.builder()
                .sequence(count.toString())
                .paymentAmount(this.formatMoney(uuid, value))
                .movimentType(null)
                .paymentType(type)
                .installments(installments)
                .cardDetails(CardDetailsDTO.builder()
                        .cardNumber(cardNumber)
                        .totalAmount(this.formatMoney(uuid, value))
                        .issuerCode(issuerCode)
                        .issuerDescription(issuerDescription)
                        .valueAddedNetworkId(valueAddedNetworkId)
                        .valueAddedNetworkDescription(valueAddedNetworkDescription)
                        .authorizationId(nsu)
                        .transactionApprovalCode(transactionCupom)
                        .taxTransactionReceiptId("955")
                        .build())
                .build());

        if (dto != null && dto.getPayment() != null && dto.getPayment().getDiscounts() != null) {

            for ( PaymentDiscountDTO discount : dto.getPayment().getDiscounts()) {
                if ("SUPER_TROCA".equals(discount.getId())){
                    count++;
                    listReceipt.add(OrderReceiptDTO.builder()
                        .sequence(count.toString())
                        .id("26")
                        .installments("1")
                        .paymentAmount(this.formatMoney(uuid, discount.getValue()))
                        .paymentType("CUPOM_PROMOCIONAL")
                        .othersDetails(OthersDetailsDTO.builder()
                            .verificationCode("23")
                            .build())
                        .build());
                }
                else if ("TROCA_FONE".equals(discount.getId())){
                    count++;
                    listReceipt.add(OrderReceiptDTO.builder()
                        .sequence(count.toString())
                        .id("26")
                        .installments("1")
                        .paymentAmount(this.formatMoney(uuid, discount.getValue()))
                        .paymentType("CUPOM_PROMOCIONAL")
                        .othersDetails(OthersDetailsDTO.builder()
                            .verificationCode("22")
                            .build())
                        .build());
                }

                logger.info("Identifier: " + identifier + " - Verificando desconto {}: found={}", discount.getId(), discount.getValue());
            }
        }

        return listReceipt;
    }

    private String getTotalAmountReceived(UUID uuid, InformationPaymentDTO info, DataSingleDTO dto) {
        if (Boolean.TRUE.equals(info.getMultiplePayment()) && info.getPayments() != null) {
            BigDecimal total = info.getPayments().stream()
                    .filter(p -> p.getPaymentStatus() == PaymentStatusEnum.APPROVED)
                    .map(p -> p.getValue() != null ? p.getValue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return total.setScale(2).toString();
        }
        
        return this.formatMoney(uuid, dto.getPayment().getValue());
    }

    private List<OrderReceiptDTO> getConsolidatedOrderReceipts(UUID uuid, InformationPaymentDTO info, 
            CallbackDTO request, DataSingleDTO dto) {
        
        if (Boolean.TRUE.equals(info.getMultiplePayment()) && info.getPayments() != null) {
            return getOrderReceiptsForMultiplePayment(uuid, info, request, dto);
        }
        
        return getOrderReceiptForSinglePayment(uuid, request, dto);
    }

    private List<OrderReceiptDTO> getOrderReceiptsForMultiplePayment(UUID uuid, InformationPaymentDTO info, 
            CallbackDTO request, DataSingleDTO dto) {
        
        List<OrderReceiptDTO> listReceipt = new ArrayList<>();
        Integer sequence = 1;
        
        for (com.omp.hub.callback.domain.model.dto.information.PaymentDTO payment : info.getPayments()) {
            if (payment.getPaymentStatus() == PaymentStatusEnum.APPROVED) {
                
                if (payment.getType() == PaymentTypeEnum.TEFWEB) {
                    OrderReceiptDTO tefwebReceipt = createTefwebOrderReceipt(uuid, sequence, payment, request, dto);
                    if (tefwebReceipt != null) {
                        listReceipt.add(tefwebReceipt);
                        sequence++;
                    }
                    
                } else if (payment.getType() == PaymentTypeEnum.CASH) {
                    OrderReceiptDTO cashReceipt = createCashOrderReceipt(uuid, sequence, payment);
                    if (cashReceipt != null) {
                        listReceipt.add(cashReceipt);
                        sequence++;
                    }
                }
            }
        }
        
        sequence = addDiscountsToReceipts(uuid, dto, listReceipt, sequence);
        
        return listReceipt;
    }
    
    
    private OrderReceiptDTO createTefwebOrderReceipt(UUID uuid, Integer sequence, 
            com.omp.hub.callback.domain.model.dto.information.PaymentDTO payment, 
            CallbackDTO request, DataSingleDTO dto) {
        
        try {
            String callbackSTR = mapper.writeValueAsString(request);
            
            if (validateService.isValid(callbackSTR, TefWebCallbackRequest.class)) {
                TefWebCallbackRequest tefwebCallback = mapper.readValue(callbackSTR, TefWebCallbackRequest.class);
                
                if (tefwebCallback.getSales() != null && !tefwebCallback.getSales().isEmpty() &&
                    tefwebCallback.getSales().get(0).getTransactions() != null && !tefwebCallback.getSales().get(0).getTransactions().isEmpty()) {
                    
                    // Usar o índice da transação do PaymentDTO (padrão: 0 para compatibilidade)
                    int transactionIndex = payment.getTransactionIndex() != null ? payment.getTransactionIndex() : 0;
                    
                    // Validar se o índice existe no array de transações
                    List<com.omp.hub.callback.domain.model.dto.callback.tefweb.TransactionsDTO> transactions = 
                        tefwebCallback.getSales().get(0).getTransactions();
                    
                    if (transactionIndex >= transactions.size()) {
                        logger.error("Identifier: {} - Transaction index {} está fora dos limites (total: {}). Usando índice 0.", 
                            identifier, transactionIndex, transactions.size());
                        transactionIndex = 0;
                    }
                    
                    logger.info("Identifier: {} - Processando transação de índice {} de {} transações totais", 
                        identifier, transactionIndex, transactions.size());
                    
                    var transactionData = transactions.get(transactionIndex).getTransactionData();
                    var electronicData = transactions.get(transactionIndex).getEletronicTransactionData();
                    
                    String cardNumber = electronicData.getCardBin() + "999999" + electronicData.getCardEmbossing();
                    String paymentType = transactionData.getPaymentType().getPaymentType() + "_" + 
                                       transactionData.getPaymentType().getDetailPaymentType();
                    
                    return OrderReceiptDTO.builder()
                            .sequence(sequence.toString())
                            .paymentAmount(payment.getValue().setScale(2).toString())
                            .movimentType(null)
                            .paymentType(paymentType)
                            .installments(transactionData.getPaymentType().getNumberInstallmentsPayment())
                            .cardDetails(CardDetailsDTO.builder()
                                    .cardNumber(cardNumber)
                                    .totalAmount(payment.getValue().setScale(2).toString())
                                    .issuerCode(electronicData.getFlagCode())
                                    .issuerDescription(electronicData.getFlag())
                                    .valueAddedNetworkId(electronicData.getAcquirator().getCode())
                                    .valueAddedNetworkDescription(electronicData.getAcquirator().getDescription())
                                    .authorizationId(electronicData.getHostNsu())
                                    .transactionApprovalCode(electronicData.getIdSitef())
                                    .taxTransactionReceiptId("955")
                                    .build())
                            .build();
                }
            }
        } catch (Exception e) {
            logger.error("Identifier: " + identifier + " - Erro ao criar OrderReceipt para TEFWEB: {}", e.getMessage());
        }
        
        return OrderReceiptDTO.builder()
                .sequence(sequence.toString())
                .id("25")
                .paymentAmount(payment.getValue().setScale(2).toString())
                .movimentType("C")
                .paymentType("CARTAO_CREDITO")
                .installments("1")
                .build();
    }
    
    private OrderReceiptDTO createCashOrderReceipt(UUID uuid, Integer sequence, 
            com.omp.hub.callback.domain.model.dto.information.PaymentDTO payment) {
        
        return OrderReceiptDTO.builder()
                .sequence(sequence.toString())
                .paymentAmount(payment.getValue().setScale(2).toString())
                .movimentType("D")
                .paymentType("DINHEIRO")
                .installments("1")
                .build();
    }
    
    private List<OrderReceiptDTO> getOrderReceiptForSinglePayment(UUID uuid, CallbackDTO request, DataSingleDTO dto) {
        try {
            String callbackSTR = mapper.writeValueAsString(request);
            List<OrderReceiptDTO> listReceipt = new ArrayList<>();
            Integer sequence = 1;
            
            if (validateService.isValid(callbackSTR, CreditCardCallbackRequest.class)) {
                CreditCardCallbackRequest creditCallback = mapper.readValue(callbackSTR, CreditCardCallbackRequest.class);
                String value = creditCallback.getValue().toString();
                String type = "CARTAO_CREDITO";
                List<String> listCardNumber = Arrays.stream(creditCallback.getCard().split("-")).toList();
                String cardNumber = listCardNumber.get(0) + "999999" + listCardNumber.get(1);
                String issuerDescription = creditCallback.getFlag();
                
                listReceipt.add(OrderReceiptDTO.builder()
                        .sequence(sequence.toString())
                        .paymentAmount(this.formatMoney(uuid, value))
                        .movimentType(null)
                        .paymentType(type)
                        .installments("1")
                        .cardDetails(CardDetailsDTO.builder()
                                .cardNumber(cardNumber)
                                .totalAmount(this.formatMoney(uuid, value))
                                .issuerCode("")
                                .issuerDescription(issuerDescription)
                                .valueAddedNetworkId("")
                                .valueAddedNetworkDescription("")
                                .authorizationId(null)
                                .transactionApprovalCode(null)
                                .taxTransactionReceiptId("955")
                                .build())
                        .build());
                sequence++;
                
            } else if (validateService.isValid(callbackSTR, TefWebCallbackRequest.class)) {
                TefWebCallbackRequest tefwebCallback = mapper.readValue(callbackSTR, TefWebCallbackRequest.class);
                
                if (tefwebCallback.getSales() != null && !tefwebCallback.getSales().isEmpty() &&
                    tefwebCallback.getSales().get(0).getTransactions() != null) {
                    
                    List<com.omp.hub.callback.domain.model.dto.callback.tefweb.TransactionsDTO> transactions = 
                        tefwebCallback.getSales().get(0).getTransactions();
                    
                    logger.info("Identifier: {} - Processando {} transação(ões) no pagamento single", 
                        identifier, transactions.size());
                    
                    // Processar todas as transações
                    for (com.omp.hub.callback.domain.model.dto.callback.tefweb.TransactionsDTO transaction : transactions) {
                        var transactionData = transaction.getTransactionData();
                        var electronicData = transaction.getEletronicTransactionData();
                        
                        String value = transactionData.getValue();
                        String type = transactionData.getPaymentType().getPaymentType() + "_" + 
                                    transactionData.getPaymentType().getDetailPaymentType();
                        String cardNumber = electronicData.getCardBin() + "999999" + electronicData.getCardEmbossing();
                        String issuerCode = electronicData.getFlagCode();
                        String issuerDescription = electronicData.getFlag();
                        String valueAddedNetworkId = electronicData.getAcquirator().getCode();
                        String valueAddedNetworkDescription = electronicData.getAcquirator().getDescription();
                        String installments = transactionData.getPaymentType().getNumberInstallmentsPayment();
                        String nsu = electronicData.getHostNsu();
                        String transactionCupom = electronicData.getIdSitef();
                        
                        listReceipt.add(OrderReceiptDTO.builder()
                                .sequence(sequence.toString())
                                .paymentAmount(this.formatMoney(uuid, value))
                                .movimentType(null)
                                .paymentType(type)
                                .installments(installments)
                                .cardDetails(CardDetailsDTO.builder()
                                        .cardNumber(cardNumber)
                                        .totalAmount(this.formatMoney(uuid, value))
                                        .issuerCode(issuerCode)
                                        .issuerDescription(issuerDescription)
                                        .valueAddedNetworkId(valueAddedNetworkId)
                                        .valueAddedNetworkDescription(valueAddedNetworkDescription)
                                        .authorizationId(nsu)
                                        .transactionApprovalCode(transactionCupom)
                                        .taxTransactionReceiptId("955")
                                        .build())
                                .build());
                        sequence++;
                        
                        logger.info("Identifier: {} - Transação adicionada: tipo={}, valor={}, nsu={}", 
                            identifier, type, value, nsu);
                    }
                }
            }
            
            // Adicionar descontos ao final
            sequence = addDiscountsToReceipts(uuid, dto, listReceipt, sequence);
            
            return listReceipt;
                                 
        } catch (Exception e) {
            logger.error("Identifier: " + identifier + " - Erro ao gerar orderReceipt para pagamento único: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private Integer addDiscountsToReceipts(UUID uuid, DataSingleDTO dto, List<OrderReceiptDTO> listReceipt, Integer sequence) {
        if (dto != null && dto.getPayment() != null && dto.getPayment().getDiscounts() != null) {
            for (PaymentDiscountDTO discount : dto.getPayment().getDiscounts()) {
                if ("SUPER_TROCA".equals(discount.getId()) || "TROCA_FONE".equals(discount.getId())) {
                    listReceipt.add(OrderReceiptDTO.builder()
                        .sequence(sequence.toString())
                        .id("26")
                        .installments("1")
                        .paymentAmount(this.formatMoney(uuid, discount.getValue()))
                        .paymentType("CUPOM_PROMOCIONAL")
                        .othersDetails(OthersDetailsDTO.builder()
                            .verificationCode("SUPER_TROCA".equals(discount.getId()) ? "23" : "22")
                            .build())
                        .build());
                    sequence++;
                }
            }
        }
        return sequence;
    }

    private String formatMoney(UUID uuid, String value) {
        if (value == null || value.isEmpty()) {
            return "0.00";
        }

        try {
            // Se o valor já contém ponto decimal, assumir que já está formatado
            // corretamente
            if (value.contains(".")) {
                BigDecimal valueBD = new BigDecimal(value);
                return valueBD.setScale(2).toString();
            }

            // Se o valor não contém ponto, assumir que está em centavos
            BigDecimal valueBD = new BigDecimal(value);
            BigDecimal correctValue = valueBD.divide(new BigDecimal("100"));
            return correctValue.setScale(2).toString();
        } catch (NumberFormatException e) {
            logger.error("Identifier: " + identifier + " - Erro ao formatar valor monetário: {}", value, e);
            return "0.00";
        }
    }
}