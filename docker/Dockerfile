# 使用官方OpenJDK 11作为基础镜像
FROM openjdk:11-jre

# 设定镜像内的工作目录
WORKDIR /app

# 将Maven打包好的JAR文件复制到镜像内
COPY product-sale-system-1.0.0.jar /app/application.jar

# 复制Docker专用的配置文件
COPY application-docker.yml /app/application-docker.yml

# 创建上传文件目录
RUN mkdir -p /app/uploads

# 声明容器运行时监听的端口（Spring Boot默认端口）
EXPOSE 8080

# 设置容器启动时执行的命令，使用docker profile
ENTRYPOINT ["java", "-jar", "/app/application.jar", "--spring.profiles.active=docker", "--spring.config.additional-location=file:/app/application-docker.yml"]
