# 购物系统Docker部署指南

## 系统概述

这是一个基于Spring Boot的在线购物系统，使用Docker容器化部署，包含以下组件：
- **应用服务**：Spring Boot Web应用（端口8080）
- **数据库服务**：MySQL 8.0（端口3307）

系统采用容器间网络通信，应用通过服务名`mysql`访问数据库，确保在Docker环境中可靠连接。

## 系统要求

### 硬件要求
- 内存：至少2GB RAM
- 存储：至少1GB可用空间
- CPU：双核处理器或更高

### 软件要求
- **必需**：Docker Desktop（Windows/Mac）或 Docker Engine（Linux）
  - Docker版本：20.10.0或更高
  - Docker Compose版本：2.0.0或更高
- **可选**：Git（用于克隆项目）

## 快速开始

### 1. 准备部署文件

确保以下文件在同一个目录中（通常是`shopping-system/shopping-system/docker/`目录）：
```
├── docker-compose.yml          # 服务编排配置
├── Dockerfile                  # 应用镜像构建配置
├── application-docker.yml      # Docker专用配置文件
├── product-sale-system-1.0.0.jar  # Spring Boot应用JAR文件
├── .dockerignore              # Docker忽略文件
├── deploy.bat                 # Windows部署脚本
├── deploy.sh                  # Linux/Mac部署脚本
└── validate-config.bat        # 配置验证脚本
```

**重要**：如果JAR文件不在当前目录，需要先将其复制到docker目录：
```bash
# 从项目target目录复制JAR文件到docker目录
copy "shopping-system\target\product-sale-system-1.0.0.jar" "shopping-system\shopping-system\docker\"
```

### 2. 启动系统

打开终端，导航到包含上述文件的目录，执行：

#### Windows PowerShell：
```powershell
# 进入docker目录
cd "shopping-system\shopping-system\docker"

# 启动所有服务（后台运行）
docker-compose up -d

# 或前台运行（查看实时日志）
docker-compose up
```

### 3. 验证部署

等待服务完全启动（约30-60秒），然后验证：

1. **检查容器状态**：
   
   ```powershell
   docker-compose ps
   ```
   应该显示两个容器都在运行：
   - `shopping-app`：状态为"Up"，端口8080
- `shopping-mysql`：状态为"Up (healthy)"，端口3307
   
2. **访问应用**：
   - **买家中心**：http://localhost:8080/
   - **卖家后台**：http://localhost:8080/seller.html

3. **验证应用响应**：
   ```powershell
   # 使用PowerShell验证
   Invoke-WebRequest -Uri "http://localhost:8080/" -Method Get -UseBasicParsing
   ```
   应该返回HTTP状态码200。

## 详细部署步骤

### Windows 系统部署

1. **安装 Docker Desktop**
   - 访问 https://www.docker.com/products/docker-desktop
   - 下载并安装 Docker Desktop
   - 启动 Docker Desktop，等待Docker引擎完全启动

2. **准备项目文件**
   - 确保项目已构建，生成JAR文件：`shopping-system/target/product-sale-system-1.0.0.jar`
   - 如果JAR文件不存在，需要先构建项目：
     ```cmd
     cd shopping-system
     mvn clean package
     ```

3. **复制JAR文件到docker目录**
   ```cmd
   copy "shopping-system\target\product-sale-system-1.0.0.jar" "shopping-system\shopping-system\docker\"
   ```

4. **使用部署脚本（推荐）**
   - 运行`deploy.bat`脚本，它会自动执行所有部署步骤
   - 或手动执行：
     ```cmd
     cd shopping-system\shopping-system\docker
     docker-compose up -d --build
     ```

5. **验证部署**
   ```cmd
   docker-compose ps
   curl http://localhost:8080/
   ```

## 服务配置说明

### 端口映射
- **应用服务**：主机8080端口 → 容器8080端口
- **数据库服务**：主机3307端口 → 容器3306端口（避免与本地MySQL冲突）

### 网络配置
- 创建专用Docker网络：`shopping-network`
- 应用通过服务名`mysql`访问数据库（容器间通信）
- 数据库连接字符串：`jdbc:mysql://mysql:3306/shopping_system?useSSL=false&serverTimezone=UTC`

### 环境变量配置
应用使用`docker` profile启动，加载以下配置：
1. **主配置**：`--spring.profiles.active=docker`
2. **附加配置**：`--spring.config.additional-location=file:/app/application-docker.yml`

