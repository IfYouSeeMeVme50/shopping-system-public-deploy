# 部署文件清单

## 必需文件

将以下文件复制到目标电脑的部署目录中：

### 核心配置文件
- `docker-compose.yml` - Docker Compose 编排文件
- `Dockerfile` - 应用镜像构建文件
- `.dockerignore` - Docker 忽略文件

### 应用文件
- `target/product-sale-system-1.0.0.jar` - 打包好的 Spring Boot 应用

### 配置文件
- `src/main/resources/application.yml` - 应用配置文件

## 可选文件

### 部署脚本
- `deploy.sh` - Linux/macOS 部署脚本
- `deploy.bat` - Windows 部署脚本
- `DEPLOYMENT_GUIDE.md` - 详细部署指南

### 数据库文件
- `src/main/java/com/example/shopping_system/配置文件和数据库/database.sql` - 数据库初始化脚本

## 文件结构

部署目录建议结构：
```
shopping-system-deploy/
├── docker-compose.yml
├── Dockerfile
├── .dockerignore
├── target/
│   └── product-sale-system-1.0.0.jar
└── src/
    └── main/
        └── resources/
            └── application.yml
```

## 快速部署包

要创建完整的部署包，可以运行以下命令：

### Linux/macOS
```bash
# 创建部署包
mkdir -p shopping-system-deploy/{target,src/main/resources}
cp docker-compose.yml Dockerfile .dockerignore shopping-system-deploy/
cp target/product-sale-system-1.0.0.jar shopping-system-deploy/target/
cp src/main/resources/application.yml shopping-system-deploy/src/main/resources/
cp deploy.sh DEPLOYMENT_GUIDE.md shopping-system-deploy/

# 打包
tar -czf shopping-system-deploy.tar.gz shopping-system-deploy/
```

### Windows
```cmd
# 创建部署包
mkdir shopping-system-deploy\target
mkdir shopping-system-deploy\src\main\resources
copy docker-compose.yml shopping-system-deploy\
copy Dockerfile shopping-system-deploy\
copy .dockerignore shopping-system-deploy\
copy target\product-sale-system-1.0.0.jar shopping-system-deploy\target\
copy src\main\resources\application.yml shopping-system-deploy\src\main\resources\
copy deploy.bat shopping-system-deploy\
copy DEPLOYMENT_GUIDE.md shopping-system-deploy\
```

## 验证部署文件

部署前请验证以下文件是否存在且内容正确：

1. **docker-compose.yml** - 包含应用和数据库服务配置
2. **Dockerfile** - 正确引用 JAR 文件路径
3. **application.yml** - 数据库连接配置正确
4. **product-sale-system-1.0.0.jar** - 文件存在且可执行

## 部署流程

1. 将上述必需文件复制到目标电脑
2. 确保目标电脑已安装 Docker 和 Docker Compose
3. 运行部署脚本或手动执行 `docker-compose up -d`
4. 访问 http://localhost:8080 验证部署

## 注意事项

- 确保目标电脑的 8080 和 3307 端口未被占用
- 首次启动可能需要几分钟时间初始化数据库
- 上传的文件会保存在容器内的 `/uploads` 目录
- 数据库数据会持久化在 Docker 卷中
