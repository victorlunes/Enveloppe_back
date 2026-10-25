package com.enveloppe.core.gateway.security;

public interface PasswordEncoderGateway {
    String encode(String rawPassword);
}
