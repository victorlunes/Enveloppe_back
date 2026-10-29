package com.enveloppe.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

// O "segurança da porta": toda requisição passa por aqui ANTES de chegar no controller.
// O trabalho dele é um só: "veio token? É válido? Então avisa o Spring quem é essa pessoa."
@Component // vira um bean pra gente conseguir injetar lá no SecurityConfig
public class SecurityFilter extends OncePerRequestFilter { // OncePerRequest = roda uma vez só por requisição, mesmo se o Spring redirecionar por dentro

    private final TokenService tokenService; // quem sabe conferir o token

    public SecurityFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization"); // o token chega no header assim: Authorization: Bearer eyJhbGci...

        if (Strings.isNotEmpty(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) { // só continua se veio o header e no formato "Bearer <token>", que é o padrão pra token
            String token = authorizationHeader.substring("Bearer ".length()); // corta o "Bearer " da frente e sobra só o token

            Optional<JWTUserData> optionalJWTUserData = tokenService.verifyToken(token); // confere assinatura e validade

            if (optionalJWTUserData.isPresent()) { // token bom
                JWTUserData jwtUserData = optionalJWTUserData.get(); // pega id, nome e email que estavam dentro do token

                UsernamePasswordAuthenticationToken authenticationToken = // o "objeto de logado" que o Spring Security entende
                        UsernamePasswordAuthenticationToken.authenticated( // authenticated = "esse aqui já foi conferido, pode confiar"
                                jwtUserData, // quem é (o Spring chama isso de principal)
                                null,        // a senha: não precisa mais, quem provou foi o token
                                List.of()    // permissões (ADMIN, USER...): ainda não temos, então vazio
                        );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken); // o pulo do gato: a partir daqui o Spring sabe que essa requisição está logada
            }

            filterChain.doFilter(request, response); // segue o caminho. Token ruim? Segue sem ninguém logado e o .authenticated() do SecurityConfig barra com 403
        } else {
            filterChain.doFilter(request, response); // sem token também segue. Login e cadastro passam (são permitAll), o resto vai ser barrado
        }
    }

}
