package com.enveloppe.core.useCases.users.updateEntry;

import com.enveloppe.core.gateway.users.UserGateway;

public class UpdateEntryUseCaseImpl implements UpdateEntryUseCase{
    private final UserGateway userGateway;

    public UpdateEntryUseCaseImpl(UserGateway userGateway){
        this.userGateway = userGateway;
    }

    @Override
    public boolean execute(Long userId) {
        return userGateway.updateEntry(userId);
    }
}
