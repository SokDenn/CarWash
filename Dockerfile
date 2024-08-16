# Первый этап: Сборка приложения
FROM maven:3.8.5-amazoncorretto-21 AS build

# Отключаем демон Maven для более стабильной сборки в контейнере
ENV MAVEN_OPTS="-Dorg.gradle.daemon=false"
LABEL stage=intermediate

# Устанавливаем рабочую директорию
WORKDIR /home/app

# Копируем POM-файл и зависимости Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src
COPY target target

# Сборка проекта
RUN ./mvnw package -DskipTests

# Второй этап: Создание минималистичного образа для запуска
FROM amazoncorretto:21

# Копируем собранный JAR файл из стадии сборки
COPY --from=build /home/app/target/carWashApplication.jar .

# Запуск приложения
CMD ["java", "-jar", "carWashApplication.jar"]