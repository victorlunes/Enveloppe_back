package com.enveloppe.core.useCases.users.createUser;

import com.enveloppe.core.entities.User;
import com.enveloppe.core.gateway.security.PasswordEncoderGateway;
import com.enveloppe.core.gateway.users.UserGateway;

// A regra de negócio do cadastro: criptografa a senha e manda salvar.
// Repara que não tem @Component: o core não usa Spring. Quem cria essa classe é o BeanConfiguration.
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserGateway userGateway;                       // na prática, o Spring entrega aqui o UserRepositoryGateway
    private final PasswordEncoderGateway passwordEncoderGateway; // na prática, o Spring entrega aqui o BCryptPasswordEncoderGateway

    public CreateUserUseCaseImpl(
            UserGateway userGateway,
            PasswordEncoderGateway passwordEncoderGateway
    ){
        this.userGateway = userGateway;
        this.passwordEncoderGateway = passwordEncoderGateway;
    }

    @Override
    public User execute(User user) {
        String encodedPassword = passwordEncoderGateway.encode(user.password()); // "123456" vira "$2a$10$Xk9...". Senha do jeito que a pessoa digitou nunca vai pro banco

        User userWithEncodedPassword = new User( // record não deixa alterar, então cria um User novo, igualzinho, só que com o hash
                user.id(),
                user.name(),
                user.email(),
                user.birthday(),
                encodedPassword
        );

        return userGateway.createUser(userWithEncodedPassword); // manda salvar. O use case nem sabe que é Postgres, só sabe que "salvou"
    }
}
