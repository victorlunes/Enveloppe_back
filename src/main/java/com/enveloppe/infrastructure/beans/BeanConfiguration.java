package com.enveloppe.infrastructure.beans;

import com.enveloppe.core.gateway.security.PasswordEncoderGateway;
import com.enveloppe.core.gateway.users.UserGateway;
import com.enveloppe.core.useCases.users.createUser.CreateUserUseCase;
import com.enveloppe.core.useCases.users.createUser.CreateUserUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// O use case não tem @Component (o core não conhece Spring), então o Spring não acharia ele sozinho.
// Aqui a gente cria com "new" e entrega pronto pro Spring.
@Configuration // o Spring lê os @Bean daqui quando a aplicação sobe
public class BeanConfiguration {

    @Bean // "Spring, guarda esse objeto e entrega pra quem pedir um CreateUserUseCase" (quem pede é o AuthController)
    public CreateUserUseCase createUserUseCase(
            UserGateway userGateway,                      // o Spring preenche sozinho com o UserRepositoryGateway
            PasswordEncoderGateway passwordEncoderGateway // o Spring preenche sozinho com o BCryptPasswordEncoderGateway
    ) {
        return new CreateUserUseCaseImpl(userGateway, passwordEncoderGateway); // monta o use case com as peças da infra
    }
}
