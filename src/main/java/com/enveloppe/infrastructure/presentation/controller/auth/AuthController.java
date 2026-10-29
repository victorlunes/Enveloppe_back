package com.enveloppe.infrastructure.presentation.controller.auth;

import com.enveloppe.core.entities.User;
import com.enveloppe.core.useCases.users.createUser.CreateUserUseCase;
import com.enveloppe.infrastructure.dtos.users.login.LoginRequest;
import com.enveloppe.infrastructure.dtos.users.login.LoginResponse;
import com.enveloppe.infrastructure.dtos.users.user.UserRequest;
import com.enveloppe.infrastructure.dtos.users.user.UserResponse;
import com.enveloppe.infrastructure.mapper.users.UserEntityMapper;
import com.enveloppe.infrastructure.mapper.users.UserMapper;
import com.enveloppe.infrastructure.persistence.users.entities.UserEntity;
import com.enveloppe.infrastructure.presentation.exception.UsernameOrPasswordInvalidException;
import com.enveloppe.infrastructure.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController                    // recebe requisição HTTP e devolve JSON
@RequestMapping("/enveloppe/auth") // toda rota daqui começa com /enveloppe/auth
public class AuthController {

    private final CreateUserUseCase createUserUseCase;         // cadastro: a regra de negócio, lá do core
    private final UserMapper userMapper;                       // cadastro: converte JSON ↔ User
    private final UserEntityMapper userEntityMapper;           // login: converte o UserEntity que logou em User
    private final AuthenticationManager authenticationManager; // login: quem confere email e senha
    private final TokenService tokenService;                   // login: quem gera o token

    public AuthController(
            CreateUserUseCase createUserUseCase,
            UserMapper userMapper,
            UserEntityMapper userEntityMapper,
            AuthenticationManager authenticationManager,
            TokenService tokenService
    ) {
        this.createUserUseCase = createUserUseCase;
        this.userMapper = userMapper;
        this.userEntityMapper = userEntityMapper;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/register") // POST porque estamos CRIANDO algo: um usuário novo
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest userRequest) { // @RequestBody: o Spring pega o JSON do corpo e transforma em UserRequest sozinho
        User user = userMapper.toUser(userRequest);     // JSON (infra) → User (core). O core não pode conhecer DTO, por isso converte antes
        User newUser = createUserUseCase.execute(user); // chama a regra de negócio: criptografa a senha e salva no banco
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toUserResponse(newUser)); // 201 = "criei". E devolve só nome e email, senha nunca
    }

    @PostMapping("/login") // POST porque email e senha vão no corpo. Num GET iriam na URL e ficariam em log, histórico do navegador...
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) { // JSON { email, password } → LoginRequest
        try {
            UsernamePasswordAuthenticationToken userAndPass = // empacota o que a pessoa digitou. Ainda NÃO está autenticado: é só "fulano diz que a senha é essa"
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

            // Aqui o Spring faz o trabalho pesado:
            // 1. chama o AuthService.loadUserByUsername(email) → busca o usuário no banco
            // 2. compara a senha digitada com o hash do banco usando o BCrypt
            // 3. bateu? Devolve o Authentication. Não bateu? Estoura BadCredentialsException
            Authentication authentication = authenticationManager.authenticate(userAndPass);

            UserEntity userEntity = (UserEntity) authentication.getPrincipal(); // principal = quem logou. É UserEntity porque foi isso que o AuthService devolveu
            User user = userEntityMapper.toUser(userEntity);                    // UserEntity → User, porque o TokenService trabalha com o User do core

            String token = tokenService.generateToken(user); // login certo: gera o crachá (JWT) dessa pessoa

            return ResponseEntity.ok(new LoginResponse(token)); // 200 com { "token": "eyJ..." }. Daqui pra frente o cliente manda esse token em toda requisição
        }catch (BadCredentialsException e) { // senha errada OU email que não existe. O Spring trata os dois igual de propósito
            throw new UsernameOrPasswordInvalidException("Username or password incorrect"); // nossa exceção. O ApplicationControllerAdvice transforma ela em 401
        }
    }
}
