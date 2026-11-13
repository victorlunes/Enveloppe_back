package com.enveloppe.infrastructure.presentation.controller.user.mural;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enveloppe.core.entities.Mural;
import com.enveloppe.core.useCases.users.mural.create.CreateMuralUseCase;
import com.enveloppe.infrastructure.dtos.users.mural.MuralDto;
import com.enveloppe.infrastructure.mapper.users.mural.MuralMapper;
import com.enveloppe.infrastructure.security.JWTUserData;

@RestController
@RequestMapping("/enveloppe/user/mural")
public class MuralController {

    private final CreateMuralUseCase createMuralUseCase;
    private final MuralMapper muralMapper;

    public MuralController(CreateMuralUseCase createMuralUseCase, MuralMapper muralMapper) {
        this.createMuralUseCase = createMuralUseCase;
        this.muralMapper = muralMapper;
    }

    @PostMapping
    public ResponseEntity<MuralDto> createMural(
            @RequestBody MuralDto muralDto,
            @AuthenticationPrincipal JWTUserData user
    ) {
        Mural mural = muralMapper.toMural(muralDto, user.id()); // o dono vem do token
        Mural createdMural = createMuralUseCase.execute(mural);
        return ResponseEntity.status(HttpStatus.CREATED).body(muralMapper.toMuralDto(createdMural));
    }
}
