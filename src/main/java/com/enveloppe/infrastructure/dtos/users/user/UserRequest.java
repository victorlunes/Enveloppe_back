package com.enveloppe.infrastructure.dtos.users.user;

import java.time.LocalDate;

public record UserRequest(
        String name,
        String email,
        LocalDate birthday,
        String password
) {
}
