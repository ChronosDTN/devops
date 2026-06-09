# Chronos DTN — DevOps com Docker Compose

> Módulo de infraestrutura e orquestração de containers do projeto **Chronos DTN**, desenvolvido para a Global Solution FIAP 2026.
> A solução utiliza **Docker Compose** para executar a API Java Spring Boot e o banco de dados Oracle XE em containers integrados, com rede isolada, volume persistente e usuário não-root na aplicação.

---

## 🛰️ Sobre o Projeto

O **Chronos DTN** é uma solução inspirada em cenários de comunicação Terra-Lua, simulando o gerenciamento de transações e pacotes DTN, ou seja, dados que podem ser armazenados temporariamente em caso de falha de comunicação e sincronizados posteriormente.

Neste módulo de DevOps, o foco é demonstrar a infraestrutura conteinerizada da aplicação, garantindo que a API e o banco de dados sejam executados de forma isolada, reproduzível e persistente.

---

## 🎯 Objetivo do Módulo DevOps

Este módulo tem como objetivo atender aos requisitos da disciplina **DevOps Tools & Cloud Computing**, demonstrando:

* API Java Spring Boot conteinerizada;
* Banco de dados Oracle XE em container;
* Execução via Docker Compose;
* Rede Docker dedicada entre API e banco;
* Volume nomeado para persistência dos dados;
* Variáveis de ambiente para configuração da aplicação;
* Porta exposta para acesso externo;
* Usuário não-root no container da aplicação;
* Logs dos containers;
* Evidência de persistência com `SELECT` direto no banco.

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia         | Versão/Função                      |
| ------------------ | ---------------------------------- |
| Docker Desktop     | Execução dos containers            |
| Docker Compose     | Orquestração multi-container       |
| Java               | 21                                 |
| Spring Boot        | 3.2.5                              |
| Oracle Database XE | 21c slim                           |
| Maven              | Build da aplicação                 |
| Swagger/OpenAPI    | Teste e documentação dos endpoints |

---

## 📂 Estrutura do Projeto

```bash
devops/
├── src/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
└── .gitignore
```

---

## 🧱 Arquitetura dos Containers

A solução possui dois containers principais:

| Serviço            | Container              | Porta  | Função                      |
| ------------------ | ---------------------- | ------ | --------------------------- |
| API Spring Boot    | `chronos-api-rm562744` | `8080` | Backend Java do Chronos DTN |
| Oracle Database XE | `chronos-db-rm562744`  | `1521` | Banco de dados relacional   |

Os containers se comunicam por meio da rede Docker:

```txt
dtn-mesh-net
```

O banco utiliza o volume nomeado:

```txt
oracle-data-volume
```

Esse volume garante que os dados persistam mesmo se o container for parado ou recriado.

---

## 🔐 Segurança do Container da API

A aplicação não roda como usuário root.

O `Dockerfile` cria e utiliza o usuário:

```txt
dtn_user
```

E o diretório de trabalho da aplicação é:

```txt
/app
```

Evidência realizada no container:

```bash
docker exec -it chronos-api-rm562744 sh
whoami
pwd
ls -l
```

Resultado esperado:

```txt
dtn_user
/app
app.jar
```

---

## ▶️ Como Executar o Projeto

### 1. Clonar o repositório

```bash
git clone <LINK_DO_REPOSITORIO>
cd devops
```

### 2. Subir os containers

```bash
docker compose up -d --build
```

Na primeira execução, o Oracle XE pode demorar alguns minutos para inicializar completamente.

### 3. Verificar os containers em execução

```bash
docker ps
```

Devem aparecer os containers:

```txt
chronos-db-rm562744
chronos-api-rm562744
```

### 4. Verificar logs do banco

```bash
docker logs chronos-db-rm562744
```

### 5. Verificar logs da API

```bash
docker logs chronos-api-rm562744
```

---

## 🌐 Acessar a API

A API fica disponível em:

```txt
http://localhost:8080
```

O Swagger pode ser acessado em:

```txt
http://localhost:8080/swagger-ui/index.html
```

---

## 🔑 Autenticação

A API possui endpoint para geração de token JWT:

```txt
POST /api/auth/token
```

Credenciais de teste:

```json
{
  "username": "operador",
  "password": "Chronos2026!"
}
```

O retorno inclui um token do tipo Bearer.

---

## 📡 Endpoints Principais

### Gerar token JWT

```txt
POST /api/auth/token
```

### Sincronizar transação DTN

```txt
POST /api/transações/sincronizar
```

Exemplo de corpo da requisição:

```json
{
  "sourceNode": 101,
  "targetNode": 202,
  "payload": "PACOTE_DTN_TESTE_TERRA_LUA",
  "localTimestamp": "2026-06-08T23:58:40.280Z"
}
```

