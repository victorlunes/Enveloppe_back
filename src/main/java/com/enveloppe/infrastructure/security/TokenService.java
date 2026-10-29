package com.enveloppe.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.enveloppe.core.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

// Quem cria e quem confere o token JWT. Pensa no token como um crachá:
// o login entrega o crachá, e em toda requisição a API olha o crachá pra saber quem é você.
@Component // o Spring cria e entrega pra quem pedir: o AuthController (gera) e o SecurityFilter (confere)
public class TokenService {

    @Value("${enveloppe.security.secret}") // busca no application.yaml, que por sua vez lê o JWT_SECRET do .env
    private String secret; // a chave secreta da API. Vazou? Qualquer um gera um token válido e entra como quem quiser. Por isso fica no .env, nunca no código

    // Chamado no login, depois que email e senha já foram conferidos.
    public String generateToken(User user) {
        // Por que HMAC256? Porque ele assina com UMA chave só: a mesma secret que assina é a que confere.
        // Como quem cria e quem confere o token é a própria API, uma chave só resolve, e é rápido.
        // (Se outro sistema precisasse conferir o token sem poder criar, aí seria RSA, com chave pública e privada.)
        Algorithm algorithm = Algorithm.HMAC256(secret); // monta o "carimbo" com a nossa secret. Sem a secret, ninguém consegue carimbar igual

        return JWT
                .create()                                        // começa a montar o token
                .withSubject(user.email())                       // "sub" = de quem é o token. Aqui é o email, que é o login da pessoa
                .withClaim("userId", user.id())                  // claim = informação extra que viaja dentro do token. Assim a API sabe o id sem ir no banco
                .withClaim("name", user.name())                  // mais uma claim. Cuidado: qualquer um consegue LER o token (é só Base64), então senha aqui nunca
                .withExpiresAt(Instant.now().plusSeconds(86400)) // vence em 24h (86400 segundos). Se alguém roubar o token, ele não vale pra sempre
                .withIssuedAt(Instant.now())                     // quando o token foi criado
                .withIssuer("enveloppe")                         // quem emitiu: a nossa API
                .sign(algorithm);                                // carimba com a secret e gera a string final: cabeçalho.dados.assinatura
    }


    // Chamado pelo SecurityFilter em toda requisição que chega com token.
    // Optional porque tem dois finais possíveis: token bom (devolve quem é) ou token ruim (devolve vazio).
    public Optional<JWTUserData> verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret); // a MESMA secret do generateToken. Se a assinatura não bater com ela, é token falso

            DecodedJWT jwt = JWT.require(algorithm) // "só aceito token carimbado com essa chave"
                    .build()
                    .verify(token);                 // confere a assinatura E se não venceu. Falhou qualquer um, estoura JWTVerificationException

            Long userId = jwt.getClaim("userId").asLong(); // lê de volta as claims que a gente colocou no generateToken
            String name = jwt.getClaim("name").asString();
            String subject = jwt.getSubject();             // o email (o "sub")

            return Optional.of(
                    new JWTUserData(userId, name, subject) // token bom: empacota quem está logado
            );

        }catch (JWTVerificationException exception){ // token mexido, vencido ou qualquer lixo
            return Optional.empty();                 // não explode: só avisa "não tem ninguém logado aqui". Quem barra é o Spring Security depois
        }
    }
}
