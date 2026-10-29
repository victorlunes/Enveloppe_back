package com.enveloppe.infrastructure.gateway.users;

import com.enveloppe.core.entities.User;
import com.enveloppe.core.gateway.users.UserGateway;
import com.enveloppe.infrastructure.mapper.users.UserEntityMapper;
import com.enveloppe.infrastructure.persistence.users.entities.UserEntity;
import com.enveloppe.infrastructure.persistence.users.repository.UserRepository;
import org.springframework.stereotype.Component;

// Faz o que o UserGateway do core pediu, usando o banco.
// Quando o use case chama userGateway.createUser(...), é ESTE método que roda.
@Component // por ser o único que implementa UserGateway, o Spring entrega ele pro use case
public class UserRepositoryGateway implements UserGateway {

    private final UserRepository userRepository;     // quem roda o SQL
    private final UserEntityMapper userEntityMapper; // quem converte User ↔ UserEntity

    public UserRepositoryGateway(UserRepository userRepository, UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEntityMapper = userEntityMapper;
    }

    @Override
    public User createUser(User user) {
        UserEntity userEntity = userEntityMapper.toUserEntity(user); // User (core) → UserEntity (banco)
        UserEntity savedUser = userRepository.save(userEntity);       // roda o INSERT
        return userEntityMapper.toUser(savedUser);                    // UserEntity → User, e devolve pro use case
    }
}