### 关键配置文件
1. **application-docker.yml**：Docker专用配置
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://mysql:3306/shopping_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
       username: root
       password: password
   ```
   
2. **Dockerfile**：应用镜像构建配置
   ```dockerfile
   # 关键配置：
   COPY product-sale-system-1.0.0.jar /app/application.jar
   COPY application-docker.yml /app/application-docker.yml
   ENTRYPOINT ["java", "-jar", "/app/application.jar", "--spring.profiles.active=docker", "--spring.config.additional-location=file:/app/application-docker.yml"]
   ```

### 数据持久化
- **MySQL数据**：保存在Docker卷`shopping-mysql-data`中
- **上传文件**：保存在容器内的`/app/uploads`目录（重启后丢失，生产环境应挂载卷）
- **应用日志**：输出到控制台，可通过`docker-compose logs`查看

## 系统管理命令

### 查看服务状态
```powershell
# Windows PowerShell
docker-compose ps

# 查看详细状态
docker-compose ps -a
```

### 查看日志
```powershell
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志（最后20行）
docker-compose logs app --tail=20
docker-compose logs mysql --tail=20

# 实时查看日志
docker-compose logs -f app
```

### 停止服务
```powershell
# 停止服务但保留数据和容器
docker-compose stop

# 停止并删除容器（保留数据卷）
docker-compose down

# 完全重置（删除容器、网络和数据卷）
docker-compose down -v
```

### 重启服务
```powershell
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart app
```

### 更新和重建
```powershell
# 重新构建镜像（代码更新后）
docker-compose up -d --build

# 只重建应用服务
docker-compose up -d --build app
```

### 进入容器
```powershell
# 进入应用容器
docker-compose exec app sh

# 进入MySQL容器
docker-compose exec mysql mysql -u root -ppassword shopping_system
```

## 故障排除指南

### 常见问题及解决方案

1. **端口冲突错误**
   ```
   Error: Port is already allocated
   ```
   **解决方案**：
   - 检查8080或3307端口是否被占用：`netstat -ano | findstr :8080`
   - 修改`docker-compose.yml`中的端口映射
   - 或停止占用端口的服务

2. **JAR文件找不到错误**
   ```
   COPY failed: file not found in build context or excluded by .dockerignore
   ```
   **解决方案**：
   - 确保JAR文件在docker目录中：`dir product-sale-system-1.0.0.jar`
   - 从target目录复制JAR文件：`copy "..\..\..\target\product-sale-system-1.0.0.jar" .`

3. **数据库连接失败**
   ```
   Communications link failure
   ```
   **解决方案**：
   - 等待MySQL完全启动（健康检查通过）
   - 检查MySQL日志：`docker-compose logs mysql`
   - 验证网络连接：`docker network ls` 和 `docker network inspect docker_shopping-network`

4. **应用启动缓慢或失败**
   **解决方案**：
   - 增加Docker内存分配（Docker Desktop → Settings → Resources）
   - 查看详细日志：`docker-compose logs app --tail=50`
   - 检查依赖服务状态：`docker-compose ps`


### 诊断步骤

1. **检查Docker环境**
   ```powershell
   docker --version
   docker-compose --version
   docker info
   ```

2. **验证配置文件**
   ```powershell
   # 运行验证脚本
   .\validate-config.bat
   
   # 或手动检查
   type docker-compose.yml
   type Dockerfile
   ```

3. **分步启动服务**
   ```powershell
   # 先启动数据库
   docker-compose up -d mysql
   
   # 等待MySQL健康
   docker-compose logs mysql --tail=10
   
   # 再启动应用
   docker-compose up -d app
   ```

### 完全重置系统
```powershell
# 停止并删除所有容器、网络、数据卷
docker-compose down -v

# 清理未使用的Docker资源
docker system prune -f

# 重新部署
docker-compose up -d --build
```

## 系统架构和功能

### 系统架构
```
用户浏览器
    ↓
[负载均衡器] ← 可选
    ↓
Docker容器：shopping-app (Spring Boot)
    ↓
Docker容器：shopping-mysql (MySQL 8.0)
    ↓
