package com.enveloppe.infrastructure.persistence.users.entities;

import java.time.LocalDate;

public class UserEntity {

    private final Long id;
    private final String name;
    private final String email;
    private final LocalDate birthday;
    private final String password;

    public UserEntity(Long id, String name, String email, LocalDate birthday, String password) {
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

    public String getPassword() {
        return password;
    }
}