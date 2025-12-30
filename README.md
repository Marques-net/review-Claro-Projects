# OMP Hub Payment Callback Microservice

Microserviço especializado no processamento de callbacks de pagamento dos diferentes hubs de pagamento e encaminhamento para os canais apropriados.

## Visão Geral

Este microserviço foi extraído do `omp-hub-payment-journey-ms` para isolar e especializar o processamento de callbacks de pagamento, oferecendo:

- Processamento de callbacks PIX
- Processamento de callbacks de Cartão de Crédito  
- Processamento de callbacks TEF Web
- Processamento de callbacks de Transactions
- Integração com sistemas SAP (Payments e Redemptions)
- Notificações para canais de origem
- Validação automática de tipos de callback

## Stack Tecnológica

- **Java 17**
- **Spring Boot 3.5.0**
- **Spring Data**
- **AWS SDK** (DynamoDB, Parameter Store)
- **OkHttp** para comunicação HTTP
- **SpringDoc OpenAPI** para documentação
- **Lombok** para redução de código boilerplate
- **Jackson** para serialização JSON

## Arquitetura

O projeto segue a **Arquitetura Hexagonal (Ports & Adapters)**:

```
src/main/java/com/omp/hub/callback/
├── PaymentCallbackApplication.java          # Classe principal
├── application/
│   ├── controller/
│   │   ├── CallbackController.java             # Controller principal
│   │   └── HealthCheckController.java          # Health check
│   ├── usecase/callback/                       # Use cases de callback
│   │   ├── PixCallbackUseCase.java
│   │   ├── CreditCardCallbackUseCase.java
│   │   ├── TefWebCallbackUseCase.java
│   │   └── TransactionsCallbackUseCase.java
│   └── utils/                                  # Utilitários
├── domain/
│   ├── model/dto/                              # DTOs por domínio
│   ├── service/                                # Interfaces de serviços
│   ├── ports/                                  # Contratos de integração
│   ├── enums/                                  # Enumerações
│   └── exceptions/                             # Exceções de negócio
└── infrastructure/
    ├── client/                                 # Clientes HTTP
    ├── config/                                 # Configurações
    └── persistence/                            # Implementações de persistência
```

## Endpoints

### Callback Principal

```http
POST /omphub/callback/callback
Content-Type: application/json
```

Processa automaticamente callbacks dos tipos:
- `PixCallbackRequest`
- `CreditCardCallbackRequest` 
- `TefWebCallbackRequest`
- `TransactionsRequest`

### Monitoramento

```http
GET /actuator/health           # Health check
GET /swagger-ui.html          # Documentação Swagger
GET /api-docs                 # Especificação OpenAPI
```

## Configuração

### Profiles de Ambiente

| Profile | Arquivo | Descrição |
|---------|---------|-----------|
| `local` | `application-local.yml` | Desenvolvimento local |
| `dev` | `application-dev.yml` | Desenvolvimento |
| `hom` | `application-hom.yml` | Homologação |
| `prod` | `application-prod.yml` | Produção |

### Dependências AWS

- **DynamoDB**: Persistência de dados de pagamento
- **Parameter Store**: Configurações e credenciais

### Integrações

- **SAP Payments**: Processamento de pagamentos
- **SAP Redemptions**: Processamento de resgates  
- **Transactions Notifications**: Notificações de transações
- **Apigee Gateway**: Autenticação e roteamento

## Execução

### Requisitos

- Java 17+
- Maven 3.6+
- Acesso AWS configurado

### Comandos Maven

```bash
# Compilação
./mvnw clean compile

# Execução local
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Execução em desenvolvimento
./mvnw spring-boot.run.profiles=dev

# Testes
./mvnw test

# Verificação de saúde
curl http://localhost:8090/actuator/health
```

### Docker

```bash
# Build
docker build -t omp-hub-payment-callback-ms .

# Execução
docker run -p 8090:8090 \
  -e SPRING_PROFILES_ACTIVE=local \
  omp-hub-payment-callback-ms
```

## Fluxo de Processamento

