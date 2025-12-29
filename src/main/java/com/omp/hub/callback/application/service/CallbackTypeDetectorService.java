package com.omp.hub.callback.application.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.validator.CallbackValidator;
import com.omp.hub.callback.domain.model.dto.callback.creditcard.CreditCardCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.pix.PixCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.tefweb.TefWebCallbackRequest;
import com.omp.hub.callback.domain.model.dto.callback.transactions.TransactionsRequest;
import com.omp.hub.callback.domain.service.check.CheckTypeObjectService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CallbackTypeDetectorService {

    private final ObjectMapper mapper;
    private final CheckTypeObjectService validateService;
    private final CallbackValidator callbackValidator;

    public void detectTypeAndValidate(String object) throws JsonProcessingException {
        if (validateService.isValid(object, TransactionsRequest.class)) {
            TransactionsRequest callback = mapper.readValue(object, TransactionsRequest.class);
            callbackValidator.validate(callback, "Transactions");
        } else if (validateService.isValid(object, TefWebCallbackRequest.class)) {
            TefWebCallbackRequest callback = mapper.readValue(object, TefWebCallbackRequest.class);
            callbackValidator.validate(callback, "TefWeb");
        } else if (validateService.isValid(object, CreditCardCallbackRequest.class)) {
            CreditCardCallbackRequest callback = mapper.readValue(object, CreditCardCallbackRequest.class);
            callbackValidator.validate(callback, "CreditCard");
        } else if (validateService.isValid(object, PixCallbackRequest.class)) {
            PixCallbackRequest callback = mapper.readValue(object, PixCallbackRequest.class);
            callbackValidator.validate(callback, "Pix");
        }
    }

    public boolean isValidCallbackType(String object) {
        return validateService.isValid(object, TransactionsRequest.class) ||
               validateService.isValid(object, TefWebCallbackRequest.class) ||
               validateService.isValid(object, CreditCardCallbackRequest.class) ||
               validateService.isValid(object, PixCallbackRequest.class);
    }
}
