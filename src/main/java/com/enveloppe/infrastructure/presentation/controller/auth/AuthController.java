package com.enveloppe.infrastructure.presentation.controller.auth;

import com.enveloppe.core.entities.User;
import com.enveloppe.core.useCases.users.createUser.CreateUserUseCase;
import com.enveloppe.infrastructure.dtos.users.user.UserRequest;
import com.enveloppe.infrastructure.dtos.users.user.UserResponse;
import com.enveloppe.infrastructure.mapper.users.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enveloppe/auth")
public class AuthController {

    private final CreateUserUseCase createUserUseCase;
    private final UserMapper userMapper;

    public AuthController(CreateUserUseCase createUserUseCase, UserMapper userMapper) {
        this.createUserUseCase = createUserUseCase;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest userRequest) {
        User user = userMapper.toUser(userRequest);
        User newUser = createUserUseCase.execute(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toUserResponse(newUser));
    }
}