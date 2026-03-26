# Backend API - Sistema de Votação

Solução para gerenciamento de pautas e sessões de votação em assembleias, desenvolvida com Java 21 e Spring Boot. O projeto utiliza processamento assíncrono para garantir performance em cenários de alta volumetria.

## Diferenciais Técnicos

*   **Processamento Assíncrono:** Uso de Apache Kafka para desacoplar a recepção de votos da persistência em banco de dados.
*   **Virtual Threads:** Configuração nativa do Java 21 para otimizar o uso de recursos em chamadas bloqueantes.
*   **Fail-Fast:** Validação de formato de CPF (@CPF) na camada de entrada (DTO).
*   **Resiliência:** Tratamento global de exceções e integridade garantida por constraints de banco de dados.

## Stack Tecnológica

*   **Linguagem:** Java 21
*   **Framework:** Spring Boot 4.0.4
*   **Mensageria:** Apache Kafka (Broker em modo KRaft)
*   **Banco de Dados:** PostgreSQL 16
*   **Migrações:** Flyway
*   **Integração:** RestClient (Spring 3.2+)

## Arquitetura e Fluxo de Votação

1.  **Validação:** O sistema verifica o formato do CPF, a existência da pauta e o status da sessão (aberta/fechada).
2.  **Integração (Bônus 1):** Consulta um serviço externo (simulado localmente) para validar a elegibilidade do associado.
3.  **Mensageria (Bônus 2):** O voto é publicado em um tópico Kafka e o cliente recebe imediatamente um 202 Accepted.
4.  **Consumo:** Um listener assíncrono processa a mensagem e persiste o voto no PostgreSQL.

## Endpoints (v1)

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| POST | /api/v1/pautas | Cadastro de nova pauta |
| POST | /api/v1/pautas/{id}/sessao | Abertura de sessão (1min por padrão) |
| POST | /api/v1/pautas/{id}/votos | Recebimento de voto (Assíncrono) |
| GET | /api/v1/pautas/{id}/resultado | Contabilização e status final |

## Execução

### Infraestrutura
```bash
docker-compose up -d