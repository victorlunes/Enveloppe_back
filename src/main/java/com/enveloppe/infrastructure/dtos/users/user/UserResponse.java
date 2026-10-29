package com.enveloppe.infrastructure.dtos.users.user;

// O JSON que volta no cadastro: { "name": "Ana", "email": "ana@email.com" }
public record UserResponse(
        String name,
        String email // sem senha (nem o hash) e sem aniversário: só o que o cliente precisa ver
) {
}
