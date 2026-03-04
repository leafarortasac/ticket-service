package com.br.ticket_service.application.service;

import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.entity.TicketMessage;
import com.br.ticket_service.domain.repository.TicketMessageRepository;
import com.br.ticket_service.domain.repository.TicketRepository;
import com.br.ticket_service.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class TicketMessageService {

    private final TicketMessageRepository messageRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public void addComment(UUID ticketId, String content, boolean internal) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado"));

        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .userId(getAuthenticatedUserId())
                .message(content)
                .internalOnly(internal)
                .build();

        message.setTenantId(ticket.getTenantId());
        messageRepository.save(message);

        log.info("[TicketLog] Nova mensagem adicionada ao ticket: {}", ticket.getProtocol());
    }

    @Transactional(readOnly = true)
    public List<TicketMessage> getHistory(UUID ticketId) {
        return messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    private UUID getAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(auth.getPrincipal().toString());
    }
}