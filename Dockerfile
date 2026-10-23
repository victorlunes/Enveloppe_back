# MEU PC = meu Mac, a pasta do projeto
# DOCKER = dentro da imagem/container

# ===== PARTE 1: gera o .jar =====

# DOCKER: usa uma imagem que já tem Maven + Java 21 para compilar o projeto
FROM maven:3.9.9-eclipse-temurin-21 AS build

# MEU PC -> DOCKER: copia a pasta src do projeto para /app/src
COPY src /app/src

# MEU PC -> DOCKER: copia o pom.xml do projeto para /app
COPY pom.xml /app

# DOCKER: entra na pasta /app (como um "cd /app")
WORKDIR /app

# DOCKER: o Maven compila o projeto e gera o .jar em /app/target
# o -DskipTests é pra ele ignorar os testes
RUN mvn clean install -DskipTests

# ===== PARTE 2: roda o .jar =====

# DOCKER: começa uma imagem nova, só com Java 21 para rodar (menor que a da parte 1)
FROM eclipse-temurin:21-jre-alpine

# PARTE 1 -> PARTE 2: pega o .jar gerado lá em cima e salva como /app/app.jar
COPY --from=build /app/target/enveloppe-0.0.1-SNAPSHOT.jar /app/app.jar

# DOCKER: entra na pasta /app
WORKDIR /app

# DOCKER: avisa que a aplicação usa a porta 8080
# (para acessar pelo MEU PC: docker run -p 8080:8080 ...)
EXPOSE 8080

# DOCKER: comando que inicia a aplicação quando o container sobe
CMD ["java", "-jar", "app.jar"]