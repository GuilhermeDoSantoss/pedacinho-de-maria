# Build multi-stage: o estágio "build" tem o Maven completo (~500MB extra),
# mas a imagem final (FROM eclipse-temurin:21-jre-alpine) só carrega o JAR e o
# JRE. Isso mantém a imagem de produção pequena — menos superfície de ataque,
# menos custo de storage/transferência no registry, deploy mais rápido.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
# Camada de dependências separada da camada de código: se só o código mudar,
# o Docker reaproveita o cache desta camada e não rebaixa tudo do Maven Central de novo.
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Roda como usuário não-root — princípio de menor privilégio. Se o processo
# Java for comprometido, o atacante não herda privilégios de root no container.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# CMD em forma de string (não array) é executado via /bin/sh -c, o que
# permite expansão de variável de ambiente aqui. Isso importa porque a
# aplicação escuta em ${PORT:${SERVER_PORT:8080}} (ver application.yml) — a
# própria Render injeta PORT dinamicamente, e não é garantido que seja 8080.
# ${PORT:-8080} local não muda nada (fallback continua 8080), mas em produção
# passa a testar a porta certa em vez de sempre 8080.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]