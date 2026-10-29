package com.enveloppe.core.gateway.users;

import com.enveloppe.core.entities.User;

// O core fala "preciso salvar um usuário", mas não quer saber onde nem como.
// Quem faz de verdade é o UserRepositoryGateway, lá na infra, com um INSERT no Postgres.
public interface UserGateway {
    User createUser(User user); // recebe o usuário com a senha já criptografada e salva
}
