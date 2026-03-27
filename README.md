# Sistema de Votação - Assembleias Cooperativas

Solução web fullstack desenvolvida para o gerenciamento de pautas e deliberações de associados. O projeto foi arquitetado para suportar alta volumetria de votos utilizando processamento assíncrono e oferece uma interface web baseada em estados de sessão.

## Diferenciais Técnicos

### Backend
* **Processamento Assíncrono (Bônus 2):** Implementação de mensageria com Apache Kafka para desacoplar a recepção de votos da persistência em banco de dados, garantindo performance em picos de requisição.
* **Integração Externa (Bônus 1):** Consulta a um serviço externo simulado (Facade/Client Fake) para validar a elegibilidade do associado (`ABLE_TO_VOTE` / `UNABLE_TO_VOTE`) antes do cômputo do voto.
* **Performance:** Utilização de Virtual Threads (Java 21) para otimizar chamadas bloqueantes.
* **Fail-Fast:** Validação de payload (ex: formatação de CPF) e integridade de dados na camada de entrada via DTOs e constraints de banco de dados.

### Frontend
* **UX Defensiva e Controle de Estado:** O formulário de votação reage ativamente ao ciclo de vida da pauta (Criada, Aberta, Encerrada). O acesso à submissão de votos é bloqueado fora do período de sessão.
* **Proxy de Desenvolvimento:** Configuração do Vite para atuar como proxy das requisições `/api`, resolvendo entraves de CORS localmente sem expor a API desnecessariamente.
* **Roteamento Modular:** Estruturação em Single Page Application (SPA) com React Router v7.

## Stack Tecnológica

* **Backend:** Java 21, Spring Boot 4.0.4, Flyway, Maven
* **Mensageria e Persistência:** Apache Kafka (Confluent Local KRaft), PostgreSQL 16
* **Frontend:** React 19, Vite 5, React Router DOM 7, Tailwind CSS, Axios
* **Infraestrutura:** Docker, Docker Compose

## Fluxo de Votação

1. **Cadastro:** A pauta é cadastrada e inicializada com o status inativo.
2. **Abertura:** A sessão é iniciada com um tempo pré-definido via frontend (default de 1 minuto caso não especificado).
3. **Votação:** O associado envia seu voto (SIM/NÃO) informando o CPF.
4. **Validação e Enfileiramento:** O backend verifica a elegibilidade do associado via Facade. Sendo válido e não duplicado, o voto é publicado no tópico do Kafka (HTTP 202 Accepted).
5. **Processamento:** Um listener consome a mensagem assincronamente e persiste a informação no PostgreSQL.
6. **Resultado:** A sessão é encerrada pelo tempo limite e o frontend exibe a contabilização final através do endpoint de resultados.

## Endpoints da API (v1)

A API utiliza a estratégia de versionamento via URI Path (`/api/v1`).

| Método | Endpoint | Descrição | Payload Exemplo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/pautas` | Cadastro de nova pauta | `{"descricao": "..."}` |
| `GET` | `/api/v1/pautas` | Listagem de todas as pautas | - |
| `GET` | `/api/v1/pautas/{id}` | Busca de detalhes de uma pauta | - |
| `POST` | `/api/v1/pautas/{id}/sessao` | Abertura de sessão de votação | `{"duracaoSegundos": 60}` |
| `POST` | `/api/v1/pautas/{id}/votos` | Recepção assíncrona de voto | `{"associadoId": "123...", "escolha": "SIM"}` |
| `GET` | `/api/v1/pautas/{id}/resultado` | Contabilização e status final | - |

## Instruções de Execução

A aplicação está contêinerizada para facilitar a execução orquestrada dos serviços de banco de dados, mensageria, backend e frontend.

### Via Docker Compose (Recomendado)

1. Clone o repositório:
```bash
git clone <url-do-repositorio>
cd <nome-do-diretorio>
```

2. Suba a infraestrutura completa:
```bash
docker-compose up -d --build
```

3. Acesse a aplicação:
* **Frontend:** http://localhost:5173
* **Backend (API):** http://localhost:8080/api/v1

*Nota: Para encerrar a execução e remover os containers, utilize o comando `docker-compose down`.*

### Execução Local (Ambiente de Desenvolvimento)

Caso seja necessário rodar os serviços da aplicação isoladamente:

**1. Inicializar Infraestrutura Base:**
```bash
docker-compose up -d postgres kafka
```

**2. Executar o Backend:**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**3. Executar o Frontend:**
```bash
cd frontend
npm install
npm run dev
```