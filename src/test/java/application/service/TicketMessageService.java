package application.service;

import com.br.ticket_service.application.service.TicketMessageService;
import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.repository.TicketMessageRepository;
import com.br.ticket_service.domain.repository.TicketRepository;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketMessageServiceTest {

    @InjectMocks
    private TicketMessageService messageService;

    @Mock
    private TicketMessageRepository messageRepository;

    @Mock
    private TicketRepository ticketRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve adicionar comentário com sucesso vinculando tenant e usuário")
    void shouldAddCommentSuccessfully() {
        // Arrange
        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        mockSecurityContext(userId);

        Ticket ticket = new Ticket();
        ticket.setProtocol("TKT-LOG");
        ticket.setTenantId(tenantId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Act
        messageService.addComment(ticketId, "Novo comentário", true);

        // Assert
        verify(messageRepository, times(1)).save(argThat(msg ->
                msg.getMessage().equals("Novo comentário") &&
                        msg.getTenantId().equals(tenantId) &&
                        msg.getUserId().equals(userId) &&
                        msg.isInternalOnly()
        ));
    }

    private void mockSecurityContext(UUID userId) {
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(auth.getPrincipal()).thenReturn(userId.toString());
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }
}