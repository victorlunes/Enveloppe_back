package com.enveloppe.core.useCases.users.createUser;

import com.enveloppe.core.entities.User;
import com.enveloppe.core.gateway.security.PasswordEncoderGateway;
import com.enveloppe.core.gateway.users.UserGateway;

public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserGateway userGateway;
    private final PasswordEncoderGateway passwordEncoderGateway;

    public CreateUserUseCaseImpl(
            UserGateway userGateway,
            PasswordEncoderGateway passwordEncoderGateway
    ){
        this.userGateway = userGateway;
        this.passwordEncoderGateway = passwordEncoderGateway;
    }

    @Override
    public User execute(User user) {
        String encodedPassword = passwordEncoderGateway.encode(user.password());

        User userWithEncodedPassword = new User(
                user.id(),
                user.name(),
                user.email(),
                user.birthday(),
                encodedPassword
        );

        return userGateway.createUser(userWithEncodedPassword);
    }
}
