package com.br.ticket_service.presentation.controller;

import com.br.shared.contracts.api.TicketsApi;
import com.br.shared.contracts.model.*;
import com.br.ticket_service.application.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TicketController implements TicketsApi {

    private final TicketService ticketService;

    @Override
    public ResponseEntity<List<TicketResponseRepresentation>> createTickets(
            List<TicketRequestRepresentation> ticketRequestRepresentation) {
        List<TicketResponseRepresentation> response = ticketService.createTickets(ticketRequestRepresentation);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<TicketResponseRepresentation> getTicketById(UUID id) {
        return ResponseEntity.ok(ticketService.findById(id));
    }

    @Override
    public ResponseEntity<TicketDocumentResponseRepresentation> listTickets(
            TicketStatusRepresentation status,
            TicketPriorityRepresentation priority,
            String category,
            Integer limit,
            Integer page,
            String sortField,
            String sortDir,
            Boolean unPaged) {

        var response = ticketService.listAll(
                status, priority, category, limit, page, sortField, sortDir, unPaged
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> updateTicketStatus(UUID id, UpdateTicketStatusRequestRepresentation request) {
        ticketService.updateStatus(id, request);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> uploadAttachment(UUID id, MultipartFile file) {
        ticketService.saveAttachment(id, file);
        return ResponseEntity.accepted().build();
    }
}