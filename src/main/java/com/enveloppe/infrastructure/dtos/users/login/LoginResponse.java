package com.enveloppe.infrastructure.dtos.users.login;

// O JSON que volta no login: { "token": "eyJhbGciOiJIUzI1NiIs..." }
public record LoginResponse(
        String token // o JWT. O cliente guarda e manda em toda requisição: Authorization: Bearer <token>
) {
}
