package application.service;

import com.br.shared.contracts.model.ApproveTicketRequestRepresentation;
import com.br.ticket_service.application.service.ApprovalService;
import com.br.ticket_service.domain.entity.ApprovalRequest;
import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.enums.ApprovalStatus;
import com.br.ticket_service.domain.enums.TicketStatus;
import com.br.ticket_service.domain.repository.ApprovalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @InjectMocks
    private ApprovalService approvalService;

    @Mock
    private ApprovalRepository approvalRepository;

    @Test
    @DisplayName("Deve aprovar um ticket com sucesso")
    void shouldApproveTicketSuccessfully() {
        UUID approvalId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setProtocol("TKT-123");

        ApprovalRequest approval = new ApprovalRequest();
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setTicket(ticket);

        ApproveTicketRequestRepresentation request = new ApproveTicketRequestRepresentation();
        request.setApproved(true);
        request.setJustification("Tudo ok");

        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(approval));

        approvalService.processApproval(approvalId, request);

        assertEquals(ApprovalStatus.APPROVED, approval.getStatus());
        assertEquals("Tudo ok", approval.getApproverNotes());
        assertNotEquals(TicketStatus.CANCELLED, ticket.getStatus());
    }

    @Test
    @DisplayName("Deve cancelar o ticket ao rejeitar a aprovação")
    void shouldCancelTicketWhenRejected() {
        UUID approvalId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ApprovalRequest approval = new ApprovalRequest();
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setTicket(ticket);

        ApproveTicketRequestRepresentation request = new ApproveTicketRequestRepresentation();
        request.setApproved(false);
        request.setJustification("Faltam dados");

        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(approval));

        approvalService.processApproval(approvalId, request);

        assertEquals(ApprovalStatus.REJECTED, approval.getStatus());
        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar processar pedido que não está pendente")
    void shouldThrowExceptionWhenNotPending() {
        UUID approvalId = UUID.randomUUID();
        ApprovalRequest approval = new ApprovalRequest();
        approval.setStatus(ApprovalStatus.APPROVED);

        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(approval));

        assertThrows(RuntimeException.class, () ->
                approvalService.processApproval(approvalId, new ApproveTicketRequestRepresentation())
        );
    }
}