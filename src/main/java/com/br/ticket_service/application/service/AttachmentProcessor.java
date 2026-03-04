package com.br.ticket_service.application.service;

import com.br.ticket_service.domain.entity.Attachment;
import com.br.ticket_service.domain.repository.AttachmentRepository;
import com.br.ticket_service.domain.repository.TicketRepository;
import com.br.ticket_service.infrastructure.exception.BusinessException;
import com.br.ticket_service.infrastructure.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachmentProcessor {

    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;

    @Async
    @Transactional
    public void processUploadAsync(byte[] fileContent, String fileName, String contentType, UUID ticketId, UUID tenantId) {
        log.info("[Async] Processando anexo {} para o ticket {}", fileName, ticketId);

        try {
            Path uploadPath = Paths.get("./uploads", tenantId.toString());

            Files.createDirectories(uploadPath);

            String finalFileName = UUID.randomUUID() + "_" + fileName;
            Path targetFile = uploadPath.resolve(finalFileName);

            Files.write(targetFile, fileContent);

            String storagePath = targetFile.toString();

            TenantContext.setCurrentTenant(tenantId.toString());

            var ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket não encontrado no processo assíncrono"));

            var attachment = new Attachment();
            attachment.setTicket(ticket);
            attachment.setFileName(fileName);
            attachment.setContentType(contentType);
            attachment.setFileSize((long) fileContent.length);
            attachment.setStoragePath(storagePath);
            attachment.setTenantId(tenantId);

            attachmentRepository.save(attachment);

            log.info("[Async] Anexo salvo com sucesso: {}", storagePath);

        } catch (IOException e) {
            log.error("[Async] Erro crítico ao manipular arquivos: ", e);
            throw new BusinessException("Falha técnica ao salvar anexo.");
        }
    }
}