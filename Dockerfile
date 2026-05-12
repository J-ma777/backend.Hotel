# ---------------------------
# ETAPA 1: BUILD (compilación)
# ---------------------------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests

# ---------------------------
# ETAPA 2: RUNTIME (ejecución)
# ---------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 3030

ENTRYPOINT ["java", "-jar", "app.jar"]