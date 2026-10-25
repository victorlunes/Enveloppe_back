package com.enveloppe.infrastructure.gateway.security;

import com.enveloppe.core.gateway.security.PasswordEncoderGateway;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderGateway implements PasswordEncoderGateway {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordEncoderGateway(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
