package com.br.ticket_service.infrastructure.messaging;

import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.event.TicketCreatedEvent;
import com.br.ticket_service.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishCreatedEvent(Ticket ticket) {
        try {
            TicketCreatedEvent event = new TicketCreatedEvent(
                    ticket.getId(),
                    ticket.getTenantId(),
                    ticket.getProtocol(),
                    ticket.getPriority().name()
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TICKET_EXCHANGE,
                    RabbitMQConfig.TICKET_ROUTING_KEY,
                    event
            );
            log.debug("[Messaging] Evento de criação enviado para o Rabbit: {}", ticket.getProtocol());
        } catch (Exception e) {
            log.error("[Messaging] Falha ao enviar evento para o RabbitMQ: {}", ticket.getProtocol(), e);
        }
    }
}