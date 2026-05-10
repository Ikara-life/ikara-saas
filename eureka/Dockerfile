FROM eclipse-temurin:25-jre-alpine

COPY target/eureka-1.1.0.jar eureka-1.1.0.jar

EXPOSE 9999

VOLUME [ "/logs" ]

ENV INSTANCE_ID=default

ENTRYPOINT ["java", "--sun-misc-unsafe-memory-access=allow", "-Dlogging.file.name=/logs/eureka-${INSTANCE_ID}.log", "-jar", "eureka-1.1.0.jar"]
