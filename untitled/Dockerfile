FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/coding-agent-*-jar-with-dependencies.jar /app/coding-agent.jar
RUN mkdir -p /root/.coding-agent
VOLUME /root/.coding-agent
ENTRYPOINT ["java", "-jar", "/app/coding-agent.jar"]
CMD ["--help"]