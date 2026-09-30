# Stage 1: build
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# Stage 2: run
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /workspace/target/coding-agent-1.0.0-jar-with-dependencies.jar /app/coding-agent.jar

RUN mkdir -p /root/.coding-agent

VOLUME /root/.coding-agent
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/coding-agent.jar"]
CMD ["--help"]
