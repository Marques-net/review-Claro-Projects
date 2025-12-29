package com.omp.hub.callback.application.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omp.hub.callback.application.service.CallbackTypeDetectorService;
import com.omp.hub.callback.application.validator.CallbackValidationException;
import com.omp.hub.callback.domain.exceptions.BusinessException;
import com.omp.hub.callback.domain.model.dto.callback.CallbackRequest;
import com.omp.hub.callback.domain.model.dto.response.CallbackResponse;
import com.omp.hub.callback.domain.service.impl.callback.CallbackService;
import com.omp.hub.callback.infrastructure.util.JsonSanitizerUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/omphub/callback")
@RequiredArgsConstructor
@Tag(name = "Callback", description = "API de Callback dos Hubs de pagamento")
public class CallbackController {

    private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);

    @Autowired
    CallbackService callbackService;
    
    @Autowired
    ObjectMapper mapper;

    @Autowired
    CallbackTypeDetectorService callbackTypeDetector;

    @org.springframework.beans.factory.annotation.Value("${feature.async-processing.enabled:true}")
    private boolean asyncProcessingEnabled;

    @PostMapping()
    @Operation(summary = "Callback", description = "Recebe os Callbacks dos Hubs de pagamento, atualiza o status e envia o callback para os canais")
    public <T> ResponseEntity<?> processCallback(@RequestBody CallbackRequest<T> request) {

        try {
            String objetct = mapper.writeValueAsString(request.getData());
            
            // Sanitizar o JSON antes de processar para remover valores malformados
            logger.info("JSON original recebido no callback");
            String sanitizedJson = JsonSanitizerUtil.sanitizeCallbackJson(objetct, mapper);
            
            if (!sanitizedJson.equals(objetct)) {
                logger.info("JSON foi sanitizado. Campos malformados foram removidos.");
            }

            if (!callbackTypeDetector.isValidCallbackType(sanitizedJson)) {
                throw new BusinessException(
                    "Payload inválido: não corresponde a nenhum tipo de callback suportado (PIX, CreditCard, TefWeb ou Transactions)",
                    "INVALID_CALLBACK_TYPE",
                    "O payload recebido não possui a estrutura esperada para nenhum dos tipos de callback",
                    HttpStatus.BAD_REQUEST
                );
            }

            callbackTypeDetector.detectTypeAndValidate(sanitizedJson);
            
            if (asyncProcessingEnabled) {
                callbackService.processCallbackAsync(request);
            } else {
                logger.info("Processamento assíncrono desabilitado. Processando callback de forma síncrona.");
                callbackService.processCallback(sanitizedJson);
            }
            
            
            CallbackResponse response = CallbackResponse.builder()
                .apiVersion("1;2019-09-11")
                .transactionId("Id-34fcb05c6d1923e35cef248d")
                .data(CallbackResponse.ResponseData.builder()
                    .result("SUCCESS")
                    .build())
                .build();
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
            
        } catch (JsonProcessingException e) {
            logger.error("Erro ao converter JSON: {}", e.getMessage());
            throw new BusinessException("Erro convert Json", "ERROR_CONVERT_JSON", e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (CallbackValidationException e) {
            logger.error("Erro de validação: {}", e.getMessage());
            throw new BusinessException(e.getMessage(), "VALIDATION_ERROR", e.getDetails(),
                    HttpStatus.BAD_REQUEST);
        } catch (BusinessException e) {
            logger.error("Erro de validação: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado: {}", e.getMessage(), e);
            throw new BusinessException("Erro ao processar callback", "ERROR_PROCESS_CALLBACK", e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
