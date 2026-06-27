Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SparkFlow 灵感微光 - 启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "[错误] 未检测到 Java，请安装 JDK 17+" -ForegroundColor Red
    exit 1
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "[错误] 未检测到 Maven，请安装 Maven 3.8+" -ForegroundColor Red
    exit 1
}

if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    Write-Host "[错误] 未检测到 Node.js，请安装 Node.js 18+" -ForegroundColor Red
    exit 1
}

Write-Host "[1/3] 启动后端 Spring Boot (端口 8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\backend'; mvn spring-boot:run"

Write-Host "[2/3] 安装前端依赖..." -ForegroundColor Yellow
Set-Location "$root\frontend"
if (-not (Test-Path "node_modules")) {
    npm install
}

Write-Host "[3/3] 启动前端 Vite (端口 5173)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\frontend'; npm run dev"

Write-Host ""
Write-Host "启动完成！" -ForegroundColor Green
Write-Host "  前端: http://localhost:5173"
Write-Host "  后端: http://localhost:8080/api/health"
