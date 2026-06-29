# Speed Violation Service

Microserviço para avaliação de infrações de excesso de velocidade com base nos arts. 218-I, 218-II e 218-III do CTB (Código de Trânsito Brasileiro).

## Tecnologias

- **Java 21** – versão LTS com records, pattern matching e sealed classes
- **Spring Boot 4.1** – Web MVC, Validation, Data JPA, OpenTelemetry
- **Maven** – build e gerenciamento de dependências
- **H2** – banco de dados em memória (runtime)
- **Lombok** – redução de boilerplate
- **JaCoCo** – métricas de cobertura (85% instrução + branch)
- **Spring Cloud Contract** – testes de contrato para a API
- **JUnit 5 + Mockito + AssertJ** – testes unitários e de integração
- **Docker + Make** – containerização e automação de comandos

## Pré-requisitos

- JDK 21
- Maven 3.9+
- Docker (opcional)
- Make (opcional, mas recomendado)

## Execução

```bash
# Desenvolvimento (hot reload)
make run
# ou
cd SpeedViolationService && mvn spring-boot:run

# Build
make build
# ou
cd SpeedViolationService && mvn clean package -DskipTests

# Docker
make docker-build && make docker-run
```

A aplicação inicia em `http://localhost:8080`.

## Exemplos de uso com curl

### Infração (SERIOUS)

```bash
curl -s -X POST http://localhost:8080/api/v1/violations/evaluate \
  -H "x-origin: FIXED" \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "ABC1D23",
    "measuredSpeed": 92,
    "speedLimit": 60,
    "equipmentId": "RAD-CWB-001",
    "captureTimestamp": "2026-06-08T14:30:00Z"
  }' | jq
```

```json
{
  "licensePlate": "ABC1D23",
  "equipmentId": "RAD-CWB-001",
  "measuredSpeed": 92.0,
  "consideredSpeed": 85.0,
  "speedLimit": 60.0,
  "excessPercentage": 41.67,
  "hasViolation": true,
  "violation": {
    "severity": "SERIOUS",
    "ctbCode": "218-II"
  },
  "processedAt": "2026-06-29T12:00:00Z"
}
```

### Sem infração (dentro da tolerância)

```bash
curl -s -X POST http://localhost:8080/api/v1/violations/evaluate \
  -H "x-origin: FIXED" \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "ABC1D23",
    "measuredSpeed": 67,
    "speedLimit": 60,
    "equipmentId": "RAD-001",
    "captureTimestamp": "2026-06-08T14:30:00Z"
  }' | jq
```

```json
{
  "licensePlate": "ABC1D23",
  "equipmentId": "RAD-001",
  "measuredSpeed": 67.0,
  "consideredSpeed": 60.0,
  "speedLimit": 60.0,
  "excessPercentage": 0,
  "hasViolation": false,
  "processedAt": "2026-06-29T12:00:00Z"
}
```

### Erro (placa inválida)

```bash
curl -s -X POST http://localhost:8080/api/v1/violations/evaluate \
  -H "x-origin: FIXED" \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "INVALID",
    "measuredSpeed": 80,
    "speedLimit": 60,
    "equipmentId": "RAD-001",
    "captureTimestamp": "2026-06-08T14:30:00Z"
  }' | jq
```

```json
{
  "status": 400,
  "error": "Validation failed",
  "message": "[licensePlate: must match \"[A-Z]{3}[0-9][A-Z][0-9]{2}|[A-Z]{3}[0-9]{4}\"]",
  "path": "/api/v1/violations/evaluate",
  "timestamp": "2026-06-29T12:00:00Z"
}
```

### Consulta de infrações por placa

```bash
curl -s http://localhost:8080/api/v1/violations?licensePlate=ABC1D23 | jq
```

## Testes

```bash
# Executar testes unitários
make test
# ou
cd SpeedViolationService && mvn test

# Relatório de cobertura (HTML em target/site/jacoco/index.html)
make coverage
# ou
cd SpeedViolationService && mvn clean test jacoco:report

# Testes no Docker
make docker-test
```

O projeto exige cobertura mínima de **85%** (instrução e branch), verificada por JaCoCo.

## Decisões técnicas e justificativas

### Organização do código (RNF1)

A arquitetura segue os princípios de **separação de responsabilidades** com pacotes bem definidos:

- **`controller`** – camada de entrada HTTP. Contém apenas validação de cabeçalho (`x-origin`) e delega ao service. Sem lógica de negócio.
- **`service`** – lógica central de avaliação. Aplica a tolerância (fixa de 7 km/h para limites ≤ 100 km/h, percentual de 7% para > 100 km/h), calcula o excesso e classifica a gravidade conforme o CTB.
- **`model`** – domínio puro. `Violation` (record imutável) e `ViolationSeverity` (enum com códigos do CTB). Sem dependências do framework.
- **`dto`** – objetos de transferência com validação declarativa (`@NotBlank`, `@Pattern`, `@Positive`, `@PastOrPresent`). Desacoplam o contrato da API do modelo interno.
- **`store`** – abstração de persistência. Implementação em memória com `ConcurrentHashMap` para simplicidade, trocável por um repositório JPA sem impacto nas demais camadas.
- **`config`** – configurações externalizadas via `@ConfigurationProperties`, permitindo ajustar tolerâncias e limites sem recompilar.
- **`exception`** – tratamento centralizado de erros com `@RestControllerAdvice`, garantindo respostas consistentes (400 para validação, 500 para erros internos) e logs estruturados.

### Justificativas

- **`record` em vez de classe** – imutabilidade por padrão, elimina boilerplate de equals/hashCode/toString, ideal para DTOs e modelos de valor.
- **`ViolationStore` em vez de JPA direto** – a regra de negócio independe do banco; a store pode ser substituída por uma implementação JPA ou MongoDB sem alterar o service.
- **Tolerância variável** – limites até 100 km/h usam tolerância fixa de 7 km/h; acima disso, 7% (prática adotada por órgãos de trânsito brasileiros).
- **Classificação por excesso percentual** – alinhada aos arts. 218-I (até 20%), 218-II (20% a 50%) e 218-III (acima de 50%) do CTB.
- **Validação no controller** – `x-origin` validado no controller por ser concern de transporte; os demais campos validados no DTO com Bean Validation.
- **JaCoCo 85%** – cobertura obrigatória para garantir qualidade sem burocracia extrema.
