package com.br.ticket_service.presentation.controller;

import com.br.shared.contracts.api.CategoriesApi;
import com.br.shared.contracts.model.CategoryRequestRepresentation;
import com.br.shared.contracts.model.CategoryResponseRepresentation;
import com.br.ticket_service.application.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoriesApi {

    private final CategoryService service;

    @Override
    public ResponseEntity<List<CategoryResponseRepresentation>> listCategories() {
        return ResponseEntity.ok(service.findAll());
    }

    @Override
    public ResponseEntity<CategoryResponseRepresentation> createCategory(CategoryRequestRepresentation categoryRequest) {
        var response = service.create(categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<Void> updateCategory(UUID id, CategoryRequestRepresentation categoryRequest) {
        service.update(id, categoryRequest);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteCategory(UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}