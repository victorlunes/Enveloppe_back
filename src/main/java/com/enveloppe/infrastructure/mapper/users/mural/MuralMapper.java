package com.enveloppe.infrastructure.mapper.users.mural;

import com.enveloppe.core.entities.Mural;
import com.enveloppe.infrastructure.dtos.users.mural.MuralDto;
import org.springframework.stereotype.Component;

// Converte entre o JSON (MuralDto) e o Mural do core. O core nunca vê DTO, quem traduz é essa classe.
@Component // o Spring cria e entrega pro MuralController
public class MuralMapper {

    public Mural toMural(MuralDto muralDto, Long userId) {
        // ida: JSON que chegou + id do token → Mural
        return new Mural(
            null, // id ainda não existe: quem gera é o banco
            userId, // o dono vem do token, não do JSON
            muralDto.title(),
            muralDto.type(),
            muralDto.observation(),
            muralDto.achievedIn(),
            null, // createdAt: quem preenche é o banco
            null // updatedAt: quem preenche é o banco
        );
    }

    public MuralDto toMuralDto(Mural mural) {
        // volta: Mural → JSON de resposta (sem userId)
        return new MuralDto(
            mural.id(),
            mural.title(),
            mural.type(),
            mural.observation(),
            mural.achievedIn(),
            mural.createdAt()
        );
    }
}
