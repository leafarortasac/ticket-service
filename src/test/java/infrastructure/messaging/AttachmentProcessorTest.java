package infrastructure.messaging;

import com.br.ticket_service.application.service.AttachmentProcessor;
import com.br.ticket_service.domain.entity.Attachment;
import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.repository.AttachmentRepository;
import com.br.ticket_service.domain.repository.TicketRepository;
import com.br.ticket_service.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentProcessorTest {

    @InjectMocks
    private AttachmentProcessor attachmentProcessor;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Test
    @DisplayName("Deve processar upload, salvar ficheiro e persistir no banco")
    void shouldProcessUploadAndPersistMetadata() throws IOException {
        // Arrange
        UUID ticketId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        byte[] content = "conteudo do teste".getBytes();
        String fileName = "documento.pdf";

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setTenantId(tenantId);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Mocking static Files para não tocar no disco real
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
            mockedFiles.when(() -> Files.write(any(Path.class), any(byte[].class))).thenReturn(null);

            // Act
            assertDoesNotThrow(() ->
                    attachmentProcessor.processUploadAsync(content, fileName, "application/pdf", ticketId, tenantId)
            );

            // Assert
            verify(attachmentRepository, times(1)).save(any(Attachment.class));
            mockedFiles.verify(() -> Files.write(any(Path.class), eq(content)), times(1));
        }
    }

    @Test
    @DisplayName("Deve lançar BusinessException e não salvar no banco se houver falha de IO")
    void shouldHandleIOExceptionGracefully() throws IOException {
        // Arrange
        UUID ticketId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        // REMOVI o when(ticketRepository...) daqui porque o Mockito reclamou que não era usado

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("Disk Full"));

            // Act & Assert
            assertThrows(BusinessException.class, () ->
                    attachmentProcessor.processUploadAsync(new byte[0], "erro.txt", "text/plain", ticketId, tenantId)
            );

            verify(attachmentRepository, never()).save(any());
        }
    }
}