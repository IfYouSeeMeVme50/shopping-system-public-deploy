@echo off
echo ========================================
echo Docker配置验证脚本
echo ========================================
echo.

echo 1. 检查必要的文件...
if exist "..\target\product-sale-system-1.0.0.jar" (
    echo   ✓ JAR文件存在: ..\target\product-sale-system-1.0.0.jar
) else (
    echo   ✗ 错误: JAR文件不存在
    echo     请运行: mvn clean package
    goto :error
)

if exist "application-docker.yml" (
    echo   ✓ Docker配置文件存在: application-docker.yml
) else (
    echo   ✗ 错误: application-docker.yml不存在
    goto :error
)

if exist "Dockerfile" (
    echo   ✓ Dockerfile存在
) else (
    echo   ✗ 错误: Dockerfile不存在
    goto :error
)

if exist "docker-compose.yml" (
    echo   ✓ docker-compose.yml存在
) else (
    echo   ✗ 错误: docker-compose.yml不存在
    goto :error
)

echo.
echo 2. 检查数据库初始化脚本...
if exist "..\src\main\java\com\example\shopping_system\配置文件和数据库\database.sql" (
    echo   ✓ 数据库初始化脚本存在
) else (
    echo   ⚠ 警告: 数据库初始化脚本不存在
    echo      路径: ..\src\main\java\com\example\shopping_system\配置文件和数据库\database.sql
)

echo.
echo 3. 验证Dockerfile内容...
findstr "application-docker.yml" Dockerfile >nul
if %errorlevel% equ 0 (
    echo   ✓ Dockerfile包含application-docker.yml配置
) else (
    echo   ✗ 错误: Dockerfile不包含application-docker.yml配置
    goto :error
)

echo.
echo ========================================
echo 配置验证完成！
echo.
echo 下一步:
echo 1. 确保Docker Desktop正在运行
echo 2. 构建镜像: docker build -t shopping-app .
echo 3. 启动服务: docker-compose up
echo ========================================
exit /b 0

:error
echo.
echo ========================================
echo 配置验证失败！
echo 请修复上述错误后重试。
echo ========================================
exit /b 1
