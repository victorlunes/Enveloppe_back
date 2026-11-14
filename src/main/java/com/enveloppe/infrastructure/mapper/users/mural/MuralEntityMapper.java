package com.enveloppe.infrastructure.mapper.users.mural;

import com.enveloppe.core.entities.Mural;
import com.enveloppe.infrastructure.persistence.users.entities.MuralEntity;
import org.springframework.stereotype.Component;

// Converte entre o Mural (core) e o MuralEntity (banco), campo por campo.
@Component // o Spring cria e entrega pro MuralRepositoryGateway
public class MuralEntityMapper {

    public MuralEntity toMuralEntity(Mural mural) {
        // antes do INSERT: Mural → MuralEntity
        return new MuralEntity(
            mural.id(),
            mural.userId(),
            mural.title(),
            mural.type(),
            mural.observation(),
            mural.achievedIn(),
            mural.createdAt(),
            mural.updatedAt()
        );
    }

    public Mural toMural(MuralEntity entity) {
        // depois do INSERT/SELECT: MuralEntity → Mural
        return new Mural(
            entity.getId(),
            entity.getUserId(),
            entity.getTitle(),
            entity.getType(),
            entity.getObservation(),
            entity.getAchievedIn(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
