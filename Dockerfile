FROM openjdk:17-oracle

LABEL maintainer="ivan"

RUN mkdir /app

# Копируем JAR-файл в директорию в образе
COPY build/libs/courier-0.0.1-SNAPSHOT.jar /app/courier.jar

# Указываем рабочую директорию
WORKDIR /app

#ENV TZ=Asia/Almaty
#RUN cp /usr/share/zoneinfo/$TZ /etc/localtime

# Открываем порт
EXPOSE 28080

ENTRYPOINT ["java", "-jar", "courier.jar"]