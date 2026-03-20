# Gateway de Pagamento

API REST desenvolvida em **Java 21 + Spring Boot** para simular operações centrais de um gateway de pagamento, com foco em **segurança, consistência transacional, multi-tenant, idempotência, webhooks e testes de integração**.

> Projeto de portfólio construído para demonstrar competências práticas de back-end aplicadas a um cenário de mercado: autenticação por API Key, proteção contra requisições duplicadas, limitação de taxa por merchant, isolamento de dados e comunicação assíncrona por eventos.

## Stack

- **Java 21**
- **Spring Boot 3**
- **Spring Web**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway**
- **Bucket4j**
- **Spring WebFlux**
- **JUnit 5**
- **MockMvc**
- **Testcontainers**
- **Docker Compose**
- **Maven**

## Principais funcionalidades

- Criação de pagamentos com status inicial `AUTHORIZED`
- Captura e reembolso respeitando o ciclo de vida do pagamento
- Autenticação por `X-API-Key` para identificar o merchant chamador
- Suporte a `Idempotency-Key` para evitar transações duplicadas
- Isolamento **multi-tenant** para separar dados por merchant
- **Rate limiting** por merchant para proteção da API
- Cadastro e disparo de **webhooks** para eventos de pagamento
- Versionamento de banco com **Flyway**
- Testes de integração com **Testcontainers + PostgreSQL**

## Visão geral

Este projeto representa a base de um gateway responsável por receber requisições de pagamento de diferentes lojistas, validar o acesso, registrar transações, controlar o ciclo de vida do pagamento e notificar sistemas externos via webhook.

Além do fluxo principal de criação de pagamentos, a aplicação implementa preocupações que aparecem em sistemas reais:

- **Autenticação por API Key** para identificar o merchant chamador
- **Idempotência** para evitar pagamentos duplicados em reenvios da mesma requisição
- **Isolamento multi-tenant** para separar dados por merchant
- **Rate limiting** para proteção básica contra abuso da API
- **Webhooks** para integração orientada a eventos
- **Tratamento padronizado de erros** para consumo mais previsível da API
- **Testes de integração com Testcontainers** usando PostgreSQL real

## Destaques técnicos

### 1. Idempotência com isolamento por merchant
A API suporta o header `Idempotency-Key` na criação de pagamentos. Quando a mesma chave é reutilizada com o mesmo payload para o mesmo merchant, a aplicação retorna a mesma resposta original, evitando a criação de transações duplicadas.

Se a mesma chave for reutilizada com payload diferente, a API responde com conflito.

Além disso, a chave é tratada em contexto **multi-tenant**, permitindo que merchants diferentes reutilizem a mesma idempotency key sem colisão entre si.

### 2. Segurança por API Key
Todas as rotas de negócio exigem o header `X-API-Key`. O filtro de autenticação identifica o merchant, valida se ele está ativo e injeta esse contexto durante o processamento da requisição.

### 3. Rate limiting por merchant
A aplicação possui limitação de taxa com **Bucket4j**, aplicando uma política por merchant para reduzir risco de abuso e excesso de chamadas.

### 4. Webhooks para eventos de pagamento
O sistema permite cadastrar um endpoint de webhook por merchant e enfileira eventos relacionados ao ciclo de vida do pagamento, como:

- `payment.authorized`
- `payment.captured`
- `payment.refunded`

### 5. Testes de integração realistas
Os testes usam **Testcontainers + PostgreSQL**, o que aproxima o ambiente de teste do comportamento real da aplicação, especialmente em cenários de persistência, autenticação e idempotência.

## Tecnologias utilizadas

- **Java 21**
- **Spring Boot 3**
- **Spring Web**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway**
- **Bucket4j**
- **Spring WebFlux**
- **JUnit 5**
- **MockMvc**
- **Testcontainers**
- **Docker Compose**
- **Maven**

## Arquitetura e organização do projeto

A estrutura foi separada por responsabilidades de domínio e infraestrutura:

