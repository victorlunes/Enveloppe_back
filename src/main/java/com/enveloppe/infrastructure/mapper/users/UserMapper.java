package com.enveloppe.infrastructure.mapper.users;

import com.enveloppe.core.entities.User;
import com.enveloppe.infrastructure.dtos.users.user.UserRequest;
import com.enveloppe.infrastructure.dtos.users.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(UserRequest userRequest) {
        return new User(
                null,
                userRequest.name(),
                userRequest.email(),
                userRequest.birthday(),
                userRequest.password()
        );
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(user.name(), user.email());
    }
}
