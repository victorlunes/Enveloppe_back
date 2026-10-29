package com.enveloppe.infrastructure.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // fica de olho em todos os controllers: exceção que escapar deles cai aqui e vira resposta HTTP
public class ApplicationControllerAdvice {

    @ExceptionHandler(UsernameOrPasswordInvalidException.class) // quando essa exceção estourar...
    @ResponseStatus(HttpStatus.UNAUTHORIZED)                    // ...responde 401 (não autorizado). Sem isso viraria 500, como se a API tivesse quebrado
    public String handleUsernameOrPasswordInvalidException(UsernameOrPasswordInvalidException exception) {
        return exception.getMessage(); // o corpo da resposta: "Username or password incorrect"
    }
}
