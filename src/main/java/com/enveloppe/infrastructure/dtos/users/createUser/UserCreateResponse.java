package com.enveloppe.infrastructure.dtos.users.createUser;

// O JSON que volta no cadastro: { "name": "Ana", "email": "ana@email.com" }
public record UserCreateResponse(
    String name,
    String email // sem senha (nem o hash) e sem aniversário: só o que o cliente precisa ver
) {}
