package com.br.ticket_service.application.service;

import com.br.ticket_service.domain.entity.Attachment;
import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.repository.AttachmentRepository;
import com.br.ticket_service.domain.repository.TicketRepository;
import com.br.ticket_service.infrastructure.exception.BusinessException;
import com.br.ticket_service.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("application/pdf", "image/jpeg", "image/png");

    @Transactional
    public void upload(UUID ticketId, MultipartFile file) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado"));

        validateFile(file);

        try {

            Path tenantPath = Paths.get(uploadDir, ticket.getTenantId().toString());
            if (!Files.exists(tenantPath)) {
                Files.createDirectories(tenantPath);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = tenantPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath);

            Attachment attachment = Attachment.builder()
                    .ticket(ticket)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .storagePath(filePath.toString())
                    .build();
            attachment.setTenantId(ticket.getTenantId());

            attachmentRepository.save(attachment);
            log.info("[Attachment] Ficheiro {} salvo para o ticket {}", fileName, ticket.getProtocol());

        } catch (IOException e) {
            log.error("[Attachment] Erro ao salvar ficheiro no disco", e);
            throw new BusinessException("Não foi possível processar o upload do ficheiro.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("O ficheiro está vazio.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("O ficheiro excede o limite de 10MB.");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException("Tipo de ficheiro não permitido. Apenas PDF, JPEG e PNG.");
        }
    }
}