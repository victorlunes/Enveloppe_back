package com.enveloppe.core.entities;

import java.time.LocalDate;

public record User(
        Long id,
        String name,
        String email,
        LocalDate birthday,
        String password
) {
}
