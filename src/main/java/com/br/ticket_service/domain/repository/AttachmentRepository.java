package com.br.ticket_service.domain.repository;

import com.br.ticket_service.domain.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID>, JpaSpecificationExecutor<Attachment> {

    List<Attachment> findByTicketId(UUID ticketId);

    java.util.Optional<Attachment> findByIdAndTicketId(UUID id, UUID ticketId);
}