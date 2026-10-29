package com.enveloppe.infrastructure.persistence.users.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

// O usuário do jeito que o BANCO enxerga: uma linha da tabela elp_users.
// E por que o UserDetails está AQUI e não no User do core? Porque UserDetails é do Spring Security.
// Se o User do core implementasse, o core ia depender do Spring, e a etapa "Check clean architecture" da pipeline ficaria vermelha.
public class UserEntity implements UserDetails { // UserDetails = o "formato de usuário" que o Spring Security entende na hora do login

    private final Long id;            // coluna id
    private final String name;        // coluna name
    private final String email;       // coluna email
    private final LocalDate birthday; // coluna birthday (DATE)
    private final String password;    // coluna password: o hash, nunca a senha pura

    public UserEntity(Long id, String name, String email, LocalDate birthday, String password) { // tudo final e sem setter: criou, não muda mais
        this.id = id;
        this.name = name;
        this.email = email;
        this.birthday = birthday;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    @Override
    public String getPassword() { // cadastro: o INSERT pega o hash daqui | login: o Spring pega o hash daqui pra comparar com a senha digitada
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // permissões (ADMIN, USER...). Ainda não temos, então lista vazia
        return List.of();
    }

    @Override
    public String getUsername() { // o Spring pergunta "qual é o login desse usuário?". No enveloppe o login é o email
        return email;
    }
}
