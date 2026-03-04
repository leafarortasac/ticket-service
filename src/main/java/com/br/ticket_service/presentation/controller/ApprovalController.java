package com.br.ticket_service.presentation.controller;

import com.br.shared.contracts.api.ApprovalsApi;
import com.br.shared.contracts.model.ApproveTicketRequestRepresentation;
import com.br.ticket_service.application.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ApprovalController implements ApprovalsApi {

    private final ApprovalService approvalService;

    @Override
    public ResponseEntity<Void> approveTicket(UUID id, ApproveTicketRequestRepresentation request) {
        approvalService.processApproval(id, request);
        return ResponseEntity.ok().build();
    }
}
