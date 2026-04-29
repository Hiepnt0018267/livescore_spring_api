# Giai đoạn 1: Build bằng Maven (Dùng Java 17)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy toàn bộ vào container
COPY . .

# Chạy lệnh build trỏ vào file pom.xml
RUN mvn -f api/api/pom.xml clean package -DskipTests

# Giai đoạn 2: Chạy bằng Java 17 nhẹ
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy file jar từ thư mục đích (đảm bảo đúng cấu trúc thư mục của Hiệp)
COPY --from=build /app/api/api/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]