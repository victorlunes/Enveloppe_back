package com.enveloppe.infrastructure.beans;

import com.enveloppe.core.gateway.security.PasswordEncoderGateway;
import com.enveloppe.core.gateway.users.UserGateway;
import com.enveloppe.core.useCases.users.createUser.CreateUserUseCase;
import com.enveloppe.core.useCases.users.createUser.CreateUserUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CreateUserUseCase createUserUseCase(
            UserGateway userGateway,
            PasswordEncoderGateway passwordEncoderGateway
    ) {
        return new CreateUserUseCaseImpl(userGateway, passwordEncoderGateway);
    }
}
