package com.enveloppe.core.useCases.users.mural.create;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enveloppe.core.entities.Mural;
import com.enveloppe.core.enums.MuralType;
import com.enveloppe.core.gateway.users.mural.MuralGateway;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateMuralUseCaseImplTest {

    @Mock
    private MuralGateway muralGateway;

    @InjectMocks
    private CreateMuralUseCaseImpl useCase;

    @Test
    void salvaOMuralEDevolveOQueOBancoGerou() {
        LocalDateTime achieved = LocalDateTime.of(2026, 10, 19, 8, 30);
        Mural mural = new Mural(null, 1L, "Meia maratona", MuralType.SAUDE, "21 km", achieved, null, null);
        LocalDateTime now = LocalDateTime.now();
        Mural saved = new Mural(10L, 1L, "Meia maratona", MuralType.SAUDE, "21 km", achieved, now, now);
        when(muralGateway.createMural(mural)).thenReturn(saved);

        Mural result = useCase.execute(mural);

        verify(muralGateway).createMural(mural);
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.type()).isEqualTo(MuralType.SAUDE);
    }
}