```mermaid
graph TD
    A[Hub de Pagamento] -->|POST /callback| B[CallbackController]
    B --> C[Validação Automática]
    C -->|PIX| D[PixCallbackUseCase]
    C -->|Cartão| E[CreditCardCallbackUseCase]
    C -->|TEF Web| F[TefWebCallbackUseCase]
    C -->|Transactions| G[TransactionsCallbackUseCase]

    D --> H[Atualização DynamoDB]
    E --> H
    E --> I[Integração SAP]
    F --> H
    F --> I
    G --> H

    H --> J[Notificação Canais]
    I --> J
```
### Diagrama de Classe
```mermaid
classDiagram
direction LR

class CallbackController {
  +processCallback(request)
}

class CallbackTypeDetectorService {
  +detectTypeAndValidate(object)
  +isValidCallbackType(object)
}

class CheckTypeObjectService {
  <<interface>>
  +isValid(request, nameClass)
}

class CheckTypeObjectServiceImpl {
  +isValid(request, nameClass)
  -hasRequiredFields(node, fields)
}

class CallbackValidator {
  +validate(object, objectType)
}

class CallbackService {
  +processCallback(json)
  +processCallbackAsync(request)
}

class CallbackServiceImpl {
  +processCallback(json)
  +processCallbackAsync(request)
  -internalProcessCallback(object)
}

class TransactionsCallbackUseCase {
  <<interface>>
  +sendCallback(request)
}

class PixCallbackUseCase {
  <<interface>>
  +sendCallback(request)
}

class CreditCardCallbackUseCase {
  <<interface>>
  +sendCallback(request)
}

class TefWebCallbackUseCase {
  <<interface>>
  +sendCallback(request)
}

class TransactionsCallbackUseCaseImpl {
  +sendCallback(request)
  -extractTxId(request)
  -processNotificationsIfEligible(uuid, txId, request)
  -getRootCauseMessage(e)
}

class PixCallbackUseCaseImpl {
  +sendCallback(request)
  -processPixAutomaticoNotification(uuid, request)
  -sendRtdmNotification(uuid, request, eventType)
  -sendHubNotification(uuid, request)
}

class CreditCardCallbackUseCaseImpl {
  +sendCallback(request)
  -determineErrorMessage(errorDetails)
  -getRootCauseMessage(e)
}

class TefWebCallbackUseCaseImpl {
  +sendCallback(request)
  -processMultiplePayment(request, info, uuid, identifier, dto, headerBuilder, originalTransactionId)
  -processSinglePayment(request, info, uuid, identifier, dto, headerBuilder)
  -updatePaymentStatusSuccess(identifier, uuid)
  -handlePaymentError(e, identifier, uuid)
  -extractBusinessException(e)
  -determineErrorMessage(errorDetails)
  -getRootCauseMessage(e)
  -isCancellationCallback(request)
  -processCancellationCallback(request, info, uuid, identifier, headerBuilder)
  -updateJourneyCancellationStatus(identifier, tefwebStatus, uuid)
  -extractTransactionStatus(request)
}

class SapIntegrationService {
  +shouldSendToSap(info, currentPaymentType)
  +areAllPaymentsApproved(info, currentPaymentType)
  +updatePaymentStatusApproved(identifier, paymentType, transactionOrderId)
  +updatePaymentStatusApproved(identifier, paymentType, transactionOrderId, callbackData)
  +extractBaseTransactionOrderId(transactionOrderId)
  +sendToSapRedemptionsAndPayments(uuid, request, info, headerBuilder, dto)
  +sendToSapBillingPayments(uuid, request, info, headerBuilder, dto)
  +sendChannelNotification(uuid, request, headerBuilder, info)
  +hasBillingProducts(dto)
  +hasSalesOrderId(dto, identifier)
  -isValidUUID(value)
  -getTransactionOrderIdForSap(info, dto)
  -getPrefix()
  -getSuffixFormat()
  -getBaseLength()
}

class RetryService {
  +executeWithRetrySyncVoid(uuid, operationName, operation)
  +executeWithRetrySyncVoid(uuid, operationName, operation, callbackData)
  -sendToDLQ(callbackData, exception)
}

class SqsCallbackListener {
  +startPolling()
  +stopPolling()
  -pollMessages()
  -processMessage(message)
  -deleteMessage(receiptHandle)
  -getRetryCount(messageSQS)
  -handleProcessingError(messageSQS, messageId, txId, error, receiptHandle, retryCount)
  -parseMessage(messageBody)
  -extractDataAsJson(messageSQS)
  -extractTxId(messageSQS)
  #isRunning()
  #setRunning(value)
}

class SqsMessageRepository {
  +sendMessage(callbackRequest)
  +sendToDLQ(message, error)
  +resendForRetry(message)
  -extractMessageGroupIdFromMessage(message)
  -getStackTraceAsString(error)
  -extractMessageGroupId(callbackRequest)
}

class JsonSanitizerUtil {
  +sanitizeCallbackJson(jsonString, mapper)
  -sanitizePaymentItem(paymentObj)
  -isEscapedNullString(value)
}

class CallbackErrorNotificationService {
  +notifyJourneyAboutCallbackFailure(identifier, maxRetries, lastException)
}

class ErrorResponseMapper {
  +mapExceptionToErrorResponse(ex, path)
  +mapMaxRetriesExceededError(identifier, maxRetries, lastException)
}

class CallbackRequest~T~ {
  +data: T
}

class TransactionsRequest {
  +ompTransactionId
  +callbackTarget
  +targetSystem
  +flowType
  +event: EventDTO
}

class TefWebCallbackRequest {
  +ompTransactionId
  +service
  +paymentType
  +sales: List~SalesDTO~
  +multiplePayment
  +mixedPaymentTypes
}

class CreditCardCallbackRequest {
  +ompTransactionId
  +sucess
  +service
  +statusCode
  +statusMessage
  +transactionId
  +flag
  +card
  +value
  +numberInstallments
  +orderId
  +orderDate
  +acquirator: AcquiratorDTO
  +retryProcessor: List~RetryProcessorDTO~
  +antifraud: AntifraudDTO
}

class PixCallbackRequest {
  +ompTransactionId
  +service
  +paymentType
  +paymentDate
  +value
  +endToEndId
  +txId
  +orderId
}

class EventDTO {
  +type
  +payment: List~PaymentDTO~
  +customer: CustomerDTO
  +additionalInfo
  +originPaymentMethod: OriginPaymentMethodDTO
  +targetPaymentMethod: TargetPaymentMethodDTO
  +paymentMethod
  +recurrenceId
  +txId
  +status
  +updates: List~UpdatesDTO~
  +attempts: List~AttemptsDTO~
  +activation: ActivationDTO
  +addon: AddonDTO
}

class PaymentDTO {
  +type
  +date
  +value
  +numberInstallments
  +pointOfSales
  +acquirer: AcquirerDTO
  +pix: PixDTO
  +cash: CashDTO
  +tefweb: TefWebDTO
}

class InformationPaymentPort {
  <<interface>>
  +sendFindByIdentifier(identifier)
  +updatePaymentInList(identifier, paymentType, request)
  +sendUpdate(request)
}

class TransationsNotificationsPort {
  <<interface>>
  +send(uuid, request, headers)
}

class GenerateCallbackPixService {
  <<interface>>
  +generateRequest(request)
}

class GenerateCallbackTransactionsService {
  <<interface>>
  +generateRequest(request)
}

class ApigeeHeaderService {
  <<interface>>
  +generateHeaderApigee(uuid)
}

class PixEventMappingService {
  <<interface>>
  +isPixAutomaticoEvent(txId, paymentType)
  +shouldNotify(txId, eventType)
  +mapEventTypeToEnum(txId, eventType, status, paymentMethod, recurrenceId)
  +mapPaymentTypeToEvent(txId, paymentType)
}

class NotificationManagerService {
  <<interface>>
  +processPixAutomaticoNotification(uuid, txId, eventType)
}

CallbackController --> CallbackService
CallbackController --> CallbackTypeDetectorService
CallbackController --> CallbackValidator
CallbackController --> CallbackRequest~T~
CallbackController --> JsonSanitizerUtil

CallbackTypeDetectorService --> CheckTypeObjectService
CallbackTypeDetectorService --> CallbackValidator
CheckTypeObjectService <|.. CheckTypeObjectServiceImpl

CallbackService <|.. CallbackServiceImpl
CallbackServiceImpl --> SqsMessageRepository

CallbackTypeDetectorService --> TransactionsRequest
CallbackTypeDetectorService --> TefWebCallbackRequest
CallbackTypeDetectorService --> CreditCardCallbackRequest
CallbackTypeDetectorService --> PixCallbackRequest

CallbackServiceImpl --> TransactionsCallbackUseCase
CallbackServiceImpl --> PixCallbackUseCase
CallbackServiceImpl --> CreditCardCallbackUseCase
CallbackServiceImpl --> TefWebCallbackUseCase

TransactionsCallbackUseCase <|.. TransactionsCallbackUseCaseImpl
PixCallbackUseCase <|.. PixCallbackUseCaseImpl
CreditCardCallbackUseCase <|.. CreditCardCallbackUseCaseImpl
TefWebCallbackUseCase <|.. TefWebCallbackUseCaseImpl

TefWebCallbackUseCaseImpl --> SapIntegrationService
TefWebCallbackUseCaseImpl --> RetryService

TransactionsCallbackUseCaseImpl --> InformationPaymentPort
TransactionsCallbackUseCaseImpl --> TransationsNotificationsPort
TransactionsCallbackUseCaseImpl --> GenerateCallbackTransactionsService
TransactionsCallbackUseCaseImpl --> ApigeeHeaderService
TransactionsCallbackUseCaseImpl --> NotificationManagerService
TransactionsCallbackUseCaseImpl --> PixEventMappingService

PixCallbackUseCaseImpl --> InformationPaymentPort
PixCallbackUseCaseImpl --> TransationsNotificationsPort
PixCallbackUseCaseImpl --> GenerateCallbackPixService
PixCallbackUseCaseImpl --> ApigeeHeaderService
PixCallbackUseCaseImpl --> PixEventMappingService
PixCallbackUseCaseImpl --> NotificationManagerService

RetryService --> SqsMessageRepository
SapIntegrationService --> InformationPaymentPort
SapIntegrationService --> TransationsNotificationsPort
SapIntegrationService --> RetryService

SqsCallbackListener --> CallbackService
SqsCallbackListener --> CallbackErrorNotificationService
SqsCallbackListener --> SqsMessageRepository

CallbackErrorNotificationService --> InformationPaymentPort
CallbackErrorNotificationService --> ErrorResponseMapper

TransactionsRequest --> EventDTO
EventDTO --> PaymentDTO

```
### Processamento Detalhado

