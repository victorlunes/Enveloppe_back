package com.enveloppe.infrastructure.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration      // classe de configuração: o Spring lê os @Bean daqui quando a aplicação sobe
@EnableWebSecurity  // liga o Spring Security com as NOSSAS regras, no lugar das regras padrão
public class SecurityConfig {

    private final SecurityFilter securityFilter; // nosso filtro que lê o token

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }


    // A "portaria" da API: toda requisição passa por essas regras antes de chegar no controller.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // CSRF é um ataque que se aproveita do cookie que o navegador manda sozinho. Nosso token vai no header, que o navegador NÃO manda sozinho, então não precisa. E ligado, ele barraria todo POST
                .sessionManagement(
                        sessionManagement
                                -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // STATELESS = a API não guarda sessão, não lembra de ninguém. É por isso que toda requisição precisa trazer o token
                )
                .authorizeHttpRequests(authorize -> authorize // regras por rota, lidas de cima pra baixo. A primeira que bater, vale
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll() // quando dá erro, o Spring joga pra /error por dentro. Sem isso, o próprio erro seria barrado e você veria 403 no lugar do erro de verdade
                        // Por que essas duas rotas não pedem token? Porque são justamente as portas de quem AINDA NÃO TEM token.
                        // E por que só POST? Porque é o método que elas usam. Libero só o mínimo: um GET nesse mesmo endereço continua pedindo token.
                        .requestMatchers(HttpMethod.POST, "/enveloppe/auth/register").permitAll() // cadastro: a pessoa nem tem conta ainda, como ia ter token?
                        .requestMatchers(HttpMethod.POST, "/enveloppe/auth/login").permitAll()    // login: é exatamente aqui que a pessoa GANHA o token
                        .anyRequest().authenticated() // todo o resto exige token válido. Por quê? A API é stateless: não lembra que você fez login, então você prova quem é a cada requisição
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class) // coloca nosso filtro ANTES do filtro padrão do Spring. Assim, quando o Spring for checar "tá logado?", o token já foi lido
                .build();
    }

    // O AuthController precisa do AuthenticationManager pra conferir email e senha no login.
    // O Spring já cria ele por dentro, mas não deixa disponível pra injetar. Esse @Bean expõe ele.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager(); // já vem montado com o nosso AuthService (busca no banco) e o PasswordEncoder aqui de baixo (confere a senha)
    }

    // O BCrypt trabalha nas DUAS rotinas:
    // cadastro → encode("123456") gera o hash pra salvar | login → matches("123456", hash) confere a senha digitada
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // mesma senha gera hash diferente toda vez, e não tem como desfazer. Se o banco vazar, as senhas não vazam junto
    }
}
