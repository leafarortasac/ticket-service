package application.service;

import com.br.shared.contracts.model.TicketStatusRepresentation;
import com.br.shared.contracts.model.UpdateTicketStatusRequestRepresentation;
import com.br.ticket_service.application.mapper.TicketMapper;
import com.br.ticket_service.application.service.TicketService;
import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.enums.TicketStatus;
import com.br.ticket_service.domain.repository.CategoryRepository;
import com.br.ticket_service.domain.repository.TicketRepository;
import com.br.ticket_service.infrastructure.exception.BusinessException;
import com.br.ticket_service.infrastructure.exception.ResourceNotFoundException;
import com.br.ticket_service.infrastructure.messaging.TicketProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private TicketProducer ticketProducer;

    @Mock
    private CategoryRepository categoryRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve lançar exceção ao fechar ticket que ainda está aberto")
    void shouldThrowExceptionWhenClosingOpenTicket() {

        UUID ticketId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessException.class, () -> {
            ticketService.updateStatus(ticketId, new UpdateTicketStatusRequestRepresentation(TicketStatusRepresentation.CLOSED));
        });

        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir mudar de OPEN para IN_PROGRESS")
    void shouldAllowTransitionFromOpenToInProgress() {

        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        mockSecurityContext(userId);

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setProtocol("TKT-2026-001");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setTenantId(UUID.randomUUID());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        ticketService.updateStatus(ticketId, new UpdateTicketStatusRequestRepresentation(TicketStatusRepresentation.IN_PROGRESS));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar pular de IN_PROGRESS direto para CLOSED")
    void shouldThrowExceptionWhenSkippingResolvedStatus() {

        UUID ticketId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessException.class, () -> {
            ticketService.updateStatus(ticketId, new UpdateTicketStatusRequestRepresentation(TicketStatusRepresentation.CLOSED));
        });
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o ticket pertence a outro Tenant")
    void shouldThrowNotFoundWhenTicketBelongsToOtherTenant() {

        UUID ticketId = UUID.randomUUID();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.updateStatus(ticketId, new UpdateTicketStatusRequestRepresentation(TicketStatusRepresentation.IN_PROGRESS));
        });
    }

    private void mockSecurityContext(UUID userId) {

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getPrincipal()).thenReturn(userId.toString());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }
}