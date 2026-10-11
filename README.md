# 购物系统 (升级版)

这是一个基于Spring Boot的完整购物系统，支持商品展示、购买意向提交、卖家后台管理和丰富的商品管理功能。系统已升级包含商品分类、多图上传、富文本描述、库存管理等新功能。

## 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本
- 现代浏览器（支持HTML5和JavaScript）

## 快速开始

### 1. 数据库配置

**这是最重要的配置步骤！** 您需要修改数据库连接信息：

#### 步骤 1：创建数据库
在MySQL中创建一个新的数据库：
```sql
CREATE DATABASE shopping_system;
```

#### 步骤 2：修改数据库配置
编辑文件 `src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shopping_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: your_mysql_username      # 修改为您的MySQL用户名
    password: your_mysql_password      # 修改为您的MySQL密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**需要修改的关键参数：**
- `url` 中的数据库名：将 `shopping_system` 改为您创建的数据库名
- `username`：您的MySQL用户名（通常是 `root`）
- `password`：您的MySQL密码

#### 步骤 3：数据库初始化
项目使用JPA自动创建表结构，无需手动执行SQL脚本。启动应用时会自动创建以下表：
- `categories` - 商品分类表（支持多级分类）
- `products` - 商品表（支持多图、分类、库存）
- `product_images` - 商品图片表
- `sellers` - 卖家表  
- `customers` - 客户表
- `purchase_intents` - 购买意向表

### 2. 构建和运行

#### 运行redis服务器

```bash
Start-Process -NoNewWindow -FilePath "C:\redis\redis-server.exe" ; Start-Sleep -Seconds 2 ; C:\redis\redis-cli.exe ping 2>&1 #根据实际情况填写redis目录
```

#### 方式一：使用Maven直接运行
```bash
# 进入项目目录
cd shopping-system

# 编译并运行
mvn clean compile
mvn spring-boot:run
```

#### 方式二：打包后运行
```bash
# 进入项目目录
cd shopping-system

# 打包项目（跳过测试）
mvn clean package -DskipTests

# 运行JAR包
java -jar target/product-sale-system-1.0.0.jar
```

#### 方式三：PowerShell运行（Windows）
```powershell
# 进入项目目录
cd shopping-system

# 打包项目
mvn clean package -DskipTests

# 运行应用
java -jar target/product-sale-system-1.0.0.jar
```

### 3. 访问应用

应用启动后，在浏览器中访问：
- 用户前台：http://localhost:8080/
- 卖家后台：http://localhost:8080/seller.html

**默认端口：8080**（可在application.yml中修改）

## 项目功能

### 用户功能
- 浏览商品信息（支持分类筛选）
- 搜索商品（支持关键词搜索）
- 分页浏览商品
- 提交购买意向
- 填写联系信息

### 卖家功能
- 登录卖家后台（用户名：admin，密码：admin123）
- 商品管理（新增、编辑、删除商品）
- 多图上传（支持上传多张商品图片）
- 富文本描述（使用Quill.js编辑器）
- 分类管理（支持多级商品分类）
- 库存管理（自动下架零库存商品）
- 查看购买意向
- 管理商品状态
- 修改密码
- 客户信息管理

## 项目结构

```
src/main/java/com/example/shopping_system/
├── ShoppingSystemApplication.java    # 主启动类
├── Controller类/                     # 控制器层
│   ├── HomeController.java           # 主页控制器
│   ├── ProductController.java        # 商品控制器
│   ├── PurchaseIntentController.java # 购买意向控制器
│   └── SellerController.java         # 卖家控制器
├── Service类/                        # 服务层
│   ├── ProductService.java
│   ├── PurchaseIntentService.java
│   └── SellerService.java
├── Repository接口/                   # 数据访问层
│   ├── CategoryRepository.java       # 分类数据访问
│   ├── ProductRepository.java
│   ├── PurchaseIntentRepository.java
│   ├── SellerRepository.java
│   └── CustomerRepository.java
├── 实体类/                           # 实体类
│   ├── Category.java                 # 分类实体（支持多级分类）
│   ├── Product.java                  # 商品实体（支持多图、分类、库存）
│   ├── PurchaseIntent.java
│   ├── Seller.java
│   ├── Customer.java                 # 客户实体
│   ├── ProductStatus.java
│   └── IntentStatus.java
├── DTO类/                           # 数据传输对象
│   ├── PurchaseIntentDTO.java
│   ├── SellerLoginDTO.java
│   ├── ChangePasswordDTO.java
│   └── CompleteTransactionDTO.java
└── 配置类/                          # 配置类
    ├── DataInitializer.java         # 数据初始化
    └── WebConfig.java               # Web配置
```

## 数据库配置详细说明

### 默认配置
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/product_sale?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### 需要修改的内容

1. **数据库名称**：将 `product_sale` 改为您创建的数据库名
2. **用户名**：将 `root` 改为您的MySQL用户名
3. **密码**：将 `your_password` 改为您的MySQL密码

### 示例修改
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shopping_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: myuser
    password: mypassword123
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 确认数据库名、用户名、密码是否正确
- 检查MySQL是否允许远程连接（如果使用远程数据库）

### 2. 端口被占用
默认端口是8080，如果被占用可以修改 `application.yml` 中的：
```yaml
server:
  port: 8081  # 修改为其他端口
```

### 3. 编译错误
确保使用Java 17：
```bash
java -version
```

## 新功能特性

### 商品分类系统
- 支持多级分类结构
- 分类关联商品
- 分类筛选功能

### 多图上传
- 支持上传多张商品图片
- 图片预览和管理
- 自动保存图片路径

### 富文本描述
- 集成Quill.js富文本编辑器
- 支持格式化文本、图片、链接
- 所见即所得编辑体验

### 库存管理
- 商品库存跟踪
- 自动下架零库存商品
- 库存状态显示

### 分页功能
- 商品列表分页显示
- 支持分类和关键词搜索分页
- 分页导航控件

### 客户信息管理
- 完整的客户信息记录
- 客户购买历史
- 联系信息管理

## 技术栈

- **后端**：Spring Boot 2.7.0, Spring Data JPA, Spring MVC
- **数据库**：MySQL 8.0
- **前端**：HTML, CSS, JavaScript, Thymeleaf, Quill.js
- **构建工具**：Maven
