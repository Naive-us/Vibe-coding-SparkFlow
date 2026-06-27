@echo off
echo ========================================
echo   SparkFlow 灵感微光 - 启动脚本
echo ========================================
echo.

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java，请安装 JDK 17+
    exit /b 1
)

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Maven，请安装 Maven 3.8+
    exit /b 1
)

where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Node.js，请安装 Node.js 18+
    exit /b 1
)

echo [1/3] 启动后端 Spring Boot (端口 8080)...
start "SparkFlow Backend" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"

echo [2/3] 安装前端依赖...
cd /d %~dp0frontend
if not exist node_modules (
    call npm install
)

echo [3/3] 启动前端 Vite (端口 5173)...
start "SparkFlow Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo 启动完成！
echo   前端: http://localhost:5173
echo   后端: http://localhost:8080/api/health
echo.
pause
