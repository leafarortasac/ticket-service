package com.br.ticket_service.domain.repository;

import com.br.ticket_service.domain.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByProtocolAndTenantId(String protocol, UUID tenantId);

    Page<Ticket> findByRequesterId(UUID requesterId, Pageable pageable);

    Page<Ticket> findByAssignedAgentId(UUID agentId, Pageable pageable);

    boolean existsByProtocol(String protocol);
}