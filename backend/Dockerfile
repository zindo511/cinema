# STAGE 1: BUILD
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# COPY Maven Wrapper + pom trước để tận dụng cache
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline  # Tải thư viện về cất sẵn

# COPY source code
COPY src/ ./src/

RUN ./mvnw clean package -DskipTests

#STAGE 2: RUNTIME
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]