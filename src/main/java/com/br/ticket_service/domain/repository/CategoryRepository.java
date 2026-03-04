package com.br.ticket_service.domain.repository;

import com.br.ticket_service.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {

    Optional<Category> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Category> findByNameAndTenantId(String name, UUID tenantId);
}