```text
src/main/java/com/project/gateway_pagamento
├── config
├── idempotency
├── merchants
├── payments
│   ├── api
│   ├── domain
│   └── infra
├── security
├── shared
└── webhooks
    ├── api
    ├── domain
    ├── infra
    └── service
```

### Responsabilidade de cada módulo

- **config**: configuração de segurança, health check e clientes auxiliares
- **idempotency**: persistência e tratamento de chaves idempotentes
- **merchants**: dados e repositório de lojistas
- **payments/api**: endpoints, requests e responses
- **payments/domain**: regras de negócio do pagamento
- **payments/infra**: acesso a dados
- **security**: filtros, contexto do merchant e rate limiting
- **shared**: paginação, resposta de erro global e exceções customizadas
- **webhooks**: cadastro, eventos e despacho de notificações

## Fluxo principal da aplicação

### Criar pagamento
1. O cliente envia a requisição para `POST /v1/payments`
2. O filtro valida o `X-API-Key`
3. O merchant autenticado é carregado no contexto da requisição
4. A aplicação verifica a `Idempotency-Key`, quando informada
5. Um pagamento é criado com status inicial `AUTHORIZED`
6. Um evento de webhook é enfileirado
7. A resposta é devolvida ao cliente

### Capturar pagamento
Um pagamento em status `AUTHORIZED` pode ser capturado, passando para `CAPTURED`.

### Reembolsar pagamento
Um pagamento em status `CAPTURED` pode ser reembolsado, passando para `REFUNDED`.

## Regras de negócio implementadas

- Apenas requisições autenticadas com `X-API-Key` acessam os endpoints de negócio
- Merchants inativos recebem resposta `403 Forbidden`
- Reenvio da mesma `Idempotency-Key` com mesmo payload retorna a mesma resposta anterior
- Reenvio da mesma `Idempotency-Key` com payload diferente retorna `409 Conflict`
- A listagem de pagamentos retorna apenas registros do merchant autenticado
- Um pagamento só pode ser capturado se estiver em `AUTHORIZED`
- Um pagamento só pode ser reembolsado se estiver em `CAPTURED`
- URL de webhook precisa começar com `http://` ou `https://`

## Modelo de status do pagamento

```text
AUTHORIZED -> CAPTURED -> REFUNDED
```

## Banco de dados e migrations

O projeto utiliza **Flyway** para versionamento do banco de dados.

Migrations presentes:

- `V1__create_payments.sql`
- `V2__create_idempotency_keys.sql`
- `V3__create_merchants.sql`
- `V4__add_api_key_to_merchants.sql`
- `V5__make_idempotency_keys_multi_tenant.sql`
- `V6__create_webhooks.sql`
- `V7__create_webhook_events.sql`

Esse versionamento mostra evolução incremental do sistema e facilita reprodução do ambiente em diferentes máquinas.

## Como executar o projeto localmente

### Pré-requisitos

- Java 21
- Maven
- Docker e Docker Compose

### 1. Subir o banco de dados

```bash
docker compose up -d
```

O PostgreSQL será exposto em:

- **porta:** `5433`
- **database:** `gatewaydb`
- **user:** `gateway`
- **password:** `gateway`

### 2. Executar a aplicação

No diretório do projeto:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

### 3. URL base da aplicação

```text
http://localhost:8080
```

### 4. Health check

```http
GET /actuator/health
```

## Configuração principal

A aplicação está configurada para usar PostgreSQL local com as seguintes propriedades:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/gatewaydb
spring.datasource.username=gateway
spring.datasource.password=gateway
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

## Endpoints principais

### Pagamentos

#### Criar pagamento
```http
POST /v1/payments
```

Headers:

```http
X-API-Key: sk_live_loja2_456
Idempotency-Key: pagamento-001
Content-Type: application/json
```

Body:

```json
{
  "amount": 5000,
  "currency": "BRL",
  "paymentMethod": "card",
  "cardToken": "tok_test_123"
}
```

Resposta esperada:

