@echo off
REM Script d'analyse de sécurité pour Windows

echo ========================================
echo    Analyse de Sécurité DevSecOps
echo ========================================

set PROJECT_ROOT=%~dp0..
set REPORTS_DIR=%PROJECT_ROOT%\devsecops\reports

if not exist "%REPORTS_DIR%" mkdir "%REPORTS_DIR%"

echo.
echo [1/3] OWASP Dependency-Check
echo ----------------------------------------
echo Analyse des dépendances des services...

cd /d "%PROJECT_ROOT%\product-service"
call mvn dependency-check:check -DfailBuildOnAnyVulnerability=false

cd /d "%PROJECT_ROOT%\order-service"
call mvn dependency-check:check -DfailBuildOnAnyVulnerability=false

cd /d "%PROJECT_ROOT%\api-gateway"
call mvn dependency-check:check -DfailBuildOnAnyVulnerability=false

echo.
echo [2/3] Trivy - Scan des images Docker
echo ----------------------------------------

REM Vérifier si Trivy est installé
where trivy >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Trivy n'est pas installe.
    echo Installation: choco install trivy
    goto :sonar
)

docker-compose build

trivy image --severity HIGH,CRITICAL product-service:latest
trivy image --severity HIGH,CRITICAL order-service:latest
trivy image --severity HIGH,CRITICAL api-gateway:latest
trivy image --severity HIGH,CRITICAL frontend:latest

:sonar
echo.
echo [3/3] SonarQube (optionnel)
echo ----------------------------------------
echo Pour l'analyse SonarQube, assurez-vous que le serveur est demarré.
echo URL: http://localhost:9000

echo.
echo ========================================
echo    Analyse terminee
echo ========================================
echo Rapports disponibles dans: %REPORTS_DIR%

cd /d "%PROJECT_ROOT%"
pause
