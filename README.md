# Gateway de Pagamento

API REST desenvolvida em **Java + Spring Boot** para simular um gateway de pagamento com foco em **boas práticas de back-end**, **segurança**, **idempotência**, **multi-tenant**, **webhooks** e **testes de integração**.

O projeto foi construído para representar um cenário próximo ao mercado: cada lojista (merchant) possui sua própria chave de API, consegue criar e consultar pagamentos, realizar captura e reembolso, configurar webhooks e proteger operações críticas contra duplicidade de requisições.

---

## Visão geral

Este projeto implementa uma camada de gateway responsável por:

- autenticar requisições via **API Key**;
- isolar os dados por **merchant**;
- criar pagamentos com suporte a **idempotência**;
- listar e consultar pagamentos;
- capturar e reembolsar pagamentos com validação de status;
- configurar **webhooks** por merchant;
- enfileirar e despachar eventos de pagamento com **retry**;
- aplicar **rate limiting** por merchant;
- padronizar respostas de erro;
- garantir confiabilidade com **testes de integração**.

---

## Objetivos do projeto

Este projeto foi desenvolvido para praticar e demonstrar competências importantes de back-end, como:

- construção de APIs REST com Spring Boot;
- modelagem de domínio para pagamentos;
- persistência com Spring Data JPA;
- versionamento de banco com Flyway;
- autenticação customizada com filtro de segurança;
- proteção contra duplicidade com idempotência;
- comunicação assíncrona via webhooks;
- observabilidade com correlation id e logs;
- testes de integração com Testcontainers e PostgreSQL.

---

## Tecnologias utilizadas

- **Java 21**
- **Spring Boot 3**
- **Spring Web**
- **Spring Security**
- **Spring Data JPA**
- **Spring Validation**
- **Spring WebFlux** (WebClient para envio de webhooks)
- **PostgreSQL**
- **Flyway**
- **Bucket4j**
- **JUnit 5**
- **Spring Boot Test**
- **Testcontainers**
- **Docker Compose**
- **Maven**

---

## Principais funcionalidades

### 1. Autenticação por API Key
Todas as rotas protegidas exigem o header:

```http
X-API-Key: sua-chave
```

A autenticação é feita por filtro customizado, buscando o merchant no banco e validando se ele está ativo.

### 2. Multi-tenant por merchant
O sistema foi pensado para separar os dados por lojista. Cada pagamento pertence a um merchant, e as listagens retornam apenas os registros do merchant autenticado.

### 3. Idempotência em criação de pagamento
A criação de pagamento suporta o header:

```http
Idempotency-Key: chave-unica-da-requisicao
```

Se a mesma chave for enviada novamente com o **mesmo payload**, a API devolve a mesma resposta anterior. Se a mesma chave for reutilizada com **payload diferente**, a API retorna conflito.

### 4. Ciclo de vida do pagamento
Atualmente o fluxo principal contempla os estados:

- `AUTHORIZED`
- `CAPTURED`
- `REFUNDED`

Também existem estados modelados no domínio para evolução futura:

- `CREATED`
- `CANCELED`
- `DECLINED`

### 5. Captura e reembolso com regra de negócio
O projeto valida transições de status importantes:

- só é possível **capturar** um pagamento em `AUTHORIZED`;
- só é possível **reembolsar** um pagamento em `CAPTURED`.

### 6. Webhooks
Cada merchant pode configurar uma URL de webhook para receber eventos de pagamento.

Eventos gerados atualmente:

- `payment.authorized`
- `payment.captured`
- `payment.refunded`

### 7. Retry de webhook
Os eventos de webhook são persistidos e reenviados automaticamente em caso de falha, com política simples de retry baseada em tempo.

### 8. Rate limiting
A API aplica limite de requisições por merchant usando Bucket4j.

Configuração atual:

- **60 requisições por minuto por merchant**

Quando o limite é excedido, a API responde com `429 Too Many Requests` e headers de controle.

### 9. Tratamento global de erros
Os erros são centralizados em um handler global, retornando payload consistente para cenários como:

- validação de entrada;
- recurso não encontrado;
- conflito de regra de negócio;
- erro interno.

### 10. Correlation ID
A aplicação utiliza `X-Correlation-Id` para rastreabilidade de requisições e propagação em payloads de webhook.

---

## Arquitetura e organização do projeto

A estrutura foi separada por responsabilidade e contexto de domínio:

```bash
src/main/java/com/project/gateway_pagamento
├── config/
├── idempotency/
├── merchants/
├── payments/
│   ├── api/
│   ├── domain/
│   └── infra/
├── security/
├── shared/
└── webhooks/
    ├── api/
    ├── domain/
    ├── infra/
    └── service/
```