数据卷持久化
```

### 主要功能模块
- **买家功能**：
  - 商品浏览、搜索、分类查看
  - 购买意向提交
  - 用户注册登录
- **卖家功能**：
  - 商品上架、编辑、下架
  - 库存管理
  - 订单处理
- **系统管理**：
  - 用户管理
  - 分类管理
  - 文件上传（商品图片）

### 数据库初始化
系统启动时自动执行：
1. 创建数据库`s shopping_system`（如果不存在）
2. 创建所有必要的表结构
3. 插入初始数据：
   - 默认卖家账号：admin/admin123
   - 商品分类结构
   - 示例商品数据

### 健康检查机制
- **MySQL健康检查**：每5秒检查一次，确保数据库完全就绪
- **应用依赖管理**：应用容器等待MySQL健康后才启动
- **网络配置**：专用Docker网络确保容器间可靠通信

## 备份与恢复

### 备份数据库
```powershell
# 创建数据库备份（Windows）
docker exec shopping-mysql mysqldump -u root -ppassword shopping_system > backup_%date:~0,4%%date:~5,2%%date:~8,2%.sql

# 或使用PowerShell日期格式
docker exec shopping-mysql mysqldump -u root -ppassword shopping_system > backup_$(Get-Date -Format "yyyyMMdd").sql
```

### 恢复数据库
```powershell
# 恢复数据库
Get-Content backup.sql | docker exec -i shopping-mysql mysql -u root -ppassword shopping_system
```

### 备份Docker卷数据
```powershell
# 备份MySQL数据卷
docker run --rm -v shopping-mysql-data:/source -v ${PWD}:/backup alpine tar czf /backup/mysql_backup_$(date +%Y%m%d).tar.gz -C /source .
```

## 性能优化

### 资源限制配置
在`docker-compose.yml`中优化资源分配：
```yaml
services:
  app:
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '1.0'
        reservations:
          memory: 512M
          cpus: '0.5'
  
  mysql:
    deploy:
      resources:
        limits:
          memory: 512M
          cpus: '0.5'
```

### 应用性能调优
1. **JVM参数优化**：在Dockerfile中添加JVM参数
   ```dockerfile
   ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "/app/application.jar", "--spring.profiles.active=docker", "--spring.config.additional-location=file:/app/application-docker.yml"]
   ```

2. **数据库连接池优化**：在application-docker.yml中配置
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 10
         minimum-idle: 5
         connection-timeout: 30000
   ```

## 生产环境部署建议

### 安全加固
1. **修改默认凭证**：
   - 修改`docker-compose.yml`中的MySQL root密码
   - 更新`application-docker.yml`中的数据库连接密码
   - 使用环境变量管理敏感信息

2. **网络隔离**：
   ```yaml
   networks:
     shopping-network:
       driver: bridge
       internal: true  # 内部网络，不对外暴露
   ```

3. **SSL/TLS配置**：
   - 为应用启用HTTPS
   - 配置数据库SSL连接

### 监控和日志
1. **集中日志收集**：
   ```yaml
   services:
     app:
       logging:
         driver: "json-file"
         options:
           max-size: "10m"
           max-file: "3"
   ```

2. **健康检查端点**：
   - 应用健康检查：`http://localhost:8080/actuator/health`
   - 自定义健康检查端点

### 高可用性配置
1. **数据库主从复制**：配置MySQL主从
2. **应用集群部署**：使用多个应用实例
3. **负载均衡器**：配置Nginx或Traefik

## 基于本次部署的经验总结

### 成功部署的关键步骤
1. **JAR文件准备**：确保JAR文件在Docker构建上下文目录中
2. **配置文件优化**：使用专用的`application-docker.yml`配置文件
3. **容器依赖管理**：配置健康检查和`depends_on`条件
4. **网络配置**：使用服务名进行容器间通信

### 验证部署的完整流程
1. 检查容器状态：`docker-compose ps`
2. 查看应用日志：`docker-compose logs app --tail=20`
3. 验证HTTP响应：`Invoke-WebRequest http://localhost:8080/ -UseBasicParsing`
4. 测试数据库连接：`docker-compose exec mysql mysql -u root -ppassword -e "SHOW DATABASES;"`

## 技术支持与故障排除

### 快速诊断命令
```powershell
# 1. 检查Docker环境
docker --version
docker-compose --version
docker info

# 2. 检查容器状态
docker-compose ps -a
docker-compose logs --tail=50

# 3. 检查网络配置
docker network ls
docker network inspect docker_shopping-network

# 4. 检查资源使用
docker stats
```

### 紧急恢复步骤
```powershell
# 完全重置并重新部署
docker-compose down -v
docker system prune -f -a
docker-compose up -d --build

# 验证恢复
Start-Sleep -Seconds 30
docker-compose ps
Invoke-WebRequest http://localhost:8080/ -UseBasicParsing
```
