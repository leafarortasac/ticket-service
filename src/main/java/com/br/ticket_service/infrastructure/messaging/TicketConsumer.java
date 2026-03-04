package com.br.ticket_service.infrastructure.messaging;

import com.br.ticket_service.domain.entity.ApprovalRequest;
import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.enums.ApprovalStatus;
import com.br.ticket_service.domain.enums.TicketPriority;
import com.br.ticket_service.domain.event.TicketCreatedEvent;
import com.br.ticket_service.domain.repository.ApprovalRequestRepository;
import com.br.ticket_service.domain.repository.TicketRepository;
import com.br.ticket_service.infrastructure.config.RabbitMQConfig;
import com.br.ticket_service.infrastructure.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketConsumer {

    private final TicketRepository ticketRepository;

    private final ApprovalRequestRepository approvalRequestRepository;

    @RabbitListener(queues = RabbitMQConfig.TICKET_CREATED_QUEUE)
    @Transactional
    public void handleTicketCreated(TicketCreatedEvent event) {
        log.info("[Consumer] Processando novo ticket assincronamente: {}", event.getProtocol());

        try {

            TenantContext.setCurrentTenant(event.getTenantId().toString());

            var ticket = ticketRepository.findById(event.getTicketId())
                    .orElseThrow(() -> new RuntimeException("Ticket não encontrado: " + event.getTicketId()));

            if (TicketPriority.CRITICAL.name().equals(event.getPriority())) {
                applyCriticalRules(ticket);
            }

            calculateAndSetSLA(ticket);

            log.info("[Consumer] Ticket {} processado com sucesso.", ticket.getProtocol());

        } catch (Exception e) {
            log.error("[Consumer] Erro ao processar ticket {}: {}", event.getProtocol(), e.getMessage());
            throw e;
        } finally {
            TenantContext.clear();
        }
    }

    private void applyCriticalRules(Ticket ticket) {
        log.warn("[Rule Engine] Ticket CRÍTICO detectado: {}. Criando solicitação de aprovação.", ticket.getProtocol());

        ApprovalRequest approval = new ApprovalRequest();
        approval.setTicket(ticket);
        approval.setTenantId(ticket.getTenantId());
        approval.setRequesterId(ticket.getRequesterId());

        approval.setJustification("Aprovação automática gerada por criticidade alta.");

        approvalRequestRepository.save(approval);

        log.info("[Rule Engine] Solicitação de aprovação gerada para o ticket: {}", ticket.getProtocol());
    }
    private void calculateAndSetSLA(Ticket ticket) {
        int hours = ticket.getCategory().getSlaHours();

        LocalDateTime deadline = ticket.getCreatedAt().plusHours(hours);

        log.info("[SLA] Prazo definido para {}: {}", ticket.getProtocol(), deadline);
    }
}