### Papel de cada módulo

- **config**: configurações gerais da aplicação, segurança, health check e cliente HTTP de webhook.
- **idempotency**: persistência e reaproveitamento de respostas para requisições idempotentes.
- **merchants**: entidade e repositório de lojistas.
- **payments/api**: controllers, requests e responses da API de pagamentos.
- **payments/domain**: regras de negócio de pagamento.
- **payments/infra**: acesso a dados de pagamentos.
- **security**: autenticação por API key, contexto do merchant, correlation id e rate limiting.
- **shared**: paginação, respostas de erro e exceções compartilhadas.
- **webhooks**: configuração, persistência, criação de eventos e dispatcher de webhooks.

---

## Modelo de domínio resumido

### Payment
Representa a transação de pagamento.

Campos principais:

- `id`
- `merchantId`
- `amount`
- `currency`
- `paymentMethod`
- `status`
- `createdAt`

### Merchant
Representa o lojista autenticado na plataforma.

Campos principais:

- `id`
- `name`
- `status`
- `apiKey`
- `createdAt`

### IdempotencyKey
Armazena o hash da requisição e a resposta já devolvida para evitar duplicidade.

Chave composta por:

- `merchantId`
- `key`

### Webhook
Configuração de endpoint de notificação por merchant.

### WebhookEvent
Evento persistido para envio assíncrono e retry.

---

## Banco de dados e migrations

O versionamento do banco é feito com **Flyway**, com migrations para:

- criação da tabela `payments`;
- criação da tabela `idempotency_keys`;
- criação da tabela `merchants`;
- adição da `api_key` em merchants;
- adaptação da idempotência para cenário **multi-tenant**;
- criação da tabela `webhooks`;
- criação da tabela `webhook_events`.

---

## Como executar o projeto localmente

### Pré-requisitos

- Java 21
- Maven 3.9+ ou uso do Maven Wrapper (`./mvnw`)
- Docker e Docker Compose

### 1. Subir o PostgreSQL

```bash
docker-compose up -d
```

O banco será exposto em:

- **host:** `localhost`
- **porta:** `5433`
- **database:** `gatewaydb`
- **username:** `gateway`
- **password:** `gateway`

### 2. Executar a aplicação

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

### 3. URL base da API

```bash
http://localhost:8080
```

---

## Configurações principais

As configurações principais da aplicação incluem:

- conexão com PostgreSQL em `localhost:5433`;
- `ddl-auto=validate` para validar o schema existente;
- Flyway habilitado para migrations automáticas;
- endpoints do actuator expostos para health/info.

---

## Endpoints principais

## Health check

### `GET /health`
Retorna um health check simples da aplicação.

### `GET /actuator/health`
Health check via Spring Boot Actuator.

---

## Pagamentos

### `POST /v1/payments`
Cria um novo pagamento.

#### Headers

```http
X-API-Key: sk_live_loja2_456
Idempotency-Key: pagamento-001
Content-Type: application/json
```

#### Body

```json
{
  "amount": 5000,
  "currency": "BRL",
  "paymentMethod": "card",
  "cardToken": "tok_test_123"
}
```

#### Exemplo de resposta

```json
{
  "id": "7f5f6c6a-8f1a-4cb8-a5be-f8ff6d9f9d31",
  "status": "AUTHORIZED",
  "amount": 5000,
  "currency": "BRL",
  "paymentMethod": "card",
  "merchantId": "loja2"
}
```

---

### `GET /v1/payments?page=0&size=10`
Lista os pagamentos do merchant autenticado com paginação.

#### Query params

- `page`: página atual
- `size`: tamanho da página
- `status` *(opcional)*: filtra por status

#### Exemplo de resposta

