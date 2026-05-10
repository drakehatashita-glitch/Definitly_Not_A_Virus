@echo off
title Building "Definitly not a virus" Minecraft Mod
echo.
echo ============================================
echo  Building: Definitly not a virus mod
echo ============================================
echo.

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found!
    echo.
    echo Please install Java 21 from:
    echo   https://adoptium.net/temurin/releases/?version=21
    echo.
    echo After installing, close and re-open this window.
    pause
    exit /b 1
)

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
echo Java found: %JAVA_VERSION%
echo.

if not exist "gradlew.bat" (
    echo Gradle wrapper not found. Downloading...
    where gradle >nul 2>&1
    if %errorlevel% neq 0 (
        echo ERROR: Neither gradlew nor gradle found.
        echo.
        echo Please install Gradle from:
        echo   https://gradle.org/install/
        echo Then run:  gradle wrapper --gradle-version 8.8
        pause
        exit /b 1
    )
    gradle wrapper --gradle-version 8.8
)

echo Running build (this will take several minutes on first run)...
echo Forge is downloading Minecraft files - please wait.
echo.

call gradlew.bat build

if %errorlevel% neq 0 (
    echo.
    echo BUILD FAILED. Check the output above for errors.
    pause
    exit /b 1
)

echo.
echo ============================================
echo  BUILD SUCCESSFUL!
echo ============================================
echo.
echo Your mod jar is ready:
echo.

for %%f in (build\libs\Definitly not a virus-*.jar) do (
    echo   %%f
    copy "%%f" "%~dp0Definitly not a virus.jar" >nul
    echo.
    echo Copied to: %~dp0Definitly not a virus.jar
)

echo.
echo Drop "Definitly not a virus.jar" into your .minecraft\mods\ folder.
echo.
pause
