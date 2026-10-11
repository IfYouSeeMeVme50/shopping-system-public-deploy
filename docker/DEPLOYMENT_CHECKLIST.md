# Docker部署检查清单

## 原始问题解答
**问：docker目录下已有dockerfile，现在满足docker部署的所有条件了吗？还是说要把application.yml复制到这个目录来？**

**答：** 现在已经满足Docker部署的所有条件。**不需要**简单地将application.yml复制到docker目录，因为：

1. 已经创建了专门的`application-docker.yml`配置文件，针对Docker环境进行了优化
2. Dockerfile已更新为使用这个配置文件
3. docker-compose.yml配置正确
4. 所有必要的文件都已就位

## 当前部署状态

### ✅ 已完成的配置

1. **Dockerfile** - 已更新，包含：
   - 使用OpenJDK 11基础镜像
   - 复制JAR文件
   - 复制`application-docker.yml`配置文件
   - 设置Spring Profile为`docker`
   - 指定配置文件位置

2. **application-docker.yml** - 已创建，包含：
   - 正确的数据库连接配置（使用服务名`mysql`而非`localhost`）
   - 与原始application.yml相同的其他配置

3. **docker-compose.yml** - 已优化，包含：
   - MySQL数据库服务
   - 应用服务
   - 正确的网络和卷配置

4. **验证脚本** - `validate-config.bat`可用于检查配置

### 📋 部署前检查

1. **确保JAR文件存在**：
   ```bash
   # 在项目根目录运行
   mvn clean package
   ```

2. **检查文件结构**：
   ```
   docker/
   ├── Dockerfile                    # Docker构建文件
   ├── application-docker.yml       # Docker专用配置
   ├── docker-compose.yml           # 服务编排
   ├── .dockerignore                # 忽略文件
   ├── validate-config.bat          # 验证脚本
   └── DEPLOYMENT_CHECKLIST.md      # 本文件
   ```

3. **数据库初始化脚本**：
   - 位置：`../src/main/java/com/example/shopping_system/配置文件和数据库/database.sql`
   - 已确认存在

## 🚀 部署步骤

### 方法1：使用docker-compose（推荐）

```bash
# 进入docker目录
cd shopping-system/shopping-system/docker

# 启动所有服务
docker-compose up

# 或后台运行
docker-compose up -d
```

### 方法2：手动构建和运行

```bash
# 进入docker目录
cd shopping-system/shopping-system/docker

# 构建Docker镜像
docker build -t shopping-app .

# 运行MySQL数据库
docker run -d --name shopping-mysql \
  -e MYSQL_ROOT_PASSWORD=Qwe235354744 \
  -e MYSQL_DATABASE=shopping_system \
  -p 3307:3306 \
  mysql:8.0

# 运行应用
docker run -d --name shopping-app \
  -p 8080:8080 \
  --link shopping-mysql:mysql \
  shopping-app
```

## 🔧 验证部署

1. **检查容器状态**：
   ```bash
   docker ps
   ```

2. **查看应用日志**：
   ```bash
   docker logs shopping-app
   ```

3. **访问应用**：
   - 浏览器打开：http://localhost:8080

4. **检查数据库连接**：
   ```bash
   docker exec -it shopping-mysql mysql -uroot -pQwe235354744 shopping_system
   ```

## 📝 配置说明

### 为什么不需要复制原始application.yml？
- 原始application.yml配置了`localhost:3306`，这在容器中无法工作
- Docker环境中应用需要通过服务名`mysql`访问数据库
- 创建专门的`application-docker.yml`更清晰、更易于维护

### 环境变量覆盖
如果需要动态配置，可以在docker-compose.yml中重新添加环境变量，它们会覆盖配置文件中的设置。

## 🐛 故障排除

### 常见问题

1. **Docker构建失败：找不到JAR文件**
   - 确保已运行`mvn clean package`
   - 检查`../target/product-sale-system-1.0.0.jar`是否存在

2. **应用启动失败：数据库连接错误**
   - 确保MySQL容器已启动
   - 检查网络配置
   - 验证数据库密码是否正确

3. **端口冲突**
   - 默认端口：8080（应用）、3307（数据库）
   - 可在docker-compose.yml中修改

### 日志查看
```bash
# 应用日志
docker logs shopping-app

# 数据库日志
docker logs shopping-mysql

# 查看所有日志
docker-compose logs
```

## ✅ 总结
现在Docker部署已经完全配置好，可以正常运行。所有必要的文件都已就位，配置已针对容器化环境进行了优化。
