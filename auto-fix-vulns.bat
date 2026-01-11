@echo off
REM ==========================================
REM   AUTO FIX VULNERABILITÉS MAVEN + TRIVY
REM ==========================================

REM --- Product Service ---
echo [1/6] Mise à jour des dépendances product-service...
docker run --rm -v "%cd%\product-service:/app" -w /app maven:3.9-eclipse-temurin-17 mvn versions:use-latest-releases -Dincludes='*:*' -DgenerateBackupPoms=false

echo [2/6] Installation des dépendances product-service...
docker run --rm -v "%cd%\product-service:/app" -w /app maven:3.9-eclipse-temurin-17 mvn clean install -DskipTests

REM --- Order Service ---
echo [3/6] Mise à jour des dépendances order-service...
docker run --rm -v "%cd%\order-service:/app" -w /app maven:3.9-eclipse-temurin-17 mvn versions:use-latest-releases -Dincludes='*:*' -DgenerateBackupPoms=false

echo [4/6] Installation des dépendances order-service...
docker run --rm -v "%cd%\order-service:/app" -w /app maven:3.9-eclipse-temurin-17 mvn clean install -DskipTests

REM --- Scan Trivy ---
echo [5/6] Scan Trivy product-service...
docker run --rm -v "%cd%\product-service:/project" -v "%cd%\docs\trivy-reports:/reports" aquasec/trivy:latest fs /project --scanners vuln --format json --output /reports/product-service-trivy.json

echo [6/6] Scan Trivy order-service...
docker run --rm -v "%cd%\order-service:/project" -v "%cd%\docs\trivy-reports:/reports" aquasec/trivy:latest fs /project --scanners vuln --format json --output /reports/order-service-trivy.json

echo.
echo ==========================================
echo   MISE À JOUR & SCAN TERMINÉS !
echo ==========================================
echo Consulte les nouveaux rapports dans docs\trivy-reports\
pause
