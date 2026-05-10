FROM eclipse-temurin:25-jre-alpine

COPY target/core-1.1.0.jar core-1.1.0.jar

EXPOSE 8005

VOLUME [ "/logs" ]

ENV INSTANCE_ID=default

ENTRYPOINT ["java", "--sun-misc-unsafe-memory-access=allow", "-Dlogging.file.name=/logs/core-${INSTANCE_ID}.log", "-jar", "core-1.1.0.jar"]
