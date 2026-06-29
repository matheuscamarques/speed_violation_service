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
- Docker (opcional)
- Make (opcional, mas recomendado)

## Execução

```bash
# Desenvolvimento (hot reload)
make run
# ou
cd SpeedViolationService && ./mvnw spring-boot:run

# Build
make build
# ou
cd SpeedViolationService && ./mvnw clean package -DskipTests

# Checkstyle (valida convenções)
make checkstyle
# ou
cd SpeedViolationService && ./mvnw validate

# Testes
make test
# ou
cd SpeedViolationService && ./mvnw test

# CI completo (checkstyle + testes)
make ci

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

## Arquitetura Hexagonal (Ports & Adapters)

O projeto adota **Arquitetura Hexagonal** (Alistair Cockburn) para isolar o domínio de infraestrutura e frameworks, organizado em dois grandes grupos:

```
src/main/java/br/com/velsis/SpeedViolationService/
├── domain/                    # NÚCLEO — zero dependências de framework
│   ├── model/                 #   Entidades e value objects do domínio
│   ├── port/
│   │   ├── inbound/           #   Interfaces de entrada (use cases)
│   │   └── outbound/          #   Interfaces de saída (repositories, etc.)
│   └── service/               #   Implementação dos use cases
├── adapter/                   # ADAPTADORES — infraestrutura concreta
│   ├── inbound/web/           #   Entrada: REST controller
│   ├── outbound/persistence/  #   Saída: repositórios (in-memory, JPA)
│   └── config/                #   Configuração dos beans de domínio
├── dto/                       # Objetos de transferência (contrato da API)
├── config/                    # Configuration properties
└── exception/                 # Tratamento global de erros
```

### Por que hexagonal?

| Princípio | Benefício |
|-----------|-----------|
| **Domínio puro** | `domain/` não importa nada do Spring, JPA ou HTTP. Pode ser testado isoladamente. |
| **Portas como contratos** | `ViolationRepository` e `ViolationEvaluationUseCase` definem fronteiras claras. |
| **Adaptadores trocáveis** | A persistência pode ser in-memory, JPA/H2, PostgreSQL ou MongoDB sem alterar uma linha do domínio. |
| **Testabilidade** | O use case é testado com mocks das portas; os adaptadores são testados separadamente. |

### Persistência: in-memory vs H2

O repositório de persistência é selecionado por profile do Spring:

| Profile | Implementação | Uso |
|---------|--------------|-----|
| *(default)* | `InMemoryViolationRepository` com `ConcurrentHashMap` | Desenvolvimento e testes rápidos |
| `h2` | `ViolationRepositoryJpaAdapter` com Spring Data JPA + H2 file-based | Persistência entre reinicializações |

**Por que não JPA direto no domínio?** A regra de negócio (`ViolationEvaluationService`) depende apenas da interface `ViolationRepository`. Se amanhã o banco mudar, troca-se o adaptador — o domínio permanece intacto. O H2 via profile `h2` oferece persistência real sem acoplar o código a um dialeto SQL específico.

```bash
# Com persistência em memória (padrão)
./mvnw spring-boot:run

# Com persistência H2 (arquivo em ./data/speedviolation)
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2

# Console H2 disponível em http://localhost:8080/h2-console
```

## Estilo e design

O código segue convenções deliberadas para manter consistência e previsibilidade:

### Sem `var`

Tipos são sempre declarados explicitamente — a legibilidade não é sacrificada por economia de digitação:

```java
// ✅ Sim
Violation violation = Violation.of(...);
ViolationResponse response = toResponse(violation);
double tolerance = calculateTolerance(request.speedLimit());

// ❌ Não
var violation = Violation.of(...);
var response = toResponse(violation);
var tolerance = calculateTolerance(request.speedLimit());
```

### Paradigma funcional

Prefere-se imutabilidade, records, funções puras (métodos `static` que não dependem de estado) e streams a loops imperativos:

```java
// ✅ Sim — record imutável, static factory, stream com method reference
public record Violation(UUID id, String licensePlate, ...) {
    public static Violation of(...) { ... }
}

public List<ViolationResponse> findByLicensePlate(String licensePlate) {
    return violationRepository.findByLicensePlate(licensePlate).stream()
            .map(ViolationEvaluationService::toResponse)
            .toList();
}

private static ViolationResponse toResponse(Violation violation) { ... }
private static double excessPercentage(double speed, double limit) { ... }

// ❌ Não — mutabilidade, loops imperativos, classes anêmicas
public class Violation {
    private String licensePlate;
    public void setLicensePlate(String licensePlate) { ... }
}

