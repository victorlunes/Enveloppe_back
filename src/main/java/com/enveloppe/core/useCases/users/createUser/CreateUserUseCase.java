package com.enveloppe.core.useCases.users.createUser;

import com.enveloppe.core.entities.User;

// A "ação" de criar conta. O AuthController só conhece essa interface, nunca a implementação.
public interface CreateUserUseCase {
    User execute(User user); // cada use case faz UMA coisa, e sempre pelo execute
}
