package com.enveloppe.core.entities;

import com.enveloppe.core.enums.MuralType;
import java.time.LocalDateTime;

// Uma conquista do mural, do jeito que a regra de negócio enxerga. Java puro: não sabe que existe Spring, banco ou JSON.
public record Mural(
    // record = imutável. Quer mudar algum campo? Cria um Mural novo
    Long id, // quem gera é o banco. Na criação chega null
    Long userId, // dono da conquista. Vem do token, nunca do que a tela manda
    String title,
    MuralType type, // CARREIRA, SAUDE, VIAGEM...
    String observation, // opcional
    LocalDateTime achievedIn, // quando a pessoa conquistou (pode ser no passado)
    LocalDateTime createdAt, // quem preenche é o banco
    LocalDateTime updatedAt // quem preenche é o banco
) {}
