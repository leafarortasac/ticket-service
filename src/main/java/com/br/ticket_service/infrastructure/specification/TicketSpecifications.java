package com.br.ticket_service.infrastructure.specification;

import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.enums.TicketPriority;
import com.br.ticket_service.domain.enums.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TicketSpecifications {

    public static Specification<Ticket> hasStatus(TicketStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> hasPriority(TicketPriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Ticket> hasCategoryName(String categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null || categoryName.isBlank()) return null;
            return cb.like(cb.lower(root.get("category").get("name")), "%" + categoryName.toLowerCase() + "%");
        };
    }

    public static Specification<Ticket> createdBetween(OffsetDateTime start, OffsetDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start != null && end == null) return cb.greaterThanOrEqualTo(root.get("createdAt"), start);
            if (start == null) return cb.lessThanOrEqualTo(root.get("createdAt"), end);
            return cb.between(root.get("createdAt"), start, end);
        };
    }

    public static Specification<Ticket> hasRequester(UUID requesterId) {
        return (root, query, cb) -> requesterId == null ? null : cb.equal(root.get("requesterId"), requesterId);
    }

    public static Specification<Ticket> hasAssignedAgent(UUID agentId) {
        return (root, query, cb) -> agentId == null ? null : cb.equal(root.get("assignedAgentId"), agentId);
    }
}