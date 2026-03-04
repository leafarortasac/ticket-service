package com.br.ticket_service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum TicketStatus {
    OPEN("Aberto", Set.of("IN_PROGRESS", "CANCELLED")),
    IN_PROGRESS("Em Atendimento", Set.of("RESOLVED", "CANCELLED")),
    RESOLVED("Resolvido", Set.of("CLOSED", "IN_PROGRESS")),
    CLOSED("Finalizado", Set.of()),
    CANCELLED("Cancelado", Set.of());

    private final String description;
    private final Set<String> nextAllowedStatuses;

    public boolean canTransitionTo(TicketStatus nextStatus) {
        return this.nextAllowedStatuses.contains(nextStatus.name());
    }
}