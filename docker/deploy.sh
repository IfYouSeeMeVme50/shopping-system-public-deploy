#!/bin/bash

# 购物系统快速部署脚本
# 适用于 Linux/macOS 系统

set -e

echo "=== 购物系统部署脚本 ==="
echo

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker 未安装"
    echo "请先安装 Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "错误: Docker Compose 未安装"
    echo "请先安装 Docker Compose: https://docs.docker.com/compose/install/"
    exit 1
fi

echo "✓ Docker 环境检查通过"

# 创建部署目录
DEPLOY_DIR="shopping-system-deploy"
if [ -d "$DEPLOY_DIR" ]; then
    echo "检测到现有部署目录，将进行更新..."
else
    echo "创建部署目录: $DEPLOY_DIR"
    mkdir -p "$DEPLOY_DIR"
fi

# 复制Docker配置文件到部署目录
echo "复制Docker配置文件..."
cp docker-compose.yml "$DEPLOY_DIR/"
cp Dockerfile "$DEPLOY_DIR/"
cp .dockerignore "$DEPLOY_DIR/"

cd "$DEPLOY_DIR"

echo
echo "正在启动购物系统服务..."

# 启动服务
if [ "$1" = "-d" ] || [ "$1" = "--detach" ]; then
    echo "以后台模式启动服务..."
    docker-compose up -d
else
    echo "以前台模式启动服务 (按 Ctrl+C 停止)..."
    docker-compose up
fi

echo
echo "=== 部署完成 ==="
echo "应用地址: http://localhost:8080"
echo "数据库端口: 3307"
echo
echo "管理命令:"
echo "  - 查看状态: docker-compose ps"
echo "  - 查看日志: docker-compose logs"
echo "  - 停止服务: docker-compose down"
echo "  - 重启服务: docker-compose restart"
