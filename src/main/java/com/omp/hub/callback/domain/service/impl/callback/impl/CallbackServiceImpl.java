package com.omp.hub.callback.domain.service.impl.callback.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.usecase.callback.CreditCardCallbackUseCase;
import com.omp.hub.callback.application.usecase.callback.PixCallbackUseCase;
import com.omp.hub.callback.application.usecase.callback.TefWebCallbackUseCase;
import com.omp.hub.callback.application.usecase.callback.TransactionsCallbackUseCase;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;
import com.omp.hub.callback.domain.service.check.CheckTypeObjectService;
import com.omp.hub.callback.domain.service.impl.callback.CallbackService;
import com.omp.hub.callback.infrastructure.persistence.message.sqs.SqsMessageRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CallbackServiceImpl implements CallbackService{

    private static final Logger logger = LoggerFactory.getLogger(CallbackServiceImpl.class);

    @Autowired
    private PixCallbackUseCase pixCallbackUseCase;

    @Autowired
    private CreditCardCallbackUseCase creditCardCallbackUseCase;

    @Autowired
    private TefWebCallbackUseCase tefwebCallbackUseCase;

    @Autowired
    private TransactionsCallbackUseCase transactionsCallbackUseCase;

    @Autowired
    private CheckTypeObjectService validateService;

    @Autowired
    private SqsMessageRepository sqsMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void processCallback(String object) {
        internalProcessCallback(object);
    }

    @Async("callbackExecutor")
    @Override
    public void processCallbackAsync(CallbackRequest<?> callbackRequest) {
        try {
            sqsMessageRepository.sendMessage(callbackRequest);
            logger.info("Callback enviado para SQS com sucesso");
            
        } catch (Exception e) {
            logger.error("Erro ao enviar callback para SQS: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao enviar callback para fila", "ERROR_SEND_TO_QUEUE", e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void internalProcessCallback(String object) {
        
        try {
            if (validateService.isValid(object, PixCallbackRequest.class)) {
                pixCallbackUseCase.sendCallback(objectMapper.readValue(object, PixCallbackRequest.class));
            } else if (validateService.isValid(object, CreditCardCallbackRequest.class)) {
                creditCardCallbackUseCase.sendCallback(objectMapper.readValue(object, CreditCardCallbackRequest.class));
            } else if (validateService.isValid(object, TefWebCallbackRequest.class)) {
                tefwebCallbackUseCase.sendCallback(objectMapper.readValue(object, TefWebCallbackRequest.class));
            } else if (validateService.isValid(object, TransactionsRequest.class)) {
                transactionsCallbackUseCase.sendCallback(objectMapper.readValue(object, TransactionsRequest.class));
            } else {
                logger.error("Payload não corresponde a nenhum tipo de callback suportado");
                throw new BusinessException(
                    "Payload inválido: não corresponde a nenhum tipo de callback suportado (PIX, CreditCard, TefWeb ou Transactions)",
                    "INVALID_CALLBACK_TYPE",
                    "O payload recebido não possui a estrutura esperada para nenhum dos tipos de callback",
                    HttpStatus.BAD_REQUEST
                );
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            logger.error("ERROR: {}", e.getMessage());
            throw new BusinessException("Erro convert Json", "ERROR_CONVERT_JSON", e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (BusinessException e) {
            throw e;
        }
    }


}