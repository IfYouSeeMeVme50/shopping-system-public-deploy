# 购物系统（公网部署版）

基于 Spring Boot 的完整购物系统，支持商品展示、分类筛选、购买意向提交、卖家后台管理等功能。本项目已部署到公网，**无需任何本地环境，打开网页即可使用**。

## 在线体验

- **公网地址**：https://shopping-system-public-deploy.onrender.com
- **买家前台**：直接访问上述地址，浏览商品、搜索、分类筛选、提交购买意向
- **卖家后台**：访问 https://shopping-system-public-deploy.onrender.com/seller.html ，账号 `admin` / 密码 `admin123`
- **代码仓库**：
  - GitHub（部署源）：https://github.com/IfYouSeeMeVme50/shopping-system-public-deploy
  - Gitee（主仓库，与 GitHub 双推同步）：https://gitee.com/hgb235/shopping-system-public-deploy

> **冷启动说明**：Render 免费版在 15 分钟无访问后会休眠，首次打开需等待约 1 分钟唤醒（页面会显示 Render 加载页），属正常现象，刷新即可。

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | Spring Boot 2.7.0、Spring Data JPA、Spring MVC |
| 数据库 | **TiDB Cloud Starter**（MySQL 协议兼容，东京区，免费持久化云库） |
| 部署 | **Render 免费容器**（Docker 多阶段构建，自动构建 + 自动发布） |
| 缓存 | Redis（**可选**，公网环境关闭，本地开发可选开启；不可用时自动降级直查数据库） |
| 前端 | HTML、CSS、JavaScript、Thymeleaf、Quill.js 富文本 |
| 构建 | Maven、Docker |

## 部署架构

```
用户浏览器
   │
   ▼
Render 免费容器（512MB，本仓库根目录 Dockerfile 自动构建）
   │  Spring Boot 内嵌 Tomcat，端口自适应 PORT
   │  应用配置走 render profile（application-render.yml）
   ▼
TiDB Cloud Starter（持久化云数据库，容器重启数据不丢失）
```

- **Redis 不参与公网部署**：`app.redis-enabled=false` 完全短路，商品读写直查 TiDB
- **建表**：首次启动由 JPA `ddl-auto: update` 自动完成，无需手动执行 SQL
- **预置商品图片**：已烤入 Docker 镜像层（`uploads/`），部署即带图
- **运行时上传的图片/媒体**：字节直接存 TiDB（`stored_images` 表 LONGBLOB），通过 `/images/{id}` 接口读取，容器休眠/重新部署后不丢失；旧的 `/uploads/` 文件链接继续兼容

## 正确的使用方式

### 场景一：普通用户 / 演示（零部署）

什么都不需要安装，直接用浏览器打开公网地址即可：

1. 买家：浏览商品 → 查看详情 → 填写联系信息 → 提交购买意向
2. 卖家：进入 `/seller.html` 登录 → 管理商品 / 分类 / 库存 / 查看购买意向
3. 网页上的所有数据操作（增删改商品、分类、处理订单）都会**实时写入 TiDB 云数据库**，持久保存

### 场景二：代码维护与更新

本地修改代码后，一条命令完成发布：

```bash
git push origin master   # 同时推送 Gitee + GitHub
```

Render 检测到 GitHub 新提交后自动重新构建并发布（约 5-8 分钟），构建期间旧版本继续服务不中断。

### 场景三：本地开发（可选）

如需本地调试：

1. **环境**：Java 17+、Maven 3.6+、MySQL 8.0（Redis 可选）
2. **建库**：`CREATE DATABASE shopping_system;`
3. **改配置**：编辑 `src/main/resources/application.yml` 中的数据库用户名/密码
4. **（可选）启动 Redis**：
   ```powershell
   Start-Process -NoNewWindow -FilePath "C:\redis\redis-server.exe"; Start-Sleep -Seconds 2; C:\redis\redis-cli.exe ping
   ```
   不启动也能运行（自动降级直查数据库），启动后商品读写走缓存加速
5. **运行**：
   ```bash
   mvn spring-boot:run
   ```
6. **访问**：http://localhost:8080/ （前台）、http://localhost:8080/seller.html （后台）

> 注意：本地实例连接的是**本地 MySQL**，与公网的 TiDB 是两个独立数据库，数据互不同步。

## 已知限制（免费方案固有的）

| 限制 | 说明 | 影响 |
|---|---|---|
| 应用休眠 | Render 免费版 15 分钟无访问休眠，唤醒约 1 分钟 | 首次访问变慢，不影响数据 |
| 数据库休眠 | TiDB 免费档同样有休眠机制 | 首次数据库连接可能多等十几秒（连接超时已放宽至 30 秒） |
| 图片占数据库容量 | 上传图片以 BLOB 存 TiDB（单文件上限 10MB） | 免费档 25GiB 存储，课程演示量级无压力 |
| 无缓存 | 公网无 Redis，商品读写直查数据库 | 课程演示量级下无感知 |

## 项目功能

### 买家功能
- 浏览商品（分类筛选、关键词搜索、分页）
- 查看商品详情（多图、富文本描述、库存状态）
- 提交购买意向、填写联系信息

### 卖家功能
- 登录卖家后台（admin / admin123）
- 商品管理：新增、编辑、删除、多图上传、富文本描述
- 分类管理：多级分类（一级/二级）
- 库存管理：自动下架零库存商品
- 查看购买意向、完成交易、客户信息管理
- 修改密码

## 项目结构

```
src/main/java/com/example/shopping_system/
├── ShoppingSystemApplication.java    # 主启动类
├── Controller类/                     # 控制器层（Home/Product/PurchaseIntent/Seller/Cart/Favorite/Image）
├── Service类/                        # 服务层（含 RedisCacheService：可降级缓存）
├── Repository接口/                   # Spring Data JPA 数据访问层（含 StoredImageRepository）
├── 实体类/                           # Product/Category/PurchaseIntent/Seller/Customer/StoredImage 等
├── DTO类/                            # 数据传输对象
└── 配置类/                           # DataInitializer、WebConfig

src/main/resources/
├── application.yml                   # 本地默认配置（Redis 开启）
├── application-render.yml            # 公网生产配置（Redis 关闭、TiDB 连接、端口自适应）
└── static/                           # 前端静态页面（Spring Boot 直接托管，单服务架构）

根目录
├── Dockerfile                        # 多阶段构建：源码 → JAR → 运行镜像（适配 Render）
└── uploads/                          # 预置商品图片（已烤入镜像）
```

## 常见问题

**Q: 打开网页很慢/显示 Render 加载页？**
免费版休眠唤醒中，等约 1 分钟刷新即可。

**Q: 网页上删除了分类/商品，数据库里也删了吗？**
是的。网页操作通过 JPA 直接执行 SQL 写入 TiDB，实时持久化。注意：分类下还挂着商品时，系统会拒绝删除该分类。

**Q: 如何更新线上版本？**
本地改代码 → `git push origin master` → Render 自动构建发布。

**Q: 本地跑和公网跑数据一样吗？**
不一样，两边是完全独立的数据库（本地 MySQL vs 云端 TiDB），互不同步。

**Q: 本地必须开 Redis 吗？**
不必须。降级补丁保证 Redis 不可用时自动直查数据库；开启 Redis 仅作为读缓存加速。
