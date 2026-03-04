package infrastructure.security;

import com.br.ticket_service.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve armazenar e recuperar o tenantId corretamente")
    void shouldStoreAndRetrieveTenantId() {
        // Arrange
        String expectedTenantId = UUID.randomUUID().toString();

        // Act
        TenantContext.setCurrentTenant(expectedTenantId);
        String actualTenantId = TenantContext.getCurrentTenant();

        // Assert
        assertEquals(expectedTenantId, actualTenantId);
    }

    @Test
    @DisplayName("Deve retornar null após limpar o contexto")
    void shouldReturnNullAfterClear() {
        // Arrange
        TenantContext.setCurrentTenant(UUID.randomUUID().toString());

        // Act
        TenantContext.clear();

        // Assert
        assertNull(TenantContext.getCurrentTenant());
    }
}