package com.enveloppe.core.useCases.users.mural.create;

import com.enveloppe.core.entities.Mural;
import com.enveloppe.core.gateway.users.mural.MuralGateway;

public class CreateMuralUseCaseImpl implements CreateMuralUseCase {
    private final MuralGateway muralGateway;

    public CreateMuralUseCaseImpl(MuralGateway muralGateway) {
        this.muralGateway = muralGateway;
    }

    @Override
    public Mural execute(Mural mural) {
        return muralGateway.createMural(mural);
    }
}
