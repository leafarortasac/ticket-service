package com.br.ticket_service.application.mapper;

import com.br.shared.contracts.model.*;
import com.br.ticket_service.domain.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "protocol", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Ticket toEntity(TicketRequestRepresentation request);

    TicketResponseRepresentation toResponse(Ticket ticket);

    List<TicketResponseRepresentation> toResponseList(List<Ticket> tickets);

    default TicketDocumentResponseRepresentation toDocumentResponse(Page<Ticket> page) {
        TicketDocumentResponseRepresentation response = new TicketDocumentResponseRepresentation();

        List<TicketDocumentRepresentation> records = page.getContent().stream().map(ticket -> {
            TicketDocumentRepresentation doc = new TicketDocumentRepresentation();
            doc.setTicket(toResponse(ticket));
            return doc;
        }).toList();

        response.setRecords(records);
        response.setTotalElements((int) page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setCurrentPage(page.getNumber());

        return response;
    }
}