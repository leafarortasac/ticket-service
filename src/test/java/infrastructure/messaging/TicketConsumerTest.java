package infrastructure.messaging;

import com.br.ticket_service.domain.entity.Category;
import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.enums.TicketPriority;
import com.br.ticket_service.domain.event.TicketCreatedEvent;
import com.br.ticket_service.domain.repository.ApprovalRequestRepository;
import com.br.ticket_service.domain.repository.TicketRepository;
import com.br.ticket_service.infrastructure.messaging.TicketConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketConsumerTest {

    @InjectMocks
    private TicketConsumer ticketConsumer;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;

    @Test
    @DisplayName("Deve criar um ApprovalRequest quando o ticket for CRITICAL")
    void shouldCreateApprovalRequestForCriticalTicket() {

        UUID ticketId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        TicketCreatedEvent event = new TicketCreatedEvent();
        event.setTicketId(ticketId);
        event.setTenantId(tenantId);
        event.setPriority(TicketPriority.CRITICAL.name());

        Ticket ticket = new Ticket();
        ticket.setProtocol("TKT-123");
        ticket.setPriority(TicketPriority.CRITICAL);
        ticket.setTenantId(tenantId);
        ticket.setCreatedAt(LocalDateTime.now());

        Category category = new Category();
        category.setSlaHours(24);
        ticket.setCategory(category);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        ticketConsumer.handleTicketCreated(event);

        verify(approvalRequestRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Não deve criar ApprovalRequest para tickets de prioridade LOW")
    void shouldNotCreateApprovalRequestForLowPriority() {

        UUID ticketId = UUID.randomUUID();
        TicketCreatedEvent event = new TicketCreatedEvent();
        event.setTicketId(ticketId);
        event.setPriority(TicketPriority.LOW.name());
        event.setTenantId(UUID.randomUUID());

        Ticket ticket = new Ticket();
        ticket.setPriority(TicketPriority.LOW);
        ticket.setCreatedAt(LocalDateTime.now());

        Category category = new Category();
        category.setSlaHours(48);
        ticket.setCategory(category);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        ticketConsumer.handleTicketCreated(event);

        verify(approvalRequestRepository, never()).save(any());
    }
}