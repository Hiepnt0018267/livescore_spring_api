# Giai đoạn 1: Build bằng Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy toàn bộ repo vào container
COPY . .

# Chạy lệnh build trỏ thẳng vào file pom.xml bên trong api/api
RUN mvn -f api/api/pom.xml clean package -DskipTests

# Giai đoạn 2: Chạy bằng Java
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Lấy file .jar từ thư mục target bên trong api/api
COPY --from=build /app/api/api/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]