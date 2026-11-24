@echo off
title Lou Araya Macrame - APK firmado
cd /d "%~dp0"

echo ==========================================
echo   LOU ARAYA MACRAME - VALIDAR Y COMPILAR
echo ==========================================
echo.

call gradlew.bat testDebugUnitTest
if errorlevel 1 (
    echo.
    echo ERROR: Fallaron las pruebas unitarias.
    pause
    exit /b 1
)

call gradlew.bat assembleRelease
if errorlevel 1 (
    echo.
    echo ERROR: No se pudo generar el APK release.
    echo Revisa Android Studio, Android SDK y keystore.properties.
    pause
    exit /b 1
)

echo.
echo APK generado en:
echo app\build\outputs\apk\release\app-release.apk
echo.
pause
