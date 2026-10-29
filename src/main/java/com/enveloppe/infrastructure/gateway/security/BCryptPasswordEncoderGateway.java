package com.enveloppe.infrastructure.gateway.security;

import com.enveloppe.core.gateway.security.PasswordEncoderGateway;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Faz o que o PasswordEncoderGateway do core pediu, usando o BCrypt do Spring Security.
// Só o cadastro passa por aqui. No login, o Spring usa o PasswordEncoder direto pra comparar a senha.
@Component // por ser o único que implementa PasswordEncoderGateway, o Spring entrega ele pro use case
public class BCryptPasswordEncoderGateway implements PasswordEncoderGateway {

    private final PasswordEncoder passwordEncoder; // vem do @Bean passwordEncoder() do SecurityConfig, que é o BCrypt

    public BCryptPasswordEncoderGateway(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword); // gera o hash. É o MESMO BCrypt que o login usa pra conferir, por isso bate
    }
}