Resposta esperada:

```txt
201 — Transação sincronizada com sucesso
```

### Listar transações

```txt
GET /api/transações
```

### Buscar transação por ID

```txt
GET /api/transações/{id}
```

### Atualizar status da transação

```txt
PATCH /api/transações/{id}/status
```

### Excluir transação

```txt
DELETE /api/transações/{id}
```

---

## 🗄️ Banco de Dados

O banco Oracle XE roda no container:

```txt
chronos-db-rm562744
```

Para acessar o banco:

```bash
docker exec -it chronos-db-rm562744 sqlplus chronos/ChronosSecurePassword2026@XEPDB1
```

### Tabelas criadas

```sql
SELECT table_name FROM user_tables;
```

Tabelas principais:

```txt
T_CDTN_NODE_ROUTE
T_CDTN_TRANSACTION_BUFFER
```

### Consultar transações persistidas

```sql
SET LINESIZE 200;
SET PAGESIZE 50;

SELECT id_tx, source_node, target_node, payload, sync_status
FROM T_CDTN_TRANSACTION_BUFFER;
```

Exemplo de evidência obtida:

```txt
ID_TX: 5
SOURCE_NODE: 101
TARGET_NODE: 202
PAYLOAD: PACOTE_DTN_TESTE_TERRA_LUA
SYNC_STATUS: PENDING
```

---

## ⚙️ Procedure PL/SQL

Para a sincronização da transação, foi criada a procedure:

```sql
SP_CORRIGIR_TEMPO_LUNAR
```

Exemplo simplificado utilizado no ambiente Docker:

```sql
CREATE OR REPLACE PROCEDURE SP_CORRIGIR_TEMPO_LUNAR (
    p_id_tx IN NUMBER,
    p_status OUT VARCHAR2
)
AS
BEGIN
    p_status := 'OK';
END;
/
```

---

## 🧪 Evidências de Funcionamento

Foram validados os seguintes pontos:

* API Spring Boot executando em container;
* Banco Oracle XE executando em container;
* Comunicação entre API e banco pela rede Docker;
* Swagger acessível em `localhost:8080`;
* Endpoint `POST /api/transações/sincronizar` retornando `201`;
* Registro persistido na tabela `T_CDTN_TRANSACTION_BUFFER`;
* Consulta `SELECT` executada diretamente no container do banco;
* Aplicação rodando com usuário não-root `dtn_user`;
* Diretório de execução da aplicação definido como `/app`;
* Volume nomeado configurado para persistência dos dados.

---

## 🧯 Comandos Úteis

### Parar os containers

```bash
docker compose down
```

### Parar e apagar volumes

```bash
docker compose down -v
```

Use este comando com cuidado, pois ele apaga os dados persistidos no volume do Oracle.

### Rebuild da aplicação

```bash
docker compose up -d --build
```

### Reiniciar apenas a API

```bash
docker restart chronos-api-rm562744
```

### Reiniciar apenas o banco

```bash
docker restart chronos-db-rm562744
```

---

## 🖼️ Arquitetura Macro

A arquitetura macro da solução é composta por:

```txt
Usuário / Swagger / Postman
        |
        | HTTP - Porta 8080
        v
Container API Spring Boot
chronos-api-rm562744
        |
        | Rede Docker: dtn-mesh-net
        v
Container Oracle XE
chronos-db-rm562744
        |
        v
Volume Persistente
oracle-data-volume
```

---

## 🎥 Vídeo de Demonstração

O vídeo de demonstração deve apresentar:

1. Repositório no GitHub;
2. Execução do `docker compose up -d --build`;
3. Verificação com `docker ps`;
4. Logs da API e do banco;
5. Swagger aberto no navegador;
6. Criação de uma transação com retorno `201`;
7. Consulta da transação pelo Swagger;
8. Acesso ao banco via `docker exec`;
9. `SELECT` na tabela `T_CDTN_TRANSACTION_BUFFER`;
10. Acesso ao container da API;
11. Comandos `whoami`, `pwd` e `ls -l`.

Link do vídeo: **INSERIR AQUI**

---

## 👥 Integrantes

| Nome             | RM       | Turma  |
| ---------------- | -------- | ------ |
| Evellyn Ferreira | RM562744 | 2TDSPW |
| Maicon Douglas   | RM561279 | 2TDSPW |
| Fernando Charles | RM566482 | 2TDSPW |

---

## 📌 Observações

Este projeto foi desenvolvido para fins acadêmicos, simulando uma arquitetura de backend e banco de dados para uma solução de comunicação e sincronização de transações DTN em contexto de economia espacial.

Em ambiente produtivo, recomenda-se substituir senhas fixas por Docker Secrets, Vault ou variáveis protegidas em pipeline CI/CD.
