package com.enveloppe.core.useCases.users.createUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enveloppe.core.entities.User;
import com.enveloppe.core.gateway.security.PasswordEncoderGateway;
import com.enveloppe.core.gateway.users.UserGateway;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Teste de unidade: sem Spring e sem banco. Os gateways são de mentira (mocks do Mockito)
@ExtendWith(MockitoExtension.class) // liga o Mockito: ele cria os @Mock e monta o @InjectMocks
class CreateUserUseCaseImplTest {

    @Mock
    private UserGateway userGateway; // finge o banco

    @Mock
    private PasswordEncoderGateway passwordEncoderGateway; // finge o BCrypt

    @InjectMocks
    private CreateUserUseCaseImpl useCase; // o de verdade, recebendo os dois mocks no construtor

    @Test
    void salvaOUsuarioComASenhaCriptografada() {
        User user = new User(null, "Ana", "ana@email.com", LocalDate.of(1998, 5, 20), "senha-pura");
        when(passwordEncoderGateway.encode("senha-pura")).thenReturn("hash");
        when(userGateway.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0)); // devolve o que recebeu

        User saved = useCase.execute(user);

        ArgumentCaptor<User> sent = ArgumentCaptor.forClass(User.class); // pega o User que foi pro gateway
        verify(userGateway).createUser(sent.capture());
        assertThat(sent.getValue().password()).isEqualTo("hash"); // a senha pura nunca chega no banco
        assertThat(sent.getValue().email()).isEqualTo("ana@email.com"); // o resto continua igual
        assertThat(saved.password()).isEqualTo("hash");
    }
}
