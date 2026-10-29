package com.enveloppe.infrastructure.dtos.users.user;

import java.time.LocalDate;

// O JSON que chega no cadastro. O Spring preenche esse record sozinho, casando pelo nome dos campos:
// { "name": "Ana", "email": "ana@email.com", "birthday": "1998-05-20", "password": "123456" }
public record UserRequest(
        String name,
        String email,
        LocalDate birthday, // no JSON vem como texto "AAAA-MM-DD" e o Spring converte pra LocalDate
        String password     // senha pura. Ela só vira hash lá no use case
) {
}