```json
{
  "id": "c5b0f3c2-3d93-4e0d-a5db-3d2d4d5b1234",
  "status": "AUTHORIZED",
  "amount": 5000,
  "currency": "BRL",
  "paymentMethod": "card",
  "merchantId": "loja2"
}
```

#### Listar pagamentos
```http
GET /v1/payments?page=0&size=10
```

Filtro opcional:

```http
GET /v1/payments?page=0&size=10&status=AUTHORIZED
```

#### Buscar pagamento por ID
```http
GET /v1/payments/{id}
```

#### Capturar pagamento
```http
POST /v1/payments/{id}/capture
```

#### Reembolsar pagamento
```http
POST /v1/payments/{id}/refund
```

### Webhooks

#### Consultar webhook do merchant
```http
GET /v1/webhooks
```

#### Criar ou atualizar webhook
```http
PUT /v1/webhooks
```

Body:

```json
{
  "url": "https://meusistema.com/webhooks/pagamentos",
  "enabled": true
}
```

## Exemplos de erros tratados

### Chave idempotente reutilizada com payload diferente
```json
{
  "code": "idempotency_conflict",
  "message": "Idempotency-Key reused with different payload",
  "details": null
}
```

### Erro de validação
```json
{
  "code": "validation_error",
  "message": "Invalid request",
  "details": {
    "amount": "amount must be greater than 0"
  }
}
```

### Limite de requisições excedido
A API responde com `429 Too Many Requests` e inclui headers como:

```http
Retry-After
X-RateLimit-Limit
X-RateLimit-Remaining
X-RateLimit-Reset
```

## Evidências recomendadas

Para fortalecer este projeto no GitHub e no LinkedIn, vale incluir nesta seção alguns prints ou GIFs curtos, por exemplo:

- requisição de criação de pagamento no Postman/Insomnia
- resposta de replay idempotente retornando o mesmo pagamento
- testes de integração passando no terminal
- health check `GET /actuator/health`
- visão geral da estrutura de pastas

> Sugestão: salve as imagens em uma pasta `docs/images` e referencie aqui no README.

Exemplo de uso:

```md
![Criação de pagamento](docs/images/create-payment.png)
![Testes passando](docs/images/tests-passing.png)
```

## Testes

O projeto contém testes automatizados com foco em cenários relevantes de negócio e integração.

### Cenários cobertos

- replay idempotente retorna o mesmo pagamento
- conflito de idempotência com payload diferente retorna `409`
- mesma idempotency key para merchants diferentes não gera colisão
- listagem retorna apenas pagamentos do merchant autenticado

### Executar testes

```bash
./mvnw test
```

## O que este projeto demonstra

Este projeto foi construído para evidenciar competências valorizadas em desenvolvimento back-end:

- modelagem de API REST
- separação de responsabilidades
- segurança em nível de API
- consistência de operações com idempotência
- isolamento multi-tenant
- tratamento padronizado de exceções
- versionamento de banco com Flyway
- testes de integração com infraestrutura realista
- visão de arquitetura aplicada a problema de negócio

## Possíveis evoluções

Algumas melhorias naturais para próximas versões do projeto:

- documentação OpenAPI/Swagger
- processamento assíncrono mais robusto para webhooks
- observabilidade com métricas e tracing
- autenticação com credenciais rotativas e expiração de chaves
- fila/mensageria para eventos
- circuit breaker e retry policy para entregas externas
- suporte a mais métodos de pagamento
- paginação e filtros mais avançados

## Para recrutadores e avaliadores técnicos

Este projeto não foi pensado apenas como CRUD acadêmico. Ele foi estruturado para simular desafios que aparecem em sistemas de pagamento reais, como:

- proteção contra duplicidade de cobrança
- segregação por cliente/lojista
- proteção de acesso à API
- governança mínima de tráfego
- integração com sistemas terceiros por eventos

## Autor

**Matheus Moya Oliveira**

- GitHub: `matheusmoyaoliveira`

---

Se este projeto fizer sentido para a vaga ou oportunidade, vale observar principalmente os módulos de **payments**, **security**, **idempotency** e **webhooks**, que concentram as decisões técnicas mais relevantes da solução.
