package com.enveloppe.infrastructure.dtos.users.login;

// O JSON que chega no login: { "email": "ana@email.com", "password": "123456" }
public record LoginRequest(
        String email,   // é com ele que o AuthService busca o usuário no banco
        String password // a senha digitada, em texto puro. Ela só é comparada com o hash, nunca é salva
) {
}
