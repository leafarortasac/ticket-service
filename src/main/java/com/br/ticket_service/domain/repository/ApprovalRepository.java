package com.br.ticket_service.domain.repository;

import com.br.ticket_service.domain.entity.ApprovalRequest;
import com.br.ticket_service.domain.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovalRepository extends JpaRepository<ApprovalRequest, UUID>, JpaSpecificationExecutor<ApprovalRequest> {

    List<ApprovalRequest> findByTicketIdAndStatus(UUID ticketId, ApprovalStatus status);

    Optional<ApprovalRequest> findFirstByTicketIdAndStatusOrderByCreatedAtDesc(UUID ticketId, ApprovalStatus status);
}