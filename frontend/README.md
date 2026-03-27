# Frontend - Sistema de Votacao

Interface web para gerenciamento de pautas e votacao em assembleias cooperativas.

Este projeto consome a API versionada em `/api/v1`, com foco em um fluxo claro de ponta a ponta:

1. Criacao da pauta.
2. Definicao de duracao da sessao (horas e minutos).
3. Abertura da sessao.
4. Votacao por associado (CPF + escolha SIM/NAO).
5. Exibicao de resultado ao encerrar.

## Diferenciais Tecnicos

* **Fluxo guiado por estado de sessao:** `Pauta criada`, `Sessao aberta` e `Sessao encerrada` com regras de habilitacao de tela.
* **UX defensiva:** bloqueio do formulario de voto enquanto a sessao nao estiver aberta.
* **Compatibilidade com CORS em dev:** proxy no Vite para `/api` evitando preflight direto no navegador.
* **Roteamento modular:** `Layout` compartilhado com paginas filhas via `react-router-dom`.
* **Estado de sessao local:** sincronizacao auxiliar em `localStorage` para refletir o ciclo da pauta no frontend.

## Stack Tecnologica

* **Framework UI:** React 19
* **Build Tool:** Vite 5
* **Roteamento:** React Router DOM 7
* **HTTP Client:** Axios
* **Estilos:** Tailwind CSS

## Estrutura do Projeto

```text
frontend/
  public/
	favicon.svg
	icons.svg
  src/
	components/
	  Layout.jsx
	pages/
	  Dashboard.jsx
	  DetalhePauta.jsx
	  NovaPauta.jsx
	services/
	  api.js
	  sessionState.js
	App.jsx
	index.css
	main.jsx
  Dockerfile
  package.json
  tailwind.config.js
  vite.config.js
```

## Arquitetura Funcional

### 1) Dashboard (`/`)

* Lista pautas ordenadas por data (mais recentes primeiro).
* Exibe status visual da pauta:
  * `Pauta criada`
  * `Aberta`
  * `Encerrada` (com resultado quando disponivel)
* Acesso rapido para:
  * abrir votacao (`Abrir Votacao`)
  * votar (`Votar Agora`)
  * ver detalhes (`Ver Detalhes`)

### 2) Nova Pauta (`/pautas/nova`)

* Cadastra uma nova pauta via API.
* Ao criar com sucesso:
  * registra estado local inicial como `CRIADA`
  * redireciona para `/pautas/:id` para configuracao da sessao.

### 3) Detalhe da Pauta (`/pautas/:id`)

* Permite definir duracao da sessao em **horas + minutos**.
* Converte para `duracaoSegundos` no envio ao backend.
* So libera CPF e botoes de voto quando a sessao estiver aberta.
* Quando encerrada, mostra totais `SIM` e `NAO` se houver votos.

### 4) Pautas Encerradas (`/pautas/encerradas`)

* Reuso do `Dashboard` com filtro para exibir apenas pautas encerradas.

## Rotas Frontend

| Rota | Pagina | Objetivo |
| :--- | :--- | :--- |
| `/` | Dashboard | Listagem e status das pautas |
| `/pautas/nova` | NovaPauta | Criacao de pauta |
| `/pautas/:id` | DetalhePauta | Abrir sessao, votar e ver resultado |
| `/pautas/encerradas` | Dashboard (filtro) | Listar apenas pautas encerradas |

## Integracao com API

Base utilizada no frontend:

* `VITE_API_BASE_URL` (quando definida)
* fallback: `/api/v1`

Arquivo: `src/services/api.js`

Endpoints consumidos:

| Metodo | Endpoint | Uso no frontend |
| :--- | :--- | :--- |
| `POST` | `/pautas` | Criar nova pauta |
| `GET` | `/pautas` | Listar pautas |
| `GET` | `/pautas/{id}` | Buscar detalhes da pauta |
| `POST` | `/pautas/{id}/sessao` | Abrir sessao de votacao |
| `POST` | `/pautas/{id}/votos` | Registrar voto do associado |
| `GET` | `/pautas/{id}/resultado` | Exibir resultado final |

### Payloads relevantes

**Abrir sessao**

```json
{
  "duracaoSegundos": 5400
}
```

**Votar**

```json
{
  "associadoId": "52998224725",
  "escolha": "SIM"
}
```

## Configuracao de Ambiente

Opcionalmente, crie um `.env`:

```bash
VITE_API_BASE_URL=/api/v1
```

Para apontar para URL absoluta (sem proxy), por exemplo:

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## CORS e Proxy em Desenvolvimento

O Vite esta configurado para proxy de `/api` para `http://localhost:8080`.

Arquivo: `vite.config.js`

Isso permite rodar frontend em uma origem (ex.: `5173`/`5174`) sem depender de CORS para chamadas proxied.

## Scripts

```bash
npm run dev
npm run build
npm run preview
```

## Execucao Local

### 1) Instalar dependencias

```bash
npm install
```

### 2) Rodar em desenvolvimento

```bash
npm run dev
```

### 3) Gerar build de producao

```bash
npm run build
```

### 4) Servir build localmente

```bash
npm run preview
```

## Execucao com Docker

O projeto possui `Dockerfile` para ambiente de desenvolvimento com Vite exposto.

### Build da imagem

```bash
docker build -t votacao-frontend .
```

### Subir o container

```bash
docker run --rm -p 5173:5173 votacao-frontend
```

## Regras de Fluxo Implementadas

* Pauta criada nao pode receber voto.
* Sessao aberta habilita CPF e botoes de voto.
* Sessao encerrada bloqueia voto e exibe resultado quando disponivel.
* CPF com mascara visual no input e envio sanitizado (somente digitos).

## Observacoes Importantes

* O frontend utiliza sincronizacao local de sessao em `src/services/sessionState.js` para apoiar UX.
* A verdade final de negocio continua no backend.
* Em caso de divergencia (ex.: sessao ja aberta no backend), a interface tenta reconciliar o estado automaticamente.

## Proximos Passos Recomendados

* Adicionar endpoint dedicado de consulta de sessao no backend para eliminar heuristicas no frontend.
* Incluir testes E2E do fluxo completo (criacao -> abertura -> voto -> encerramento).
* Padronizar feedback de erro por status HTTP para mensagens de UX ainda mais claras.
