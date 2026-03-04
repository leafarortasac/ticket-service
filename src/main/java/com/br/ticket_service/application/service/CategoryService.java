package com.br.ticket_service.application.service;

import com.br.shared.contracts.model.CategoryRequestRepresentation;
import com.br.shared.contracts.model.CategoryResponseRepresentation;
import com.br.ticket_service.application.mapper.CategoryMapper;
import com.br.ticket_service.domain.entity.Category;
import com.br.ticket_service.domain.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Transactional(readOnly = true)
    public List<CategoryResponseRepresentation> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponseRepresentation create(CategoryRequestRepresentation request) {
        Category category = mapper.toEntity(request);
        return mapper.toResponse(repository.save(category));
    }

    @Transactional
    public CategoryResponseRepresentation update(UUID id, CategoryRequestRepresentation request) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        mapper.updateEntityFromRequest(request, category);
        return mapper.toResponse(repository.save(category));
    }

    @Transactional
    public void delete(UUID id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
        category.setActive(false);
    }
}