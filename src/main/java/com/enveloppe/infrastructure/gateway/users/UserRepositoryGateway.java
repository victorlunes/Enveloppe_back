package com.enveloppe.infrastructure.gateway.users;

import com.enveloppe.core.entities.User;
import com.enveloppe.core.gateway.users.UserGateway;
import com.enveloppe.infrastructure.mapper.users.UserEntityMapper;
import com.enveloppe.infrastructure.persistence.users.entities.UserEntity;
import com.enveloppe.infrastructure.persistence.users.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryGateway implements UserGateway {

    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;

    public UserRepositoryGateway(UserRepository userRepository, UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEntityMapper = userEntityMapper;
    }

    @Override
    public User createUser(User user) {
        UserEntity userEntity = userEntityMapper.toUserEntity(user);
        UserEntity savedUser = userRepository.save(userEntity);
        return userEntityMapper.toUser(savedUser);
    }
}
