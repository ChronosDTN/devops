# Chronos DTN — DevOps (Docker Compose + Dockerfile)

> Módulo de infraestrutura e orquestração de containers do gateway financeiro **Chronos DTN**. Contém o `docker-compose.yml` para subir o ecossistema completo (Oracle DB + API Spring Boot) e o `Dockerfile` seguro com usuário sem privilégios de root.

---

## 🛰️ Sobre o Módulo

Este repositório centraliza toda a camada de infraestrutura do **Chronos DTN**, permitindo inicializar o ecossistema completo do backend com um único comando. A configuração segue boas práticas de segurança de containers conforme as diretrizes da **OWASP Docker Security**.

### Serviços orquestrados

| Serviço | Container | Porta | Descrição |
|---|---|---|---|
| `db` | `rm93842-oracle-db` | `1521` | Oracle Database XE 21c (banco de dados principal) |
| `api` | `rm93842-spring-api` | `8080` | API Spring Boot 3 (backend principal) |

### Rede isolada

Os dois containers se comunicam exclusivamente pela rede bridge interna `dtn-mesh-net`, isolada do host externo para maior segurança.

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Versão | Função |
|---|---|---|
| Docker Engine | 24+ | Motor de execução de containers |
| Docker Compose | v2 | Orquestração multi-container |
| Oracle Database XE | 21c slim | Banco de dados relacional de produção |
| Spring Boot | 3.2.5 | API backend principal conteinerizada |

---

## 📂 Estrutura de Arquivos

```
devops/
├── docker-compose.yml   # Orquestração completa dos serviços DB e API
└── Dockerfile           # Imagem customizada da API Spring Boot (non-root)
```

---

## ▶️ Como Executar

### Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e em execução
- O JAR compilado do `backend-java/` disponível (ou o Dockerfile fará o build automaticamente)

### 1. Subir o ecossistema completo

```bash
docker compose up -d
```

Na **primeira execução**, o Oracle XE leva aproximadamente **2 a 3 minutos** para inicializar o banco interno. A API aguardará o banco estar íntegro (`service_healthy`) antes de subir.

### 2. Verificar o status dos containers

```bash
docker compose ps
```

### 3. Acompanhar os logs em tempo real

```bash
# Todos os serviços
docker compose logs -f

# Apenas o banco de dados
docker compose logs -f db

# Apenas a API
docker compose logs -f api
```

### 4. Testar a API

```bash
curl http://localhost:8080/api/nodes
```

### 5. Encerrar e remover os containers

```bash
# Encerra sem apagar os dados do volume Oracle
docker compose down

# Encerra e remove TODOS os dados (use com cautela)
docker compose down -v
```

---

## 🔒 Segurança

- A API é executada dentro do container com o usuário `dtn_user` (sem privilégios de root), seguindo as diretrizes **OWASP Docker Security Top 10**.
- A rede `dtn-mesh-net` isola a comunicação DB ↔ API sem expor o banco diretamente ao host.
- As credenciais do banco estão em variáveis de ambiente — em produção, use **Docker Secrets** ou um **Vault**.

---

## 🔗 Repositórios do Projeto Chronos DTN

| Módulo | Descrição |
|---|---|
| [backend-java](https://github.com/seu-usuario/chronos-backend-java) | API principal Spring Boot 3 + JWT |
| [backend-dotnet](https://github.com/seu-usuario/chronos-backend-dotnet) | API secundária .NET 8 + EF Core |
| [database](https://github.com/seu-usuario/chronos-database) | Scripts Oracle SQL e Procedure PL/SQL |
| [iot-esp32](https://github.com/seu-usuario/chronos-iot-esp32) | Firmware C++ Arduino para ESP32 |
| [mobile-app](https://github.com/seu-usuario/chronos-mobile-app) | App React Native com Expo Router |

---

## 👤 Autores

Projeto desenvolvido para a **Global Solution — FIAP 2026**.
