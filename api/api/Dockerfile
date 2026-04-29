# Giai đoạn 1: Dùng Maven để đóng gói code thành file .jar
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Giai đoạn 2: Lấy file .jar chạy trên nền Java siêu nhẹ (Tiết kiệm RAM)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]