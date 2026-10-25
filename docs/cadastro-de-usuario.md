# Cadastro de usuário — enveloppe

Documentação de como a rotina de **cadastro de usuário** (`POST /enveloppe/auth/register`) foi construída no enveloppe, partindo do que existia no projeto **movieflix** e encaixando em **arquitetura limpa**.

> Status: cadastro pronto e compilando. Login (JWT) ainda não implementado — ver [Próximos passos](#11-próximos-passos-login).

---

## Sumário

1. [A ideia central: arquitetura limpa](#1-a-ideia-central-arquitetura-limpa)
2. [Estrutura de pastas](#2-estrutura-de-pastas)
3. [Do movieflix para o enveloppe](#3-do-movieflix-para-o-enveloppe)
4. [O fluxo de uma requisição](#4-o-fluxo-de-uma-requisição)
5. [Passo a passo da criação](#5-passo-a-passo-da-criação)
6. [Como o Spring monta tudo](#6-como-o-spring-monta-tudo)
7. [Decisões tomadas](#7-decisões-tomadas)
8. [Como testar](#8-como-testar)
9. [Como verificar que o core está limpo](#9-como-verificar-que-o-core-está-limpo)
10. [Glossário](#10-glossário)
11. [Próximos passos: login](#11-próximos-passos-login)

---

## 1. A ideia central: arquitetura limpa

O projeto é dividido em duas camadas:

| Camada | O que tem | Pode usar Spring / banco / web? |
|---|---|---|
| `core` | Regras de negócio: entidades, contratos (gateways) e casos de uso | **Não.** Só Java puro |
| `infrastructure` | Tudo que é detalhe técnico: controller, DTOs, SQL, BCrypt, configuração do Spring | **Sim** |

**A regra de dependência:** a `infrastructure` pode importar o `core`, mas o `core` **nunca** importa a `infrastructure`, nem `org.springframework`, nem `jakarta`.

Quando o core precisa de algo de fora (salvar no banco, criptografar uma senha), ele **declara uma interface** (gateway), e a infra **implementa** essa interface. Isso é a *inversão de dependência*:

```
movieflix:  UserService ──depende de──> UserRepository (JPA)            ← o negócio depende do banco

enveloppe:  CreateUserUseCase ──depende de──> UserGateway (core)
                                                  ↑ implementa
                                     UserRepositoryGateway (infra, SQL)  ← o banco depende do negócio
```

Consequência prática: o projeto saiu do JPA para SQL escrito à mão **sem mudar nenhuma linha do core**.

---

## 2. Estrutura de pastas

```
src/main/java/com/enveloppe/
├── core/                                         ← Java puro
│   ├── entities/
│   │   └── User.java                             entidade do domínio
│   ├── gateway/
│   │   ├── security/PasswordEncoderGateway.java  contrato: "criptografe esta senha"
│   │   └── users/UserGateway.java                contrato: "salve este usuário"
│   └── useCases/users/createUser/
│       ├── CreateUserUseCase.java                o que o sistema sabe fazer
│       └── CreateUserUseCaseImpl.java            a regra do cadastro
│
└── infrastructure/                               ← Spring, banco, web
    ├── beans/BeanConfiguration.java              registra os use cases no Spring
    ├── dtos/users/
    │   ├── user/UserRequest.java                 JSON de entrada do cadastro
    │   ├── user/UserResponse.java                JSON de saída do cadastro
    │   ├── login/LoginRequest.java               (login — ainda não usado)
    │   └── login/LoginResponse.java              (login — ainda não usado)
    ├── gateway/
    │   ├── security/BCryptPasswordEncoderGateway.java  implementa PasswordEncoderGateway
    │   └── users/UserRepositoryGateway.java            implementa UserGateway
    ├── mapper/users/
    │   ├── UserMapper.java                       JSON ↔ core
    │   └── UserEntityMapper.java                 core ↔ banco
    ├── persistence/users/
    │   ├── entities/UserEntity.java              uma linha da tabela elp_users
    │   └── repository/UserRepository.java        o SQL (JdbcClient)
    ├── presentation/controller/auth/AuthController.java   endpoint /register
    └── security/SecurityConfig.java              libera rotas e fornece o BCrypt

src/main/resources/
├── application.yaml
└── db/migration/V1__create_table_users.sql       criação da tabela elp_users
```

Repare no espelhamento: `core/gateway/users` ↔ `infrastructure/gateway/users` e `core/gateway/security` ↔ `infrastructure/gateway/security`. Cada contrato do core tem sua implementação na infra, na pasta de mesmo nome.

---

## 3. Do movieflix para o enveloppe

No movieflix (monolito) uma mesma classe fazia vários papéis. No enveloppe cada papel virou uma peça, na camada certa:

| movieflix | enveloppe | Camada |
|---|---|---|
| `entity/user/User` (JPA + `UserDetails`) | `core/entities/User` (record puro) | core |
| — (era a mesma classe acima) | `persistence/users/entities/UserEntity` | infra |
| `service/user/UserService.registerUser` | `core/useCases/users/createUser/CreateUserUseCaseImpl` | core |
| `userRepository.save(...)` dentro do service | `core/gateway/users/UserGateway` → `infrastructure/gateway/users/UserRepositoryGateway` | core → infra |
| `passwordEncoder.encode(...)` dentro do service | `core/gateway/security/PasswordEncoderGateway` → `infrastructure/gateway/security/BCryptPasswordEncoderGateway` | core → infra |
| `repository/user/UserRepository` (`JpaRepository`) | `persistence/users/repository/UserRepository` (SQL com `JdbcClient`) | infra |
| `mapper/user/UserMapper` (request ↔ entidade JPA) | `UserMapper` (JSON ↔ core) + `UserEntityMapper` (core ↔ banco) | infra |
| `@Service` no service | `infrastructure/beans/BeanConfiguration` | infra |
| `config/SecurityConfig` | `infrastructure/security/SecurityConfig` | infra |
| `controller/user/AuthController` | `infrastructure/presentation/controller/auth/AuthController` | infra |
| `db/migration/V7__create_table_users.sql` (MySQL) | `db/migration/V1__create_table_users.sql` (Postgres) | infra |

---

## 4. O fluxo de uma requisição

Exemplo de requisição:

```http
POST /enveloppe/auth/register
Content-Type: application/json

{"name":"Teste","email":"teste@teste.com","birthday":"2000-05-10","password":"123456"}
```

O caminho que ela percorre:

```
 ┌──────────────────────────── infrastructure ────────────────────────────┐
 │                                                                        │
 │  1. SecurityConfig          a rota /register é permitAll → deixa passar │
 │  2. AuthController.register  o JSON vira UserRequest                    │
 │  3. UserMapper.toUser        UserRequest → User (id = null)             │
 │                                                                        │
 └───────────────────────────────────┬────────────────────────────────────┘
                                     ▼
 ┌──────────────────────────────── core ──────────────────────────────────┐
 │                                                                        │
 │  4. CreateUserUseCaseImpl.execute                                      │
 │       4a. passwordEncoderGateway.encode("123456") → "$2a$10$..."       │
 │       4b. new User(..., senha em hash)                                 │
 │       4c. userGateway.createUser(user)                                 │
 │                                                                        │
 └───────────────────────────────────┬────────────────────────────────────┘
                                     ▼
 ┌──────────────────────────── infrastructure ────────────────────────────┐
 │                                                                        │
 │  4a ─► BCryptPasswordEncoderGateway.encode → BCrypt do Spring          │
 │  4c ─► UserRepositoryGateway.createUser                                │
 │          5. UserEntityMapper.toUserEntity   User → UserEntity          │
 │          6. UserRepository.save             INSERT INTO elp_users ...  │
 │          7. UserEntityMapper.toUser         UserEntity → User          │
 │                                                                        │
 │  8. UserMapper.toUserResponse   User → UserResponse (name, email)      │
 │  9. AuthController devolve 201 Created                                 │
 │                                                                        │
 └────────────────────────────────────────────────────────────────────────┘
```

Resposta:

```http
HTTP/1.1 201 Created

{"name":"Teste","email":"teste@teste.com"}
```

O que entra e o que sai de cada camada:

| Objeto | Quem conhece | Onde aparece |
|---|---|---|
| `UserRequest` / `UserResponse` | só a infra (web) | controller e `UserMapper` |
| `User` | core e infra | tudo que passa pelo use case |
| `UserEntity` | só a infra (banco) | `UserRepositoryGateway`, `UserEntityMapper`, `UserRepository` |

O core só vê `User` entrando e saindo. Ele não sabe que JSON, `UserEntity` ou SQL existem.

---

## 5. Passo a passo da criação

A ordem seguida foi **de dentro para fora**: primeiro os dados e o core, depois a infra que implementa os contratos e, por último, a entrada web.

### Passo 1 — DTOs (o formato do JSON)

**Arquivos:** [`UserRequest.java`](../src/main/java/com/enveloppe/infrastructure/dtos/users/user/UserRequest.java), [`UserResponse.java`](../src/main/java/com/enveloppe/infrastructure/dtos/users/user/UserResponse.java)
**movieflix:** `request/user/UserRequest.java`, `response/user/UserResponse.java`

DTO (*Data Transfer Object*) é o molde do JSON que entra e sai da API. Fica na infra porque o formato do JSON é detalhe da web.

```java
public record UserRequest(String name, String email, LocalDate birthday, String password) { }
public record UserResponse(String name, String email) { }
```

- O Spring (Jackson) transforma o JSON recebido em `UserRequest` casando cada campo pelo nome.
- O `UserResponse` não tem `password` (nunca sai na resposta), nem `birthday`, nem `id` (ver [Decisões](#7-decisões-tomadas)).

### Passo 2 — Entidade do domínio (`User`)

**Arquivo:** [`core/entities/User.java`](../src/main/java/com/enveloppe/core/entities/User.java)
**movieflix:** `entity/user/User.java`, mas sem JPA e sem Spring

```java
public record User(Long id, String name, String email, LocalDate birthday, String password) { }
```

- É o usuário do ponto de vista do negócio: só dados, sem `@Entity`, sem nada do Spring.
- É `record`, portanto **imutável**: não tem setters. Para mudar algo (como trocar a senha pelo hash) cria-se um `User` novo.
- `java.time.LocalDate` é do próprio Java, então pode ficar no core.

### Passo 3 — Entidade do banco (`UserEntity`) e saída do JPA/Lombok

**Arquivos:** [`UserEntity.java`](../src/main/java/com/enveloppe/infrastructure/persistence/users/entities/UserEntity.java), `pom.xml`, `application.yaml`
**movieflix:** a parte JPA de `entity/user/User.java`

O projeto **não usa JPA nem Lombok**. Mudanças feitas:

- `pom.xml`: `spring-boot-starter-data-jpa` → `spring-boot-starter-jdbc` (e o `-test` correspondente); removida a dependência do Lombok e o bloco do `maven-compiler-plugin` que existia só para ele.
- `application.yaml`: o `jpa.show-sql` saiu; para ver os SQLs no terminal entrou, **na raiz do arquivo** (fora do `spring:`):

  ```yaml
  logging:
    level:
      org.springframework.jdbc.core: DEBUG
  ```

- `UserEntity` virou uma classe Java simples:

  ```java
  public class UserEntity {
      private final Long id;
      private final String name;
      private final String email;
      private final LocalDate birthday;
      private final String password;

      public UserEntity(Long id, String name, String email, LocalDate birthday, String password) { ... }

      public Long getId() { ... }
      // getName(), getEmail(), getBirthday(), getPassword()
  }
  ```

- O **construtor** substitui o `@AllArgsConstructor`; os **getters** substituem o `@Data`.
- Sem setters e sem construtor vazio: eles só existiam porque o JPA precisava. O objeto nasce completo e não muda (campos `final`).
- Por que existem `User` **e** `UserEntity`? O `User` é do negócio e não pode conhecer nada do Spring. A `UserEntity` é o "usuário da infra" — é ela que, no login, vai implementar o `UserDetails` do Spring Security.

### Passo 4 — Migration da tabela

**Arquivo:** [`V1__create_table_users.sql`](../src/main/resources/db/migration/V1__create_table_users.sql)
**movieflix:** `db/migration/V7__create_table_users.sql` (MySQL)

```sql
CREATE TABLE elp_users
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    birthday DATE,
    password VARCHAR(255) NOT NULL
);
```

- O **Flyway** roda este arquivo **uma única vez**, ao subir a aplicação, e registra isso na tabela `flyway_schema_history`.
- Nome do arquivo: `V1` (versão) + `__` (dois underlines) + descrição. As versões rodam em ordem.
- `BIGSERIAL` é o equivalente Postgres ao `AUTO_INCREMENT` do MySQL: o banco gera o `id`.
- `DATE` guarda só a data e casa com `LocalDate`.
- `birthday` aceita `NULL` (aniversário opcional).
- **Regra de ouro:** migration que já rodou **nunca é editada**. Para mudar a tabela, cria-se uma `V2__...sql`. Se a V1 for alterada, o Flyway detecta pelo checksum e a aplicação não sobe.

### Passo 5 — Contrato de persistência (`UserGateway`)

**Arquivo:** [`core/gateway/users/UserGateway.java`](../src/main/java/com/enveloppe/core/gateway/users/UserGateway.java)
**movieflix:** não existe — o `UserService` chamava o `UserRepository` direto

```java
public interface UserGateway {
    User createUser(User user);
}
```

- É um **contrato**: o core diz *o que* precisa ("salvar um usuário"), sem dizer *como*.
- Recebe e devolve `User` do core — nunca `UserEntity`.
- Quem cumpre o contrato é o `UserRepositoryGateway` (Passo 9).

### Passo 6 — Contrato de senha (`PasswordEncoderGateway`)

**Arquivo:** [`core/gateway/security/PasswordEncoderGateway.java`](../src/main/java/com/enveloppe/core/gateway/security/PasswordEncoderGateway.java)
**movieflix:** não existe — o `UserService` usava o `PasswordEncoder` do Spring direto

```java
public interface PasswordEncoderGateway {
    String encode(String rawPassword);
}
```

- `rawPassword` é a senha como o usuário digitou; o retorno é o hash.
- **Por que o contrato está no core:** "nunca guardar senha em texto puro" é uma regra do cadastro, e a regra mora no use case. Como o use case (core) não pode importar o BCrypt (Spring), o core declara a interface e a infra implementa. A *implementação* (BCrypt) continua na infra.
- **Regra geral:** a interface mora junto de quem **usa**, não de quem implementa.
- Hash é de mão única: dá para ir de `"123456"` ao hash, mas não voltar. No login o Spring não "descriptografa": aplica o mesmo cálculo e compara.

### Passo 7 — Caso de uso (`CreateUserUseCase`)

**Arquivos:** [`CreateUserUseCase.java`](../src/main/java/com/enveloppe/core/useCases/users/createUser/CreateUserUseCase.java), [`CreateUserUseCaseImpl.java`](../src/main/java/com/enveloppe/core/useCases/users/createUser/CreateUserUseCaseImpl.java)
**movieflix:** `service/user/UserService.registerUser`

```java
public interface CreateUserUseCase {
    User execute(User user);
}
```

```java
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserGateway userGateway;
    private final PasswordEncoderGateway passwordEncoderGateway;

    // construtor recebe os dois gateways

    @Override
    public User execute(User user) {
        String encodedPassword = passwordEncoderGateway.encode(user.password());

        User userWithEncodedPassword = new User(
                user.id(), user.name(), user.email(), user.birthday(), encodedPassword
        );

        return userGateway.createUser(userWithEncodedPassword);
    }
}
```

Comparação com o movieflix:

| movieflix (`UserService`) | enveloppe (`CreateUserUseCaseImpl`) | Por quê |
|---|---|---|
| `userMapper.toUser(userRequest)` | não está aqui | converter JSON é papel do controller |
| `newUser.setPassword(passwordEncoder.encode(...))` | `passwordEncoderGateway.encode(...)` + `new User(...)` | `User` é record, sem setter |
| `userRepository.save(newUser)` | `userGateway.createUser(...)` | fala com o contrato, não com o banco |
| `return userMapper.toUserResponse(newUser)` | `return` do `User` | montar a resposta é papel do controller |

- **Sem `@Service`**: é Java puro. Quem registra a classe no Spring é o `BeanConfiguration` (Passo 11).
- O `user.birthday()` precisa ser repassado no `new User(...)`; se esquecido, chega `null` no banco sem erro (a coluna aceita `NULL`).

Com este passo o **core do cadastro está completo**. Daqui em diante é tudo infra.

### Passo 8 — Mapper core ↔ banco (`UserEntityMapper`)

**Arquivo:** [`UserEntityMapper.java`](../src/main/java/com/enveloppe/infrastructure/mapper/users/UserEntityMapper.java)
**movieflix:** não existe (lá a entidade JPA e a do domínio eram a mesma classe)

```java
@Component
public class UserEntityMapper {
    public UserEntity toUserEntity(User user) { ... }   // core → banco (antes do INSERT)
    public User toUser(UserEntity userEntity) { ... }   // banco → core (depois do INSERT/SELECT)
}
```

- Traduz entre `User` (record, `user.name()`) e `UserEntity` (classe, `getName()`).
- A ordem dos argumentos precisa bater com o construtor `(id, name, email, birthday, password)`. Como `name` e `email` são ambos `String`, trocar a ordem **não dá erro de compilação** — o dado só vai parar na coluna errada.
- `@Component` faz o Spring criar o objeto e injetá-lo onde for pedido. Pode ter anotação do Spring porque está na infra. A infra pode importar o core; o contrário é proibido.

### Passo 9 — SQL na mão e o gateway de usuário

**Arquivos:** [`UserRepository.java`](../src/main/java/com/enveloppe/infrastructure/persistence/users/repository/UserRepository.java), [`UserRepositoryGateway.java`](../src/main/java/com/enveloppe/infrastructure/gateway/users/UserRepositoryGateway.java)
**movieflix:** `repository/user/UserRepository` (`JpaRepository`) e a linha `userRepository.save(newUser)`

**9.1 — `UserRepository`, o SQL:**

```java
@Repository
public class UserRepository {

    private final JdbcClient jdbcClient;

    public UserEntity save(UserEntity userEntity) {
        jdbcClient.sql("""
                    INSERT INTO elp_users (name, email, birthday, password)
                    VALUES (:name, :email, :birthday, :password)
                    """)
                .param("name", userEntity.getName())
                .param("email", userEntity.getEmail())
                .param("birthday", userEntity.getBirthday())
                .param("password", userEntity.getPassword())
                .update();

        return userEntity;
    }
}
```

- Deixou de ser `interface extends JpaRepository` e virou `class`: sem JPA, ninguém gera o código — o SQL é escrito à mão.
- `@Repository`: igual ao `@Component`, mas sinaliza acesso a banco.
- `JdbcClient`: ferramenta do Spring para executar SQL. O Spring Boot cria uma instância ligada ao `datasource` do `application.yaml`. Ele cuida de abrir/fechar conexão; o SQL é todo seu.
- `"""..."""`: *text block* do Java — string de várias linhas.
- `:name`, `:email`...: **parâmetros nomeados**, preenchidos por `.param(...)`. **Nunca** concatenar valores na string do SQL (`"VALUES ('" + name + "')"`) — isso abre brecha para *SQL injection*.
- `id` fica fora do `INSERT` porque o `BIGSERIAL` gera.
- `.update()` executa `INSERT`/`UPDATE`/`DELETE` e devolve quantas linhas foram afetadas.
- Retorna a própria `userEntity`, **sem `id`** (ver [Decisões](#7-decisões-tomadas)).

**9.2 — `UserRepositoryGateway`, o adaptador:**

```java
@Component
public class UserRepositoryGateway implements UserGateway {

    @Override
    public User createUser(User user) {
        UserEntity userEntity = userEntityMapper.toUserEntity(user);
        UserEntity savedUser = userRepository.save(userEntity);
        return userEntityMapper.toUser(savedUser);
    }
}
```

- `implements UserGateway`: cumpre o contrato do Passo 5. Quando o use case chama `userGateway.createUser(...)`, é este método que roda.
- Padrão de todo método de gateway: **core → banco, chama o repositório, banco → core**.
- Por que duas classes? O repositório só sabe SQL e `UserEntity`; o gateway é a ponte que fala `User` com o core.

### Passo 10 — Segurança: `SecurityConfig` e `BCryptPasswordEncoderGateway`

**Arquivos:** [`SecurityConfig.java`](../src/main/java/com/enveloppe/infrastructure/security/SecurityConfig.java), [`BCryptPasswordEncoderGateway.java`](../src/main/java/com/enveloppe/infrastructure/gateway/security/BCryptPasswordEncoderGateway.java)
**movieflix:** `config/SecurityConfig.java` (só a parte do cadastro) e a linha `passwordEncoder.encode(...)`

**10.1 — `SecurityConfig`:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/enveloppe/auth/register").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

| Linha | Significado |
|---|---|
| `@Configuration` | a classe tem métodos `@Bean`, rodados ao subir a aplicação |
| `@EnableWebSecurity` | liga a segurança web com este `SecurityFilterChain` |
| `csrf.disable()` | CSRF protege sites com formulário + cookie de sessão; API REST com token não precisa, e com ele ligado o `POST` seria recusado |
| `STATELESS` | o servidor não guarda sessão; cada request prova sozinha quem é (no login, via JWT) |
| `DispatcherType.ERROR` permitAll | sem isso, todo erro vira um `403` vazio que esconde o problema real |
| `/register` permitAll | qualquer um pode se cadastrar sem token |
| `anyRequest().authenticated()` | todo o resto exige autenticação |
| `passwordEncoder()` | cria o BCrypt e o guarda no container com o tipo `PasswordEncoder` |

Sem este arquivo, o Spring Security bloqueia **todas** as rotas por padrão — inclusive o cadastro.

**10.2 — `BCryptPasswordEncoderGateway`:**

```java
@Component
public class BCryptPasswordEncoderGateway implements PasswordEncoderGateway {

    private final PasswordEncoder passwordEncoder;   // o BCrypt criado no SecurityConfig

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
```

- Cumpre o contrato do Passo 6, traduzindo-o para a ferramenta do Spring.
- Recebe o `PasswordEncoder` pelo construtor: o Spring encontra o BCrypt que o `SecurityConfig` criou.

### Passo 11 — `BeanConfiguration`

**Arquivo:** [`BeanConfiguration.java`](../src/main/java/com/enveloppe/infrastructure/beans/BeanConfiguration.java)
**movieflix:** não existe — o `@Service` resolvia sozinho

```java
@Configuration
public class BeanConfiguration {

    @Bean
    public CreateUserUseCase createUserUseCase(
            UserGateway userGateway,
            PasswordEncoderGateway passwordEncoderGateway
    ) {
        return new CreateUserUseCaseImpl(userGateway, passwordEncoderGateway);
    }
}
```

- O `CreateUserUseCaseImpl` não tem anotação, então o Spring não sabe que ele existe. Sem este método, a aplicação não sobe:
  `No qualifying bean of type 'CreateUserUseCase' available`.
- O **tipo de retorno** (`CreateUserUseCase`, a interface) é a "etiqueta" do bean — é por ele que o controller recebe o objeto.
- Os **parâmetros** o Spring preenche sozinho, procurando um bean de cada tipo: `UserGateway` → `UserRepositoryGateway`; `PasswordEncoderGateway` → `BCryptPasswordEncoderGateway`.
- É o único lugar onde core e infra são ligados (*composition root*).
- **Regra:** cada novo use case do core ganha um `@Bean` aqui.

### Passo 12 — Entrada da API: `UserMapper` e `AuthController`

**Arquivos:** [`UserMapper.java`](../src/main/java/com/enveloppe/infrastructure/mapper/users/UserMapper.java), [`AuthController.java`](../src/main/java/com/enveloppe/infrastructure/presentation/controller/auth/AuthController.java)
**movieflix:** `mapper/user/UserMapper.java` e `controller/user/AuthController.register`

**12.1 — `UserMapper` (JSON ↔ core):**

```java
@Component
public class UserMapper {

    public User toUser(UserRequest userRequest) {
        return new User(null, userRequest.name(), userRequest.email(),
                        userRequest.birthday(), userRequest.password());
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(user.name(), user.email());
    }
}
```

- `null` no `id`: quem chega pela API ainda não tem `id`.
- `toUserResponse` decide o que é exposto: só `name` e `email`. Quem escolhe o que sai é o mapper, não o core.
- Dois mappers, duas fronteiras: `UserMapper` (web ↔ core) e `UserEntityMapper` (core ↔ banco).

**12.2 — `AuthController`:**

```java
@RestController
@RequestMapping("/enveloppe/auth")
public class AuthController {

    private final CreateUserUseCase createUserUseCase;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest userRequest) {
        User user = userMapper.toUser(userRequest);
        User newUser = createUserUseCase.execute(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toUserResponse(newUser));
    }
}
```

- `@RestController` + `@RequestMapping`: responde às URLs que começam com `/enveloppe/auth`.
- `@PostMapping("/register")` + `@RequestBody`: o JSON do corpo vira `UserRequest`.
- O controller faz só três coisas: **traduz a entrada, chama o use case, traduz a saída**. Não tem regra de negócio e não conhece gateway, repositório nem BCrypt.
- `HttpStatus.CREATED` = `201`, o status de "recurso criado".

---

## 6. Como o Spring monta tudo

Pense no Spring como um armário de objetos (o *container*). Ao subir, ele cria cada bean e o guarda com uma etiqueta: o seu **tipo**. Quando uma classe pede algo no construtor, ele procura no armário um objeto daquele tipo.

| Etiqueta (tipo) | Objeto guardado | De onde veio |
|---|---|---|
| `JdbcClient` | criado pelo Spring Boot | `spring-boot-starter-jdbc` + `datasource` |
| `UserRepository` | `new UserRepository(jdbcClient)` | `@Repository` |
| `UserEntityMapper` / `UserMapper` | `new ...()` | `@Component` |
| `UserGateway` | `new UserRepositoryGateway(...)` | `@Component` + `implements UserGateway` |
| `PasswordEncoder` | `new BCryptPasswordEncoder()` | `@Bean` no `SecurityConfig` |
| `PasswordEncoderGateway` | `new BCryptPasswordEncoderGateway(...)` | `@Component` + `implements PasswordEncoderGateway` |
| `CreateUserUseCase` | `new CreateUserUseCaseImpl(...)` | `@Bean` no `BeanConfiguration` |
| `AuthController` | `new AuthController(...)` | `@RestController` |

Ordem de montagem (o Spring resolve sozinho, criando primeiro o que cada um pede):

```
JdbcClient → UserRepository ─┐
UserEntityMapper ────────────┴→ UserRepositoryGateway (UserGateway) ─────┐
SecurityConfig → PasswordEncoder → BCryptPasswordEncoderGateway ─────────┴→ CreateUserUseCaseImpl (BeanConfiguration)
                                                                                    │
UserMapper ─────────────────────────────────────────────────────────────────────────┴→ AuthController
```

---

## 7. Decisões tomadas

| Decisão | Motivo |
|---|---|
| **Sem JPA — SQL à mão com `JdbcClient`** | Aprender o SQL de verdade. Só a infra mudou; o core ficou intacto. `JdbcClient` foi escolhido como meio-termo: o SQL é todo escrito à mão, mas o Spring cuida de abrir/fechar conexão (ao contrário do JDBC puro). |
| **Sem Lombok** | Construtor e getters escritos à mão em `UserEntity`. Sem setters: os campos são `final`. |
| **`LocalDate` em vez de `java.util.Date`** | `Date` é um instante (data + hora + fuso) e é mutável. Com fuso (UTC-3), `2000-05-10` pode virar `2000-05-09` no banco. `LocalDate` é só a data, imutável, e mapeia direto para `DATE`. |
| **`birthday` opcional na tabela** | Coluna sem `NOT NULL`. |
| **`birthday` não volta na resposta** | `UserResponse` só tem `name` e `email`. |
| **`INSERT` sem retornar o `id`** | O cadastro não precisa devolver o `id`, então o `INSERT` é puro (`.update()`) e o `User` retornado tem `id = null`. Por isso o `id` também saiu do `UserResponse`. O login não é afetado: ele vai buscar o usuário com `SELECT ... WHERE email = ...`, e aí o `id` vem do banco. Alternativas, se um dia precisar do `id`: `RETURNING id` (Postgres) ou `KeyHolder` (JDBC, qualquer banco). |
| **`PasswordEncoderGateway` no core** | A regra "senha nunca em texto puro" fica visível no use case. Alternativa válida (não adotada): criptografar dentro do `UserRepositoryGateway`, o que esconderia a regra na infra. |
| **Login removido do controller por enquanto** | O código antigo usava classes que ainda não existem. Volta na fase de login via `LoginUseCase`. |

---

## 8. Como testar

Na raiz do projeto:

```bash
./mvnw compile
```

```bash
docker compose up -d
```

```bash
./mvnw spring-boot:run
```

No log, procure `Migrating schema "public" to version "1 - create table users"` (Flyway criou a tabela) e `Tomcat started on port 8080`.

Em outro terminal:

```bash
curl -i -X POST localhost:8080/enveloppe/auth/register -H "Content-Type: application/json" -d '{"name":"Teste","email":"teste@teste.com","birthday":"2000-05-10","password":"123456"}'
```

Esperado: `HTTP/1.1 201` com `{"name":"Teste","email":"teste@teste.com"}`. O log `DEBUG` mostra o `INSERT INTO elp_users ...`.

No banco:

```sql
SELECT id, name, email, birthday, password FROM elp_users;
```

Esperado: `id` gerado, `birthday = 2000-05-10` e `password` começando com `$2a$10$...` (nunca `123456`).

Experimentos úteis para aprender:

- Cadastre dois usuários com a mesma senha: os hashes saem **diferentes** (o BCrypt usa *salt*).
- Remova o `permitAll` do `/register`: a resposta vira `403`.
- Comente o `@Bean` do `BeanConfiguration`: a aplicação não sobe (`No qualifying bean of type 'CreateUserUseCase'`).

---

## 9. Como verificar que o core está limpo

Este comando, rodado na raiz do projeto, precisa retornar **vazio**. Se aparecer alguma linha, o core está dependendo de algo de fora:

```bash
grep -rnE "import (org\.springframework|jakarta|com\.enveloppe\.infrastructure)" src/main/java/com/enveloppe/core
```

---

## 10. Glossário

| Termo | Significado |
|---|---|
| **Arquitetura limpa** | Organização em que as regras de negócio (core) não dependem de detalhes técnicos (framework, banco, web). |
| **Core** | Camada do negócio, em Java puro. |
| **Infrastructure** | Camada dos detalhes técnicos (Spring, SQL, HTTP). |
| **Entidade (`User`)** | Representação do conceito do negócio. |
| **Gateway (core)** | Interface que o core declara quando precisa de algo de fora. Também chamado de *porta*. |
| **Gateway (infra)** | Classe que implementa a interface do core usando uma ferramenta concreta. Também chamado de *adaptador*. |
| **Caso de uso** | Uma ação que o sistema sabe fazer (ex.: cadastrar usuário). Contém a regra. |
| **Inversão de dependência** | O negócio depende de uma abstração (interface do core), e o detalhe técnico implementa essa abstração. |
| **DTO** | Objeto que representa o formato do JSON de entrada/saída. |
| **Mapper** | Classe que converte um objeto em outro entre camadas. |
| **Bean** | Objeto criado e gerenciado pelo Spring. |
| **Injeção de dependência** | O Spring entrega os objetos que uma classe pede no construtor. |
| **Composition root** | Único lugar onde as peças são ligadas (aqui, o `BeanConfiguration`). |
| **Migration** | Script SQL versionado que o Flyway executa uma vez para evoluir o banco. |
| **Hash / BCrypt** | Transformação de mão única da senha; o BCrypt adiciona um *salt* aleatório a cada hash. |
| **SQL injection** | Ataque em que o valor enviado vira parte do comando SQL. Evitado com parâmetros nomeados. |
| **Stateless** | O servidor não guarda sessão entre requisições. |

---

## 11. Próximos passos: login

O login segue o mesmo padrão, partindo do movieflix (`AuthService`, `TokenService`, `SecurityFilter`, `JWTUserData`, `UsernameOrPasswordInvalidException`):

1. Dependência `com.auth0:java-jwt` e o segredo `enveloppe.security.secret` (`SECRET` no `.env`).
2. `UserEntity implements UserDetails` (`getUsername()` retornando o email).
3. `UserRepository.findByEmail` com `SELECT` à mão.
4. Infra de segurança: `AuthService` (`UserDetailsService`), `TokenService`, `JWTUserData`, `SecurityFilter`; `SecurityConfig` com `AuthenticationManager`, `permitAll` do `/login` e o filtro.
5. Core: `core/exceptions/UsernameOrPasswordInvalidException`, `AuthGateway` e `LoginUseCase` + `Impl`.
6. Infra: `AuthenticationGateway` (o `try/catch` que no movieflix ficava no controller), `@Bean` do `LoginUseCase`, endpoint `/login` usando `LoginRequest`, e um `@RestControllerAdvice` que transforma a exceção em `401`.
