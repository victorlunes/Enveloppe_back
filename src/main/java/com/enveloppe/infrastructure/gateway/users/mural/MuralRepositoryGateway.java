package com.enveloppe.infrastructure.gateway.users.mural;

import com.enveloppe.core.entities.Mural;
import com.enveloppe.core.gateway.users.mural.MuralGateway;
import com.enveloppe.infrastructure.mapper.users.mural.MuralEntityMapper;
import com.enveloppe.infrastructure.persistence.users.entities.MuralEntity;
import com.enveloppe.infrastructure.persistence.users.repository.MuralRepository;
import org.springframework.stereotype.Component;

@Component
public class MuralRepositoryGateway implements MuralGateway {

    private final MuralRepository muralRepository;
    private final MuralEntityMapper muralEntityMapper;

    public MuralRepositoryGateway(
            MuralRepository muralRepository,
            MuralEntityMapper muralEntityMapper
    ){
        this.muralRepository = muralRepository;
        this.muralEntityMapper = muralEntityMapper;
    }

    @Override
    public Mural createMural(Mural mural) {
        MuralEntity postMural = muralEntityMapper.toMuralEntity(mural); // core → banco
        MuralEntity saved = muralRepository.save(postMural);            // INSERT, já volta com id e datas
        return muralEntityMapper.toMural(saved);                        // banco → core
    }
}
