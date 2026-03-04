package com.br.ticket_service.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApprovalStatus {
    PENDING("Aguardando decisão do gestor"),
    APPROVED("Aprovado pelo gestor"),
    REJECTED("Rejeitado pelo gestor");

    private final String description;
}