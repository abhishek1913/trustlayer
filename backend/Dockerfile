FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home trustlayer
COPY --from=build /workspace/target/trustlayer.jar app.jar
USER trustlayer
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
