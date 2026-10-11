@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo === 购物系统部署脚本 ===
echo.

REM 检查 Docker 是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Docker 未安装
    echo 请先安装 Docker Desktop: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

REM 检查 Docker Compose 是否安装
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Docker Compose 未安装
    echo 请先安装 Docker Compose
    pause
    exit /b 1
)

echo ✓ Docker 环境检查通过

REM 创建部署目录
set DEPLOY_DIR=shopping-system-deploy
if exist "%DEPLOY_DIR%" (
    echo 检测到现有部署目录，将进行更新...
) else (
    echo 创建部署目录: %DEPLOY_DIR%
    mkdir "%DEPLOY_DIR%"
)

REM 复制Docker配置文件到部署目录
echo 复制Docker配置文件...
copy docker-compose.yml "%DEPLOY_DIR%\"
copy Dockerfile "%DEPLOY_DIR%\"
copy .dockerignore "%DEPLOY_DIR%\"

cd /d "%DEPLOY_DIR%"

echo.
echo 正在启动购物系统服务...

REM 检查是否以后台模式启动
if "%1"=="-d" (
    echo 以后台模式启动服务...
    docker-compose up -d
) else if "%1"=="--detach" (
    echo 以后台模式启动服务...
    docker-compose up -d
) else (
    echo 以前台模式启动服务 (按 Ctrl+C 停止)...
    docker-compose up
)

echo.
echo === 部署完成 ===
echo 应用地址: http://localhost:8080
echo 数据库端口: 3307
echo.
echo 管理命令:
echo   - 查看状态: docker-compose ps
echo   - 查看日志: docker-compose logs
echo   - 停止服务: docker-compose down
echo   - 重启服务: docker-compose restart

pause
