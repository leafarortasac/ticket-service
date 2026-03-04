package application.service;

import com.br.ticket_service.application.mapper.CategoryMapper;
import com.br.ticket_service.application.service.CategoryService;
import com.br.ticket_service.domain.entity.Category;
import com.br.ticket_service.domain.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository repository;

    @Mock
    private CategoryMapper mapper;

    @Test
    @DisplayName("Deve inativar categoria (Soft Delete)")
    void shouldDeactivateCategory() {
        UUID id = UUID.randomUUID();
        Category category = new Category();
        category.setActive(true);

        when(repository.findById(id)).thenReturn(Optional.of(category));

        categoryService.delete(id);

        assertFalse(category.getActive());
        verify(repository, never()).delete((Category) any());
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar categoria inexistente")
    void shouldThrowExceptionWhenCategoryNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.delete(id));
    }
}