FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:25-jre

RUN groupadd --system nurl && \
    useradd --system --gid nurl nurl

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN chown nurl:nurl app.jar

USER nurl

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]