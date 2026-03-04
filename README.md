Ticket Service (Core System) 🎫🚀

O Ticket Service é o motor de gestão de chamados da plataforma. Desenvolvido com foco em Multi-tenancy, ele garante o isolamento total de dados entre diferentes empresas utilizando a mesma infraestrutura. O serviço gerencia o ciclo de vida dos chamados, fluxos de aprovação automática e armazenamento assíncrono de anexos.

🎯 Responsabilidades

Isolamento Multi-tenant: Filtro automático de dados via Hibernate Filters e propagação de contexto por ThreadLocal.

Gestão de Tickets: Workflow de estados robusto (OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED).

Fluxo de Aprovação: Gatilho automático para tickets CRITICAL, gerando pendências para o perfil MANAGER.

Armazenamento de Anexos: Upload assíncrono com persistência física em disco organizada por Tenant.

Mensageria: Comunicação via RabbitMQ para auditoria de eventos e processamento de SLAs.

🔐 Segurança e Autenticação

Spring Security 6.x: Validação de Tokens JWT.

RBAC: Controle de acesso baseado em Roles (CUSTOMER, AGENT, MANAGER).

Contexto de Tenant: O tenant_id é extraído do JWT e injetado no TenantContext em cada requisição.

🛠️ Tecnologias

Java 21: Uso de Virtual Threads para otimização de processamento I/O.

Spring Boot 3.4.x: Base da aplicação.

PostgreSQL 15 & Flyway: Persistência e versionamento de banco de dados.

RabbitMQ: Broker de mensageria.

JUnit 5 & Mockito: Cobertura de testes superior a 70%.

📡 Documentação de API (Swagger)

Acesse a documentação interativa em: 🔗 http://localhost:8081/swagger-ui.html

📦 Execução e Setup

1. Pré-requisitos

Certifique-se de ter o Maven e o Docker Desktop instalados.

2. Instalação de Contratos Compartilhados

Este serviço depende da biblioteca de modelos comuns:

Bash
cd shared-contracts
mvn clean install -DskipTests

3. Subindo o Ambiente (Docker)

Na pasta raiz do projeto principal:

Bash
docker-compose up -d --build

As tabelas e filas serão criadas automaticamente na subida dos containers.

🚀 Guia de Validação (Passo a Passo)

Para validar a solução conforme as regras de negócio, siga esta ordem lógica:

1. Configuração de Domínio (Pré-requisito)

Antes de abrir um chamado, é necessário que o Tenant possua categorias cadastradas (ex: Software, Hardware).

Ação: Realize um POST /api/v1/categories criando uma categoria.

Resultado: Você receberá um UUID da categoria. Guarde-o para o próximo passo.

2. Abertura de Ticket e Teste de Isolamento

Ação: Com o ID da categoria, faça um POST /api/v1/tickets. Em seguida, tente listar os tickets (GET) usando um Token de um Tenant B.

Resultado Esperado: O Tenant B não deve visualizar os tickets do Tenant A, garantindo o isolamento total.

3. Workflow de Aprovação (Gatilho de Prioridade)

Ação: Crie um ticket com prioridade CRITICAL.

Resultado Esperado: O sistema disparará um evento assíncrono. Verifique no banco (ou via API de aprovações) que um registro foi criado na tabela approval_requests.

4. Regras de Transição (Business Exception)

Ação: Tente mudar o status de um ticket de OPEN diretamente para CLOSED.

Resultado Esperado: O sistema deve lançar uma Business Exception (400/422), exigindo que o ticket passe por IN_PROGRESS primeiro.

5. Upload Assíncrono de Anexos

Ação: Envie um arquivo via POST /api/v1/tickets/{id}/attachments.

Resultado Esperado: Retorno 202 Accepted. O arquivo deve aparecer na pasta física ./uploads/{tenantId}/ e os metadados no banco de dados.
