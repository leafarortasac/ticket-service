package com.br.ticket_service.application.service;

import com.br.shared.contracts.model.*;
import com.br.ticket_service.application.mapper.TicketMapper;
import com.br.ticket_service.domain.entity.Ticket;
import com.br.ticket_service.domain.entity.Category;
import com.br.ticket_service.domain.enums.TicketPriority;
import com.br.ticket_service.domain.enums.TicketStatus;
import com.br.ticket_service.domain.repository.TicketRepository;
import com.br.ticket_service.domain.repository.CategoryRepository;
import com.br.ticket_service.infrastructure.exception.BusinessException;
import com.br.ticket_service.infrastructure.messaging.TicketProducer;
import com.br.ticket_service.infrastructure.security.TenantContext;
import com.br.ticket_service.infrastructure.exception.ResourceNotFoundException;
import com.br.ticket_service.infrastructure.specification.TicketSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final TicketMapper ticketMapper;
    private final TicketProducer ticketProducer;
    private final AttachmentProcessor attachmentProcessor;

    @Transactional
    public List<TicketResponseRepresentation> createTickets(List<TicketRequestRepresentation> requests) {
        UUID tenantId = UUID.fromString(TenantContext.getCurrentTenant());
        UUID requesterId = getAuthenticatedUserId();

        List<Ticket> ticketsToSave = requests.stream().map(dto -> {
            Ticket entity = ticketMapper.toEntity(dto);

            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + dto.getCategoryId()));

            entity.setTenantId(tenantId);
            entity.setRequesterId(requesterId);
            entity.setCategory(category);
            entity.setProtocol(generateProtocol());
            entity.setStatus(TicketStatus.OPEN);

            return entity;
        }).toList();

        List<Ticket> savedTickets = ticketRepository.saveAllAndFlush(ticketsToSave); // Flush força a ida ao banco

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                savedTickets.forEach(ticketProducer::publishCreatedEvent);
            }
        });

        return ticketMapper.toResponseList(savedTickets);
    }

    @Transactional(readOnly = true)
    public TicketResponseRepresentation findById(UUID id) {
        return ticketRepository.findById(id)
                .map(ticketMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public TicketDocumentResponseRepresentation listAll(
            TicketStatusRepresentation statusReq,
            TicketPriorityRepresentation priorityReq,
            String categoryName,
            Integer limit, Integer page, String sortField, String sortDir, Boolean unPaged) {

        TicketStatus status = statusReq != null ? TicketStatus.valueOf(statusReq.getValue()) : null;
        TicketPriority priority = priorityReq != null ? TicketPriority.valueOf(priorityReq.getValue()) : null;

        Specification<Ticket> spec = Specification.where(null);
        if (status != null) spec = spec.and(TicketSpecifications.hasStatus(status));
        if (priority != null) spec = spec.and(TicketSpecifications.hasPriority(priority));
        if (categoryName != null) spec = spec.and(TicketSpecifications.hasCategoryName(categoryName));

        Pageable pageable = Boolean.TRUE.equals(unPaged)
                ? Pageable.unpaged()
                : PageRequest.of(page, limit, Sort.Direction.fromString(sortDir), sortField);

        Page<Ticket> ticketPage = ticketRepository.findAll(spec, pageable);

        return ticketMapper.toDocumentResponse(ticketPage);
    }

    @Transactional
    public void updateStatus(UUID id, UpdateTicketStatusRequestRepresentation request) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado: " + id));

        TicketStatus nextStatus = TicketStatus.valueOf(request.getStatus().name());

        if (!ticket.getStatus().canTransitionTo(nextStatus)) {
            throw new BusinessException("Transição não permitida: de "
                    + ticket.getStatus() + " para " + nextStatus);
        }

        ticket.setStatus(nextStatus);

        if (nextStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        log.info("[Service] Status do ticket {} atualizado para {} por userId: {}",
                ticket.getProtocol(), nextStatus, getAuthenticatedUserId());
    }

    @Transactional
    public void saveAttachment(UUID id, MultipartFile file) {

        ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado ou acesso negado"));

        if (file.isEmpty()) {
            throw new BusinessException("Arquivo vazio.");
        }

        try {
            byte[] fileBytes = file.getBytes();
            UUID tenantId = UUID.fromString(TenantContext.getCurrentTenant());

            attachmentProcessor.processUploadAsync(
                    fileBytes,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    id,
                    tenantId
            );

        } catch (IOException e) {
            throw new BusinessException("Falha ao ler os bytes do arquivo.");
        }
    }

    private UUID getAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = auth.getPrincipal().toString();
        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException e) {
            log.warn("[Security] O principal '{}' é um e-mail. Usando um UUID persistente para este usuário de teste.", principal);
            return UUID.nameUUIDFromBytes(principal.getBytes());
        }
    }

    private String generateProtocol() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "TKT-" + datePart + "-" + randomPart;
    }
}