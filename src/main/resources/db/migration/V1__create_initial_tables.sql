-- Extensão para UUID (Necessário se for usar uuid_generate_v4)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Categorias
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    sla_hours INTEGER NOT NULL DEFAULT 24,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_category_name_tenant UNIQUE (name, tenant_id)
);

-- 2. Tickets
CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    requester_id UUID NOT NULL,
    assigned_agent_id UUID,
    category_id UUID NOT NULL,
    protocol VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_ticket_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT uk_protocol_tenant UNIQUE (protocol, tenant_id)
);

-- 3. Mensagens (Histórico)
CREATE TABLE ticket_messages (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    user_id UUID NOT NULL, -- Alterado de sender_id para bater com a Entity
    message TEXT NOT NULL,  -- Alterado de content para bater com a Entity
    internal_only BOOLEAN DEFAULT FALSE,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE
);

-- 4. Anexos
CREATE TABLE attachments (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path TEXT NOT NULL,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attachment_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE
);

-- 5. Solicitações de Aprovação
CREATE TABLE approval_requests (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    requester_id UUID,        -- ADICIONE ESTA LINHA SE NÃO TIVER
    approver_id UUID,         -- ADICIONE ESTA LINHA SE NÃO TIVER
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    justification TEXT,
    approver_notes TEXT,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_approval_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE
);

-- Índices
CREATE INDEX idx_categories_tenant ON categories(tenant_id);
CREATE INDEX idx_tickets_tenant ON tickets(tenant_id);
CREATE INDEX idx_tickets_protocol ON tickets(protocol);