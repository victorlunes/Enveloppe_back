package com.enveloppe.infrastructure.security;

import com.enveloppe.infrastructure.persistence.users.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// O Spring Security não faz ideia de onde ficam os nossos usuários. Essa classe ensina pra ele:
// "quando precisar de um usuário, busca no banco pelo email".
@Service // vira bean. Como é o único UserDetailsService do projeto, o AuthenticationManager usa ele sozinho
public class AuthService implements UserDetailsService { // UserDetailsService = a interface que o Spring Security chama na hora do login

    private final UserRepository userRepository; // onde está o SELECT por email

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { // o Spring chama de "username", mas pra gente é o email que a pessoa digitou
        return userRepository.findByEmail(username).orElseThrow( // achou? Devolve o UserEntity (que é um UserDetails). Não achou?...
                () -> new UsernameNotFoundException("User or password is incorrect") // ...estoura. O Spring troca por BadCredentialsException, então ninguém descobre se errou o email ou a senha
        );
    }
}
