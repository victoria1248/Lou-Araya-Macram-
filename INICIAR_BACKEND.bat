@echo off
title Lou Araya Macrame - Backend
cd /d "%~dp0backend"

docker compose up -d --build
if errorlevel 1 (
    echo No se pudo iniciar el backend. Verifica que Docker Desktop este abierto.
    pause
    exit /b 1
)

echo.
echo Backend iniciado en http://localhost:8080
echo Swagger: http://localhost:8080/swagger-ui.html
pause
