# 多阶段构建：源码直接构建，适配 Render 等云平台的 Git 自动部署
# 构建阶段：Maven 编译打包
FROM maven:3.8-openjdk-11 AS builder
WORKDIR /build

# 先复制 pom 单独拉依赖，利用 Docker 层缓存加速后续构建
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q package -DskipTests

# 运行阶段：精简JRE + 内存参数适配512MB免费实例
FROM eclipse-temurin:11-jre
WORKDIR /app

COPY --from=builder /build/target/product-sale-system-1.0.0.jar /app/application.jar

RUN mkdir -p /app/uploads

EXPOSE 8080

# JVM 内存上限适配免费实例：堆300m + 元空间96m + 线程栈512k
ENTRYPOINT ["java", "-Xms128m", "-Xmx300m", "-XX:MaxMetaspaceSize=96m", "-Xss512k", "-jar", "/app/application.jar", "--spring.profiles.active=render"]
