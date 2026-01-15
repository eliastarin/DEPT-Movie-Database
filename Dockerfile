# ---- Build stage ----
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app

# Leverage dependency caching
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080


ENV JAVA_OPTS=""

# app reads TMDB_BEARER_TOKEN from env via application.yml
# tmdb.bearer-token: "${TMDB_BEARER_TOKEN:}"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
