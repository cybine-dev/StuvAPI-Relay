FROM gradle:jdk17-alpine AS build
ENV SETTINGS_DATABASE_VENDOR mariadb
ENV QUARKUS_PROFILE prod

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . /home/gradle/src
RUN gradle clean build --no-daemon

FROM ibm-semeru-runtimes:open-17-jre-focal AS release
ENV SETTINGS_DATABASE_VENDOR mariadb
ENV SETTINGS_DATABASE_URL jdbc:mariadb://localhost/stuvapi
ENV SETTINGS_DATABASE_USER root
ENV SETTINGS_DATABASE_PASSWORD password

WORKDIR /opt/app
EXPOSE 8080

COPY --from=build /home/gradle/src/build/*.jar /opt/app/stuvapi-relay.jar
ENTRYPOINT ["java", "-jar", "/opt/app/stuvapi-relay.jar"]