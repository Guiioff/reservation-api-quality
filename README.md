# Reservation API Quality
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-%23f5f5f5.svg?style=for-the-badge&logo=junit5&logoColor=dc524a)
![Mockito](https://img.shields.io/badge/Mockito-%2378C257.svg?style=for-the-badge&logo=mockito&logoColor=white)
![REST Assured](https://img.shields.io/badge/REST%20Assured-%234A4A4A.svg?style=for-the-badge)
![JMeter](https://img.shields.io/badge/Apache%20JMeter-%23D22128.svg?style=for-the-badge&logo=apachejmeter&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)

API REST para gerenciamento de reservas, desenvolvida com foco em **qualidade de software, automação de testes e testes de performance**.

O projeto contém testes unitários, testes de integração, testes de performance com JMeter e pipeline de CI.

## Tecnologias
- Java 21
- Spring Boot (Web, JPA, Validation)
- PostgreSQL
- JUnit 5
- Mockito
- Rest Assured
- Testcontainers
- JMeter
- GitHub Actions

## Estrutura do Projeto
``` text
reservation-api-quality/
│
├── src/
│   │
│   ├── main/
│   │   │
│   │   ├── java/
│   │   │   └── br.com.devgui.reservationapi/
│   │   │       ├── api/
│   │   │       │   ├── controller/          # Controllers da API
│   │   │       │   ├── dto/                 # Objetos de transferência de dados
│   │   │       │   │   ├── error/           # DTOs de erro
│   │   │       │   │   ├── request/         # DTOs de entrada
│   │   │       │   │   └── response/        # DTOs de saída
│   │   │       │   ├── exception/           # Exceções relacionadas à API
│   │   │       │   └── mapper/              # Conversão entre DTOs e entidades
│   │   │       ├── domain/
│   │   │       │   ├── exception/            # Exceções de domínio
│   │   │       │   ├── model/                # Entidades e modelos de domínio
│   │   │       │   └── service/              # Regras de negócio
│   │   │       └── infrastructure/
│   │   │           ├── config/               # Configurações da aplicação
│   │   │           └── repository/           # Repositórios de persistência
│   │   └── resources/
│   │       └── application.properties       # Configurações da aplicação
│   │
│   └── test/
│       └── java/
│           └── br.com.devgui.reservationapi/
│               ├── api/controller/           # Testes dos controllers
│               ├── domain/service/           # Testes dos serviços
│               ├── infrastructure/repository/ # Testes dos repositórios
│               ├── integration/              # Testes de integração
│               └── testconfig/               # Configuração dos Testcontainers

├── jmeter/
│   ├── data/
│   │   └── reservations.csv                  # Dados utilizados nos testes
│   ├── plans/
│   │   └── load_test.jmx                     # Plano de teste de carga
│   └── results/
│       └── load_test/
│           ├── 10-users/                      # Resultados com 10 usuários
│           ├── 50-users/                      # Resultados com 50 usuários
│           └── 100-users/                     # Resultados com 100 usuários
├── .github/
│   └── workflows/                             # Workflows do GitHub Actions
├── pom.xml                                    # Dependências e configuração Maven
└── README.md
```

## Endpoints
| Método | Endpoint| Descrição |
| -------- | -------- | -------- |
|``POST``|``/api/v1/reservations``|Criar uma reserva|
|``GET``|``/api/v1/reservations/{id}``|Buscar uma reserva|
|``GET``|``/api/v1/reservations``|Listar reservas|
|``PATCH``|``/api/v1/reservations/{id}/confirm``|Confirmar uma reserva|
|``PATCH``|``/api/v1/reservations/{id}/cancel``|Cancelar uma reserva|
|``PATCH``|``/api/v1/reservations/{id}/complete``|Concluir uma reserva|

## Regras de negócio
- A data de início deve ser anterior à data de término.
- O número de pessoa em uma reserva não pode ser 0 ou negativo.
- Não são permitidas reservas com horários conflitantes.
- Uma reserva `PENDING` pode ser confirmada ou cancelada.
- Uma reserva `CONFIRMED` pode ser concluída ou cancelada.
- Uma reserva `COMPLETED` não pode ser cancelada.
- Uma reserva `CANCELLED` não pode ser confirmada ou concluída.

## Como Executar
### 1. Definir Variáveis de Ambiente
Copie o arquivo `.env.example`:

``` bash
cp .env.example .env
```

e defina os valores:

```env
# Server configuration
SERVER_PORT=

# Database configuration
DB_HOST=
DB_USERNAME=
DB_PASSWORD=
DB_NAME=
DB_PORT=
```
### 2. Subir o Banco de Dados
```bash
docker compose up -d
```
### 3. Executar a aplicação

```bash
./mvnw spring-boot:run
```

A aplicação ficará disponível na porta configurada:

```text
http://localhost:${SERVER_PORT}
```
---
## Estratégia de Testes
O projeto utiliza diferentes níveis de testes para validar regras de negócio, integração entre componentes e performance.

### Testes Unitários
Testes focados nas regras de negócio e componentes isolados da aplicação, utilizando **JUnit 5** e **Mockito**.
- Services
- Controllers
- Repositories
### Testes de integração
Testam o funcionamento da aplicação através de requisições HTTP reais, validando a integração entre os componentes da aplicação e o banco de dados.
Utilizam:
- **Rest Assured** para realizar as requisições HTTP;
- **Testcontainers** para executar uma instância real do PostgreSQL durante os testes.

Os testes cobrem cenários de sucesso, validação, recursos inexistentes, conflitos e transições de estado das reservas.

### Testes de performance
Testes realizados para avaliar o comportamento da API sob diferentes níveis de carga, analisando tempo de resposta, throughput e taxa de erros.

## Cenários de teste

| Categoria | Cenário |
|---|---|
| Criação | Criação com dados válidos |
| Criação | Dados inválidos |
| Criação | Horário conflitante |
| Criação | Quantidade inválida de pessoas |
| Consulta | Reserva existente |
| Consulta | Reserva inexistente |
| Estado | `PENDING` → `CONFIRMED` |
| Estado | `PENDING` → `CANCELLED` |
| Estado | `CONFIRMED` → `COMPLETED` |
| Estado | `CONFIRMED` → `CANCELLED` |
| Estado | Transições inválidas |

---
## Testes de performance com JMeter
### Cenário
Foi desenvolvido um **Teste de Carga (Load Test)** para avaliar o comportamento da API com diferentes quantidades de usuários simultâneos.
Foram executados cenários com:
- 10 usuários
- 50 usuários
- 100 usuários

O fluxo testado consiste em:
```
Criar reserva 
        ↓ 
Extrair ID da reserva 
        ↓ 
Consultar reserva 
        ↓ 
Confirmar reserva
```
### Configuração
Os dados das reservas são fornecidos através de um arquivo CSV e utilizados dinamicamente durante a execução do teste.

O plano de teste utiliza:
- ``Thread Group``
- ``CSV Data Set Config``
- ``HTTP Request``
- ``JSON Extractor``
- ``HTTP Header Manager``
- ``Aggregate Report``

As principais métricas analisadas são:
- Average Response Time
- P90
- P95
- P99
- Throughput
- Error %

### Resultados
Os testes foram executados com 10, 50 e 100 usuários simultâneos.

#### 10 usuários
![Resultado para teste de carga com 10 usuários](/docs/images/load_test_result_10_users.png)

> Report completo em: ``jmeter/results/load_test/10-users/report``

#### 50 usuários
![Resultado para teste de carga com 50 usuários](/docs/images/load_test_result_50_users.png)

> Report completo em: ``jmeter/results/load_test/50-users/report``

#### 100 usuários
![Resultado para teste de carga com 100 usuários](/docs/images/load_test_result_100_users.png)

> Report completo em: ``jmeter/results/load_test/100-users/report``

---
## CI
O projeto utiliza GitHub Actions para executar automaticamente os testes a cada push ou pull request direcionado à branch main.

O pipeline realiza:
- Checkout do código
- Configuração do Java 21
- Execução dos testes através do Maven
- Execução dos testes de integração com Testcontainers

---
## Próximos Passos
- [ ] Adicionar Stress Test com JMeter
- [ ] Adicionar Spike Test com JMeter
