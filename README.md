Ticket Service (Core System) 🎫🚀

O Ticket Service é o coração do Sistema de Gestão de Chamados. Ele foi desenvolvido com foco em Multi-tenancy, garantindo que múltiplas empresas (tenants) utilizem a mesma infraestrutura com isolamento total de dados. O serviço gerencia o ciclo de vida dos chamados, fluxos de aprovação para tickets críticos e armazenamento de anexos.

🎯 Responsabilidades

Isolamento Multi-tenant: Filtro automático de dados por tenant_id via Hibernate Filters e ThreadLocal.

Gestão de Tickets: Workflow completo de estados (OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED).

Fluxo de Aprovação: Identificação automática de tickets de prioridade CRITICAL para aprovação gerencial.

Armazenamento de Anexos: Processamento assíncrono de arquivos (PDF, JPEG, PNG) com persistência em disco organizada por tenant.

Mensageria: Comunicação via RabbitMQ para processamento de SLAs e notificações.

🔐 Segurança e Autenticação

O serviço utiliza Spring Security 6.x com validação de Tokens JWT.

RBAC (Role Based Access Control): Permissões distintas para CUSTOMER, AGENT, MANAGER e ADMIN.

Contexto de Tenant: O tenant_id é extraído do token e propagado via TenantContext para todas as camadas.

Header Requerido: Authorization: Bearer <TOKEN_JWT>

🛠️ Tecnologias

Java 21 (com suporte a Virtual Threads)

Spring Boot 3.4.x

Spring Data JPA & Flyway: Migrations automáticas para o PostgreSQL 15.

RabbitMQ: Processamento assíncrono de regras de negócio.

JUnit 5 & Mockito: Cobertura de testes > 70%.

Shared Contracts: Integração com biblioteca de modelos compartilhados.

📡 Documentação de API (Swagger)

A documentação interativa com todos os endpoints, modelos e schemas pode ser acessada em:

🔗 http://localhost:8081/swagger-ui.html

   Endpoints Principais

   POST /api/v1/tickets: Abertura de novos chamados.
   
   PATCH /api/v1/tickets/{id}: Transições de status (com validação de regras de negócio).
   
   POST /api/v1/tickets/{id}/attachments: Upload assíncrono de documentos (Máx 10MB).
   
   GET /api/v1/approvals: Listagem de solicitações pendentes para gerentes.

📦 Execução e Setup

1. Pré-requisitos
   Certifique-se de ter o Maven, Docker e Docker Compose instalados.

2. Instalação de Contratos Compartilhados
   Como o projeto utiliza uma biblioteca de modelos comum, instale-a primeiro:

   Bash
   cd shared-contracts
   mvn clean install -DskipTests

3. Subindo o Ambiente (Docker)

   A aplicação está configurada para subir todo o ecossistema (PostgreSQL, RabbitMQ e App) com um único comando:

Bash

docker-compose up --build

As migrations do banco de dados serão executadas automaticamente pelo Flyway.

🧪 Testes e Qualidade

Para validar a cobertura de testes e as regras de negócio:

Bash
mvn test

🚀 Guia de Validação (Cenários de Teste)

Para facilitar a avaliação das regras de negócio exigidas, seguem os principais fluxos de teste:

1. Teste de Isolamento Total (Multi-tenancy)
   
   Objetivo: Garantir que os dados de uma empresa não "vazam" para outra.

   Ação: Utilize um Token JWT vinculado ao Tenant B e tente listar os tickets através do endpoint GET /api/v1/tickets.

   Resultado Esperado: A lista deve retornar vazia ou conter apenas tickets criados para o Tenant B. Tickets de outros Tenants nunca devem ser retornados, mesmo que o ID seja conhecido.

2. Workflow de Aprovação (Gatilho de Prioridade)
   
   Objetivo: Validar o escalonamento automático de chamados críticos.

   Ação: Crie um novo ticket via POST /api/v1/tickets definindo o campo priority como CRITICAL.

   Resultado Esperado: 1.  O sistema deve processar o evento de forma assíncrona.
   
   Verifique a tabela approval_requests: um novo registro deve ter sido criado automaticamente vinculado a este ticket.
   
   Log: O TicketConsumer deve registar a identificação de criticidade e o disparo da lógica de aprovação.

3. Regras de Transição de Status (Business Exception)
   
   Objetivo: Validar a consistência do workflow de estados (Pág. 7 do PDF).

   Ação: Tente atualizar o status de um ticket que está OPEN diretamente para CLOSED via PATCH /api/v1/tickets/{id}.

   Resultado Esperado: O sistema deve impedir a operação e retornar um erro 400 Bad Request ou 422 Unprocessable Entity com um JSON estruturado, informando que a transição é inválida (ex: o ticket deve passar por IN_PROGRESS antes de ser fechado).
   
4. Upload Assíncrono de Anexos
   
   Objetivo: Validar o processamento de binários e persistência em disco.

   Ação: Envie um arquivo (PDF, JPG ou PNG) através do endpoint POST /api/v1/tickets/{id}/attachments.

   Resultado Esperado: 1.  Retorno imediato 202 Accepted. 

   O arquivo deve ser armazenado fisicamente na pasta ./uploads/{tenantId}/.

   Os metadados (nome, tipo, tamanho) devem ser persistidos na tabela attachments.