package com.br.ticket_service.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketCreatedEvent {
    private UUID ticketId;
    private UUID tenantId;
    private String protocol;
    private String priority;
}