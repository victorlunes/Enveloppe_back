package com.enveloppe.infrastructure.persistence.users.entities;

import java.time.LocalDateTime;

import com.enveloppe.core.enums.MuralType;

public class MuralEntity {
    private Long id;                  // quem gera é o banco. Na criação chega null
    private Long userId;              // dono da conquista. Vem do token, nunca do que a tela manda
    private String title;
    private MuralType type;           // CARREIRA, SAUDE, VIAGEM...
    private String observation;       // opcional
    private LocalDateTime achievedIn; // quando a pessoa conquistou (pode ser no passado)
    private LocalDateTime createdAt;  // quem preenche é o banco
    private LocalDateTime updatedAt;  // quem preenche é o banco

    public MuralEntity(
            Long id,
            Long userId,
            String title,
            MuralType type,
            String observation,
            LocalDateTime achievedIn,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.type = type;
        this.observation = observation;
        this.achievedIn = achievedIn;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public MuralType getType() {
        return type;
    }

    public String getObservation() {
        return observation;
    }

    public LocalDateTime getAchievedIn() {
        return achievedIn;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setAchievedIn(LocalDateTime achievedIn) {
        this.achievedIn = achievedIn;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public void setType(MuralType type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
