# Stage 1: build do WAR com Maven + Java 8 (mesma versão exigida pelo WildFly 10)
FROM maven:3.8.7-eclipse-temurin-8 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package

# Stage 2: runtime -- WildFly 10 oficial, só implanta o WAR gerado no stage anterior
FROM jboss/wildfly:10.1.0.Final
COPY --from=build /build/target/url-shortener.war /opt/jboss/wildfly/standalone/deployments/
EXPOSE 8080
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
