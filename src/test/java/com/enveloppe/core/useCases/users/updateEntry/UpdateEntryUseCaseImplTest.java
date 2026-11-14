package com.enveloppe.core.useCases.users.updateEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enveloppe.core.gateway.users.UserGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateEntryUseCaseImplTest {

    @Mock
    private UserGateway userGateway;

    @InjectMocks
    private UpdateEntryUseCaseImpl useCase;

    @Test
    void devolveTrueQuandoAEntradaDeHojeAindaNaoFoiContada() {
        when(userGateway.updateEntry(1L)).thenReturn(true);

        assertThat(useCase.execute(1L)).isTrue();
        verify(userGateway).updateEntry(1L);
    }

    @Test
    void devolveFalseQuandoHojeJaFoiContado() {
        when(userGateway.updateEntry(1L)).thenReturn(false);

        assertThat(useCase.execute(1L)).isFalse();
    }
}
