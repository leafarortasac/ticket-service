package com.br.ticket_service.application.service;

import com.br.shared.contracts.model.ApproveTicketRequestRepresentation;
import com.br.ticket_service.domain.entity.ApprovalRequest;
import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.enums.ApprovalStatus;
import com.br.ticket_service.domain.enums.TicketPriority;
import com.br.ticket_service.domain.enums.TicketStatus;
import com.br.ticket_service.domain.repository.ApprovalRepository;
import com.br.ticket_service.domain.repository.TicketRepository;
import com.br.ticket_service.infrastructure.exception.ResourceNotFoundException;
import com.br.ticket_service.infrastructure.specification.ApprovalSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public void processApproval(UUID approvalId, ApproveTicketRequestRepresentation request) {

        ApprovalRequest approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de aprovação não encontrado"));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new RuntimeException("Este pedido já foi processado");
        }

        boolean isApproved = Boolean.TRUE.equals(request.getApproved());
        approval.setStatus(isApproved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        approval.setApproverNotes(request.getJustification());

        Ticket ticket = approval.getTicket();
        if (isApproved) {
            log.info("[Approval] Ticket {} aprovado. Seguindo fluxo.", ticket.getProtocol());
        } else {
            log.warn("[Approval] Ticket {} rejeitado. Motivo: {}", ticket.getProtocol(), request.getJustification());
            ticket.setStatus(TicketStatus.CANCELLED);
        }
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> listApprovals(ApprovalStatus status, TicketPriority priority, Pageable pageable) {
        Specification<ApprovalRequest> spec = Specification
                .where(ApprovalSpecifications.hasStatus(status))
                .and(ApprovalSpecifications.hasTicketPriority(priority));

        return approvalRepository.findAll(spec, pageable);
    }
}