1. **Recepção**: Hub envia callback via HTTP POST
2. **Validação**: Identificação automática do tipo de callback
3. **Processamento**: Execução do use case específico
4. **Persistência**: Atualização de status no DynamoDB
5. **Integração**: Comunicação com SAP quando necessário
6. **Notificação**: Envio para canais de origem

## Testes

```bash
# Todos os testes
./mvnw test

# Relatório de cobertura
./mvnw test jacoco:report

# Testes unitários
./mvnw test -Dtest="*Test"

# Testes de integração  
./mvnw test -Dtest="*IT"
```

## Monitoramento e Logs

### Métricas

- Health checks via Spring Actuator
- Logs estruturados por nível
- Rastreamento de requests
- Métricas de performance

### Comandos de Log

```bash
# Acompanhamento em tempo real
tail -f logs/application.log

# Filtros específicos
grep "CallbackController" logs/application.log
grep "ERROR" logs/application.log
```

## Solução de Problemas

### Problemas Comuns

**Erro de Autenticação Apigee**
- Verificar header `x-client-auth` nas configurações
- Validar token de acesso no Parameter Store

**Falha na Comunicação SAP**
- Confirmar conectividade de rede
- Verificar credenciais e URLs de endpoint

**Erro de Parsing JSON**
- Validar estrutura do payload de entrada
- Verificar logs de resposta do Apigee

### Logs de Debug

Para habilitar logs detalhados, adicionar no `application.yml`:

```yaml
logging:
  level:
    com.omp.hub.callback: DEBUG
    org.springframework.web: DEBUG
```
