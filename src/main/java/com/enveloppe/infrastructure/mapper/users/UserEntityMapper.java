package com.enveloppe.infrastructure.mapper.users;

import com.enveloppe.core.entities.User;
import com.enveloppe.infrastructure.persistence.users.entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserEntity toUserEntity(User user) {
        return new UserEntity(
                user.id(),
                user.name(),
                user.email(),
                user.birthday(),
                user.password()
        );
    }

    public User toUser(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getBirthday(),
                userEntity.getPassword()
        );
    }
}