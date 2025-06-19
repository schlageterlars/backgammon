FROM sbtscala/scala-sbt:eclipse-temurin-jammy-21_35_1.9.6_3.3.1 AS build

WORKDIR /app
COPY ./ /app

RUN sbt clean metric/assembly

FROM alpine/java:21-jdk AS run

WORKDIR /app

COPY --from=build /app/metric/target/scala-3.5.0/metric-assembly-0.1.0-SNAPSHOT.jar /app/metric.jar

EXPOSE 8080
CMD [ "java", "-jar", "/app/metric.jar", "0.0.0.0", "metric" ]