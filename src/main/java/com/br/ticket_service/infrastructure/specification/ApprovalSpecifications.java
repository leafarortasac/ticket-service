package com.br.ticket_service.infrastructure.specification;

import com.br.ticket_service.domain.entity.ApprovalRequest;
import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.enums.ApprovalStatus;
import com.br.ticket_service.domain.enums.TicketPriority;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ApprovalSpecifications {

    public static Specification<ApprovalRequest> hasStatus(ApprovalStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<ApprovalRequest> hasTicketPriority(TicketPriority priority) {
        return (root, query, cb) -> {
            if (priority == null) return null;
            Join<ApprovalRequest, Ticket> ticketJoin = root.join("ticket");
            return cb.equal(ticketJoin.get("priority"), priority);
        };
    }

    public static Specification<ApprovalRequest> hasTicketProtocol(String protocol) {
        return (root, query, cb) -> {
            if (protocol == null || protocol.isBlank()) return null;
            Join<ApprovalRequest, Ticket> ticketJoin = root.join("ticket");
            return cb.equal(cb.upper(ticketJoin.get("protocol")), protocol.toUpperCase());
        };
    }

    public static Specification<ApprovalRequest> hasTenantId(UUID tenantId) {
        return (root, query, cb) -> tenantId == null ? null : cb.equal(root.get("tenantId"), tenantId);
    }
}