```json
{
  "items": [
    {
      "id": "7f5f6c6a-8f1a-4cb8-a5be-f8ff6d9f9d31",
      "status": "AUTHORIZED",
      "amount": 5000,
      "currency": "BRL",
      "paymentMethod": "card",
      "merchantId": "loja2"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### `GET /v1/payments/{id}`
Busca um pagamento por ID.

---

### `POST /v1/payments/{id}/capture`
Captura um pagamento que esteja em status `AUTHORIZED`.

---

### `POST /v1/payments/{id}/refund`
Reembolsa um pagamento que esteja em status `CAPTURED`.

---

## Webhooks

### `GET /v1/webhooks`
Consulta a configuração atual de webhook do merchant autenticado.

- retorna `200 OK` se existir webhook configurado;
- retorna `204 No Content` se não existir configuração.

### `PUT /v1/webhooks`
Cria ou atualiza a configuração de webhook do merchant.

#### Body

```json
{
  "url": "https://meusistema.com/webhooks/pagamentos",
  "enabled": true
}
```

#### Exemplo de resposta

```json
{
  "merchantId": "loja2",
  "url": "https://meusistema.com/webhooks/pagamentos",
  "enabled": true,
  "createdAt": "2026-02-18T20:00:00Z",
  "updatedAt": "2026-02-18T20:05:00Z"
}
```

---

## Exemplo de uso com cURL

### Criar pagamento

```bash
curl --location 'http://localhost:8080/v1/payments' \
--header 'X-API-Key: sk_live_loja2_456' \
--header 'Idempotency-Key: pagamento-001' \
--header 'Content-Type: application/json' \
--data '{
  "amount": 5000,
  "currency": "BRL",
  "paymentMethod": "card",
  "cardToken": "tok_test_123"
}'
```

### Listar pagamentos

```bash
curl --location 'http://localhost:8080/v1/payments?page=0&size=10' \
--header 'X-API-Key: sk_live_loja2_456'
```

### Capturar pagamento

```bash
curl --location --request POST 'http://localhost:8080/v1/payments/{id}/capture' \
--header 'X-API-Key: sk_live_loja2_456'
```

### Reembolsar pagamento

```bash
curl --location --request POST 'http://localhost:8080/v1/payments/{id}/refund' \
--header 'X-API-Key: sk_live_loja2_456'
```

### Configurar webhook

```bash
curl --location --request PUT 'http://localhost:8080/v1/webhooks' \
--header 'X-API-Key: sk_live_loja2_456' \
--header 'Content-Type: application/json' \
--data '{
  "url": "https://meusistema.com/webhooks/pagamentos",
  "enabled": true
}'
```

---

## Regras de negócio implementadas

- todo pagamento criado já nasce com status **AUTHORIZED**;
- o merchant autenticado é obtido a partir da **API Key**;
- a listagem de pagamentos retorna apenas os registros do merchant autenticado;
- o mesmo `Idempotency-Key` com o mesmo payload devolve a mesma resposta previamente salva;
- o mesmo `Idempotency-Key` com payload diferente gera conflito;
- a idempotência é isolada por merchant, permitindo a mesma chave para lojistas diferentes;
- captura só é permitida para pagamentos em `AUTHORIZED`;
- reembolso só é permitido para pagamentos em `CAPTURED`;
- webhooks só são enfileirados quando existe configuração habilitada para o merchant;
- falhas no envio de webhook geram retry automático;
- merchants inativos recebem bloqueio de acesso;
- requisições acima do limite configurado retornam `429 Too Many Requests`.

---

## Segurança e confiabilidade

O projeto implementa alguns mecanismos relevantes de segurança e robustez:

- autenticação stateless com API key;
- separação de contexto por merchant;
- rate limiting por merchant;
- correlation id para rastreabilidade;
- validação de payload com Bean Validation;
- tratamento global de exceções;
- persistência de eventos de webhook para evitar perda de notificações;
- retries em falhas de integração.

---

## Testes

O projeto possui **testes de integração** com:

- **Spring Boot Test**
- **MockMvc**
- **Testcontainers**
- **PostgreSQL**

### Cenários já cobertos

- replay idempotente retorna o mesmo pagamento;
- conflito de idempotência retorna `409 Conflict`;
- mesma chave idempotente para merchants diferentes gera pagamentos distintos;
- listagem retorna apenas pagamentos do merchant autenticado.

### Como rodar os testes

```bash
./mvnw test
```

No Windows:

```bash
mvnw.cmd test
```

---

## Possíveis melhorias futuras

Como evolução de produto e arquitetura, este projeto pode crescer para incluir:

- documentação OpenAPI/Swagger;
- observabilidade com métricas e tracing distribuído;
- fila dedicada para despacho de webhooks;
- assinatura criptográfica de payloads de webhook;
- expiração e limpeza automática de chaves de idempotência;
- autorização por perfis e escopos;
- suporte a cancelamento de pagamento;
- testes unitários adicionais para serviços e filtros;
- containerização completa da aplicação.

---

## Diferenciais para portfólio

Este projeto demonstra experiência prática com temas muito valorizados em back-end:

- design de API REST;
- regras de negócio de pagamentos;
- segurança aplicada com filtros customizados;
- idempotência em operações críticas;
- arquitetura organizada por contexto;
- integração resiliente com webhooks;
- testes de integração realistas com banco efêmero.

---

## Autor

**Matheus Moya Oliveira**  
GitHub: [matheusmoyaoliveira](https://github.com/matheusmoyaoliveira)

---

## Licença

Este projeto pode ser utilizado para fins de estudo, portfólio e evolução técnica.
