FROM maven:3.8-eclipse-temurin-21-alpine AS builder

WORKDIR /home

ARG MAVEN_GITHUB_TOKEN
ARG MAVEN_GITHUB_ORG

ENV MAVEN_GITHUB_TOKEN=$MAVEN_GITHUB_TOKEN
ENV MAVEN_GITHUB_ORG=$MAVEN_GITHUB_ORG

COPY ./jpo-conflictmonitor/pom.xml ./jpo-conflictmonitor/
COPY ./settings.xml ./jpo-conflictmonitor/

# Download dependencies alone to cache them first
WORKDIR /home/jpo-conflictmonitor
RUN mvn -s settings.xml dependency:resolve

# Copy the source code and build the conflict monitor
COPY ./jpo-conflictmonitor/src ./src
RUN mvn -s settings.xml install -DskipTests

FROM amazoncorretto:21

WORKDIR /home

COPY --from=builder /home/jpo-conflictmonitor/src/main/resources/application.yaml /home
COPY --from=builder /home/jpo-conflictmonitor/src/main/resources/logback.xml /home
COPY --from=builder /home/jpo-conflictmonitor/target/jpo-conflictmonitor.jar /home

ENTRYPOINT ["java", \
	"-Djava.rmi.server.hostname=$DOCKER_HOST_IP", \
	"-Dcom.sun.management.jmxremote.port=9090", \
	"-Dcom.sun.management.jmxremote.rmi.port=9090", \
	"-Dcom.sun.management.jmxremote", \
	"-Dcom.sun.management.jmxremote.local.only=true", \
	"-Dcom.sun.management.jmxremote.authenticate=false", \
	"-Dcom.sun.management.jmxremote.ssl=false", \
	"-Dlogback.configurationFile=/home/logback.xml", \
	"-jar", \
	"/home/jpo-conflictmonitor.jar"]