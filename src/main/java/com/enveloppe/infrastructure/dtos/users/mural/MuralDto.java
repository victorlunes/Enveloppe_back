package com.enveloppe.infrastructure.dtos.users.mural;

import java.time.LocalDateTime;

import com.enveloppe.core.enums.MuralType;

// O JSON do mural, na ida e na volta:
// { "title": "Corri minha primeira meia maratona", "type": "SAUDE", "observation": "21 km", "achievedIn": "2026-10-19T08:30:00" }
// Sem userId de propósito: o dono vem do token, nunca do que a tela manda
public record MuralDto(
        Long id,                  // na criação a tela não manda; na resposta volta o id gerado
        String title,
        MuralType type,           // no JSON vem o código (SAUDE), o Spring converte pro enum
        String observation,       // opcional
        LocalDateTime achievedIn, // texto "AAAA-MM-DDTHH:mm:ss"
        LocalDateTime createdAt   // só na resposta: quem preenche é o banco
) {
}
