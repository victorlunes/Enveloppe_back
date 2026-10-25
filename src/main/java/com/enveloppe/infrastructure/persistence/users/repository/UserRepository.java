package com.enveloppe.infrastructure.persistence.users.repository;

import com.enveloppe.infrastructure.persistence.users.entities.UserEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public UserEntity save(UserEntity userEntity) {
        jdbcClient.sql("""
                    INSERT INTO elp_users (name, email, birthday, password)
                    VALUES (:name, :email, :birthday, :password)
                    """)
                .param("name", userEntity.getName())
                .param("email", userEntity.getEmail())
                .param("birthday", userEntity.getBirthday())
                .param("password", userEntity.getPassword())
                .update();

        return userEntity;
    }
}
