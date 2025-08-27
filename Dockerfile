# Build stage
FROM maven:3.9-amazoncorretto-24 AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && dnf install -y tar && dnf clean all
COPY src ./src
RUN ./mvnw -q clean package -DskipTests

# Runtime stage
FROM amazoncorretto:24-al2023
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]