List<ViolationResponse> result = new ArrayList<>();
for (Violation v : violations) {
    result.add(toResponse(v));
}
```

### Evitar method chaining (fluent interfaces)

Encadeamento é evitado fora de streams, que são expressões funcionais autossuficientes. Construtores com parâmetros nomeados (via records ou classes) substituem builders:

```java
// ✅ Sim — construtor com parâmetros explícitos
return new ViolationResponse(
        licensePlate, equipmentId, measuredSpeed, consideredSpeed,
        speedLimit, excessPercentage, hasViolation, violation, processedAt
);

// ❌ Não
return new ViolationResponse.Builder()
        .licensePlate(licensePlate)
        .equipmentId(equipmentId)
        .measuredSpeed(measuredSpeed)
        .build();
```

Exceção: cadeias funcionais em streams e operações matemáticas são aceitas por serem autoexplicativas:

```java
// ✅ Aceito — stream funcional
return repository.findByLicensePlate(plate).stream()
        .map(Entity::toDomain)
        .toList();

// ✅ Aceito — operação matemática
return BigDecimal.valueOf((speed - limit) / limit * 100)
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();
```

### Evitar operador ternário

Condicionais usam `if/else` com retorno explícito — o fluxo fica evidente sem compressão sintática:

```java
// ✅ Sim
private ViolationSeverity classifySeverity(double excess) {
    if (excess <= config.getExcessLimitMedium()) {
        return ViolationSeverity.MEDIUM;
    }
    if (excess <= config.getExcessLimitSerious()) {
        return ViolationSeverity.SERIOUS;
    }
    return ViolationSeverity.VERY_SERIOUS;
}

// ❌ Não
return excess <= config.getExcessLimitMedium()
        ? ViolationSeverity.MEDIUM
        : excess <= config.getExcessLimitSerious()
            ? ViolationSeverity.SERIOUS
            : ViolationSeverity.VERY_SERIOUS;
```

```java
// ✅ Sim
private double calculateTolerance(double speedLimit) {
    if (speedLimit > config.getThreshold()) {
        return speedLimit * (config.getTolerancePercentage() / 100.0);
    }
    return config.getToleranceFixed();
}

// ❌ Não
return speedLimit > config.getThreshold()
        ? speedLimit * (config.getTolerancePercentage() / 100.0)
        : config.getToleranceFixed();
```

## Decisões técnicas e justificativas

### Organização do código

- **`domain/model/`** – entidades do domínio (`Violation` como record, `ViolationSeverity` como enum). Sem qualquer dependência de framework.
- **`domain/port/`** – interfaces que definem os contratos entre o núcleo e o mundo externo.
- **`domain/service/`** – lógica de negócio pura, dependendo apenas das portas.
- **`adapter/inbound/web/`** – controllers REST com validação exclusiva de transporte (`x-origin`) e delegação imediata ao use case.
- **`adapter/outbound/persistence/`** – implementações concretas de `ViolationRepository`: `InMemoryViolationRepository` (profile default) e `ViolationRepositoryJpaAdapter` (profile `h2`).
- **`adapter/outbound/config/`** – adaptador que expõe `SpeedViolationProperties` ao domínio via `ViolationConfigPort`.
- **`dto/`** – objetos de transferência com validação declarativa (`@NotBlank`, `@Pattern`, `@Positive`, `@PastOrPresent`). Desacoplam o contrato da API do modelo interno.
- **`config/`** – propriedades externalizadas (`@ConfigurationProperties`) para ajustar tolerâncias e limites sem recompilar.
- **`exception/`** – tratamento centralizado com `@RestControllerAdvice`, respostas consistentes e logs estruturados.

### Justificativas

- **`record` em vez de classe** – imutabilidade por padrão, equals/hashCode/toString gratuitos, ideal para value objects e DTOs.
- **Porta `ViolationRepository` em vez de JPA direto no service** – a regra de negócio independe do banco; a implementação concreta (in-memory ou JPA) é injetada sem impacto no domínio.
- **Profiles para alternar persistência** – `h2` ativa JPA; sem profile, usa-se `ConcurrentHashMap`. Nenhuma linha do domínio muda.
- **Tolerância variável** – limites até 100 km/h usam tolerância fixa de 7 km/h; acima disso, 7% (prática adotada por órgãos de trânsito brasileiros).
- **Classificação por excesso percentual** – alinhada aos arts. 218-I (até 20%), 218-II (20% a 50%) e 218-III (acima de 50%) do CTB.
- **Validação no controller** – `x-origin` validado no controller por ser concern de transporte; os demais campos validados no DTO com Bean Validation.
- **JaCoCo 85%** – cobertura obrigatória para garantir qualidade sem burocracia extrema.
