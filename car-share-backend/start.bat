@echo off
chcp 65001 >nul
echo ========================================
echo   拼车共享管理系统 - 后端启动脚本
echo ========================================

set PORT=8081

:CHECK_PORT
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%PORT% ^| findstr LISTENING 2^>nul') do (
    echo [INFO] 检测到端口 %PORT% 被进程 %%a 占用，正在终止...
    taskkill /PID %%a /F >nul 2>&1
    if errorlevel 1 (
        echo [WARN] 无法终止进程 %%a，可能需要管理员权限
    ) else (
        echo [INFO] 已终止进程 %%a
    )
)

timeout /t 2 /nobreak >nul

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%PORT% ^| findstr LISTENING 2^>nul') do (
    echo [WARN] 端口 %PORT% 仍被占用，等待释放...
    timeout /t 3 /nobreak >nul
    goto CHECK_PORT
)

echo [INFO] 端口 %PORT% 可用，正在启动后端服务...
call mvn spring-boot:run "-Dmaven.repo.local=.m2/repository"
