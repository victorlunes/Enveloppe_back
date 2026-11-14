package com.enveloppe.infrastructure.persistence.users.repository;

import com.enveloppe.core.enums.MuralType;
import com.enveloppe.infrastructure.persistence.users.entities.MuralEntity;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository // o Spring cria e entrega pro MuralRepositoryGateway
public class MuralRepository {

    private final JdbcClient jdbcClient;

    public MuralRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // CRIAÇÃO: salva uma conquista nova no mural e devolve ela completa, já com id e datas
    public MuralEntity save(MuralEntity muralEntity) {
        return jdbcClient
            .sql(
                """
                INSERT INTO elp_mural (user_id, title, type, observation, achieved_in)
                VALUES (:userId, :title, :type, :observation, :achievedIn)
                RETURNING id, user_id, title, type, observation, achieved_in, created_at, updated_at
                """
            ) // id, created_at e updated_at não entram: o banco gera. O RETURNING devolve a linha salva
            .param("userId", muralEntity.getUserId()) // o dono, que veio do token
            .param("title", muralEntity.getTitle())
            .param("type", muralEntity.getType().name())
            .param("observation", muralEntity.getObservation()) // pode ser null
            .param("achievedIn", muralEntity.getAchievedIn())
            .query((rs, rowNum) ->
                new MuralEntity(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("title"),
                    MuralType.valueOf(rs.getString("type")),
                    rs.getString("observation"),
                    rs.getObject("achieved_in", LocalDateTime.class),
                    rs.getObject("created_at", LocalDateTime.class),
                    rs.getObject("updated_at", LocalDateTime.class)
                )
            )
            .single();
    }
}
