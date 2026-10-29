package com.enveloppe.infrastructure.presentation.exception;

// A nossa exceção pra "email ou senha errados".
// Ela existe pra gente transformar o erro do Spring (BadCredentialsException) numa resposta 401, lá no ApplicationControllerAdvice.
public class UsernameOrPasswordInvalidException extends RuntimeException { // RuntimeException = não obriga ninguém a colocar throws na assinatura
    public UsernameOrPasswordInvalidException(String message) {
        super(message); // guarda a mensagem, que vai virar o corpo da resposta
    }
}
