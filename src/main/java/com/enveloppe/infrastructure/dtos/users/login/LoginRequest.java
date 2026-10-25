package com.enveloppe.infrastructure.dtos.users.login;

public record LoginRequest(
        String email,
        String password
) {
}
