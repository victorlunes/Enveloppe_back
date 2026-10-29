package com.enveloppe.core.entities;

import java.time.LocalDate;

// O usuário do jeito que a regra de negócio enxerga. Java puro: não sabe que existe Spring, banco ou JSON.
public record User( // record = imutável. Quer mudar algum campo? Cria um User novo
        Long id,            // quem gera é o banco. No cadastro chega null
        String name,
        String email,       // é o login da pessoa
        LocalDate birthday, // só a data, sem hora (igual à coluna DATE do banco)
        String password     // no cadastro chega a senha pura, e o use case troca pelo hash antes de salvar
) {
}
