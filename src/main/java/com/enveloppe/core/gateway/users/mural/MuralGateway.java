package com.enveloppe.core.gateway.users.mural;

import com.enveloppe.core.entities.Mural;

// O contrato do core com o banco. Só fala em Mural: o core não sabe que existe MuralEntity
public interface MuralGateway {
    Mural createMural(Mural mural);
}
