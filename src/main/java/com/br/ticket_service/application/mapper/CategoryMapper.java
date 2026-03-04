package com.br.ticket_service.application.mapper;

import com.br.shared.contracts.model.CategoryRequestRepresentation;
import com.br.shared.contracts.model.CategoryResponseRepresentation;
import com.br.ticket_service.domain.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toEntity(CategoryRequestRepresentation request);
    CategoryResponseRepresentation toResponse(Category category);

    void updateEntityFromRequest(CategoryRequestRepresentation request, @MappingTarget Category category);
}