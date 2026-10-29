package com.enveloppe.core.gateway.security;

// O core fala "preciso criptografar essa senha", mas não quer saber com qual ferramenta.
// Quem faz de verdade é o BCryptPasswordEncoderGateway, lá na infra.
public interface PasswordEncoderGateway {
    String encode(String rawPassword); // recebe "123456" e devolve algo como "$2a$10$Xk9..."
}
