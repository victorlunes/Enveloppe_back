package com.enveloppe.core.useCases.users.createUser;

import com.enveloppe.core.entities.User;

public interface CreateUserUseCase {
    User execute(User user);
}
