package com.enveloppe.infrastructure.mapper.users;

import com.enveloppe.core.entities.User;
import com.enveloppe.infrastructure.dtos.users.user.UserRequest;
import com.enveloppe.infrastructure.dtos.users.user.UserResponse;
import org.springframework.stereotype.Component;

// Converte entre o JSON (DTOs) e o User do core. O core nunca vê DTO, quem traduz é essa classe.
@Component // o Spring cria e entrega pro AuthController
public class UserMapper {

    public User toUser(UserRequest userRequest) { // ida: JSON que chegou → User
        return new User(
                null,                   // id ainda não existe: quem gera é o banco
                userRequest.name(),
                userRequest.email(),
                userRequest.birthday(),
                userRequest.password()  // ainda é a senha pura. Quem criptografa é o use case
        );
    }

    public UserResponse toUserResponse(User user) { // volta: User → JSON de resposta
        return new UserResponse(user.name(), user.email()); // só nome e email. Senha e aniversário ficam de fora
    }
}
