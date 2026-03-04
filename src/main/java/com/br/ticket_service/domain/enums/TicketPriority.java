package com.br.ticket_service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketPriority {
    LOW("Baixa prioridade - Sem impacto crítico"),
    MEDIUM("Média prioridade - Impacto parcial"),
    HIGH("Alta prioridade - Impacto significativo"),
    CRITICAL("Crítica - Operação interrompida (Requer Aprovação)");

    private final String description;
}