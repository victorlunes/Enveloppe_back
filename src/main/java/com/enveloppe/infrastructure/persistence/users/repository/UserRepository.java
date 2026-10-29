package com.enveloppe.infrastructure.persistence.users.repository;

import com.enveloppe.infrastructure.persistence.users.entities.UserEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository // o Spring cria e entrega pra quem pedir. É o único lugar do projeto que escreve SQL
public class UserRepository {

    private final JdbcClient jdbcClient; // ferramenta do Spring pra rodar SQL escrito na mão, já conectada no banco do application.yaml

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // CADASTRO: salva o usuário novo
    public UserEntity save(UserEntity userEntity) {
        jdbcClient.sql("""
                    INSERT INTO elp_users (name, email, birthday, password)
                    VALUES (:name, :email, :birthday, :password)
                    """) // o id não entra porque o banco gera sozinho (BIGSERIAL)
                .param("name", userEntity.getName())         // preenche o :name. Nunca junte texto no SQL na mão: isso abre brecha pra SQL injection
                .param("email", userEntity.getEmail())       // preenche o :email
                .param("birthday", userEntity.getBirthday()) // preenche o :birthday
                .param("password", userEntity.getPassword()) // preenche o :password. Aqui já chega o hash, não a senha
                .update(); // update() = executa INSERT, UPDATE ou DELETE

        return userEntity; // devolve o que recebeu (sem id, porque o INSERT não devolve o id gerado)
    }

    // LOGIN: busca o usuário pelo email. Quem chama é o AuthService
    public Optional<UserEntity> findByEmail(String email) { // Optional porque pode não existir ninguém com esse email
        return jdbcClient.sql("""
                    SELECT id, name, email, birthday, password
                    FROM elp_users
                    WHERE email = :email
                    """)
                .param("email", email) // preenche o :email com o que a pessoa digitou no login
                .query((rs, rowNum) -> new UserEntity( // rs = a linha que o banco devolveu. Pra cada linha, monta um UserEntity
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getObject("birthday", LocalDate.class), // coluna DATE → LocalDate
                        rs.getString("password")                   // o hash, que o Spring vai comparar com a senha digitada
                ))
                .optional(); // nenhuma linha → Optional vazio. Atenção: se existirem 2 usuários com o mesmo email, aqui dá erro
    }
}
