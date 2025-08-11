FROM maven:3.9.4-eclipse-temurin-17 AS build

COPY pom.xml pom.xml
COPY src src

RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre AS builder

WORKDIR /app

COPY --from=build target/*.jar ./application.jar
RUN java -Djarmode=layertools -jar ./application.jar extract

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder app/dependencies/ ./
COPY --from=builder app/spring-boot-loader/ ./
COPY --from=builder app/snapshot-dependencies/ ./
COPY --from=builder app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]