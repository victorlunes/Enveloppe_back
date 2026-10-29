package com.enveloppe.infrastructure.mapper.users;

import com.enveloppe.core.entities.User;
import com.enveloppe.infrastructure.persistence.users.entities.UserEntity;
import org.springframework.stereotype.Component;

// Converte entre o User (core) e o UserEntity (banco), campo por campo.
@Component // o Spring cria e entrega pro UserRepositoryGateway (cadastro) e pro AuthController (login)
public class UserEntityMapper {

    public UserEntity toUserEntity(User user) { // cadastro: User → UserEntity, antes do INSERT
        return new UserEntity(
                user.id(),
                user.name(),
                user.email(),
                user.birthday(),
                user.password()
        );
    }

    public User toUser(UserEntity userEntity) { // cadastro: depois do INSERT | login: o UserEntity que logou vira User pro TokenService
        return new User(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getBirthday(),
                userEntity.getPassword()
        );
    }
}
