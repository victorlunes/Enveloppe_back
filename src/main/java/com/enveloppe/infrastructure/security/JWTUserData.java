package com.enveloppe.infrastructure.security;

// Os dados de quem está logado, tirados de dentro do token.
// O SecurityFilter monta ele a cada requisição, sem precisar ir no banco.
public record JWTUserData(Long id, String name, String email) { // id = claim "userId" | name = claim "name" | email = o "sub" do token
}
