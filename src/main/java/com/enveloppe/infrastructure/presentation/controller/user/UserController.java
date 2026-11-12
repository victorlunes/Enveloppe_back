package com.enveloppe.infrastructure.presentation.controller.user;

import com.enveloppe.core.useCases.users.updateEntry.UpdateEntryUseCase;
import com.enveloppe.infrastructure.security.JWTUserData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enveloppe/user")
public class UserController {

    private final UpdateEntryUseCase updateEntryUseCase;         // cadastro: a regra de negócio, lá do core
    //private final UserMapper userMapper;                       // cadastro: converte JSON ↔ User
    //private final UserEntityMapper userEntityMapper;           // login: converte o UserEntity que logou em User

    public UserController(
            UpdateEntryUseCase updateEntryUseCase
            //UserMapper userMapper,
            //UserEntityMapper userEntityMapper
    ){
        this.updateEntryUseCase = updateEntryUseCase;
        //this.userMapper = userMapper;
        //this.userEntityMapper = userEntityMapper;
    }

    @PostMapping("/last-entry")
    public ResponseEntity<String> lastEntryOnUpdate(@AuthenticationPrincipal JWTUserData user) {
        boolean result = updateEntryUseCase.execute(user.id());

        if (result){
            return ResponseEntity.status(HttpStatus.OK).body("Sequencia de uso do app atualizada com sucesso");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Sequencia de uso do app já esta atualizada");
    }
}
