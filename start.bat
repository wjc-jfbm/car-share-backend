@echo off
chcp 65001 >nul

:: 添加 Node.js 到 PATH
set "PATH=D:\Node.js;%PATH%"

title 拼车协作平台 - 启动面板

echo.
echo   ╔══════════════════════════════════════════╗
echo   ║      拼车协作平台 — 一键启动             ║
echo   ╚══════════════════════════════════════════╝
echo.
echo   [1] 启动全部（后端 + 管理后台）
echo   [2] 仅启动后端 (端口 8081)
echo   [3] 仅启动管理后台 (端口 8082)
echo   [4] 检查端口占用
echo   [0] 退出
echo.
set /p choice="请输入选项 (0-4): "

if "%choice%"=="1" goto START_ALL
if "%choice%"=="2" goto START_BACKEND
if "%choice%"=="3" goto START_ADMIN
if "%choice%"=="4" goto CHECK_PORTS
if "%choice%"=="0" goto EXIT
echo 无效选项，请重新输入
goto END

:START_ALL
    call :START_BACKEND_FUNC
    timeout /t 5 /nobreak >nul
    call :START_ADMIN_FUNC
    goto END

:START_BACKEND
    call :START_BACKEND_FUNC
    goto END

:START_ADMIN
    call :START_ADMIN_FUNC
    goto END

:CHECK_PORTS
    echo.
    echo 检查端口占用情况...
    echo ──────────────────────────────────────
    netstat -ano | findstr ":8081" | findstr "LISTENING" >nul && echo   [占用] 8081 (后端) || echo   [空闲] 8081 (后端)
    netstat -ano | findstr ":8082" | findstr "LISTENING" >nul && echo   [占用] 8082 (管理后台) || echo   [空闲] 8082 (管理后台)
    netstat -ano | findstr ":3306" | findstr "LISTENING" >nul && echo   [占用] 3306 (MySQL) || echo   [空闲] 3306 (MySQL)
    netstat -ano | findstr ":6379" | findstr "LISTENING" >nul && echo   [占用] 6379 (Redis) || echo   [空闲] 6379 (Redis)
    echo ──────────────────────────────────────
    echo.
    pause
    goto END

:START_BACKEND_FUNC
    echo.
    echo [后端] 启动 Spring Boot 后端 (端口 8081)...
    start "拼车后端" cmd /c "cd /d d:\project\cs\car-share-backend && call mvn spring-boot:run -Dmaven.repo.local=.m2/repository"
    echo [后端] 已在新窗口启动，等待编译中...
    goto :EOF

:START_ADMIN_FUNC
    echo.
    echo [管理后台] 启动 Vue 管理后台 (端口 8082)...
    if not exist "d:\project\cs\car-share-backend\admin-ui\node_modules" (
        echo [管理后台] 首次启动，正在安装依赖...
        cd /d "d:\project\cs\car-share-backend\admin-ui"
        call npm install --registry=https://registry.npmmirror.com
    )
    start "拼车管理后台" cmd /c "cd /d d:\project\cs\car-share-backend\admin-ui && npm run dev -- --port 8082"
    echo [管理后台] 已在新窗口启动
    goto :EOF

:EXIT
    exit /b 0

:END
    echo.
    echo 所有服务启动后:
    echo   管理后台: http://localhost:8082
    echo   后端 API:  http://localhost:8081
    echo.
    pause
