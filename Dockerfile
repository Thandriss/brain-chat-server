FROM openjdk:19

ARG JAR_FILE=target/*.jar

WORKDIR /app

RUN mkdir -p /var/log/myapp

COPY ${JAR_FILE} app.jar

EXPOSE 8080 5005

ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "/app/app.jar"]