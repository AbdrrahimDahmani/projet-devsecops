@echo off
echo ==========================================
echo        TRIVY SECURITY SCANNER
echo ==========================================
echo.

echo [1/3] Scanning product-service dependencies...
echo ------------------------------------------
docker run --rm -v "%cd%\product-service:/project" aquasec/trivy:latest fs /project --scanners vuln --severity CRITICAL,HIGH,MEDIUM,LOW

echo.
echo [2/3] Scanning order-service dependencies...
echo ------------------------------------------
docker run --rm -v "%cd%\order-service:/project" aquasec/trivy:latest fs /project --scanners vuln --severity CRITICAL,HIGH,MEDIUM,LOW

echo.
echo [3/3] Saving JSON reports...
echo ------------------------------------------
docker run --rm -v "%cd%\product-service:/project" -v "%cd%\docs\trivy-reports:/reports" aquasec/trivy:latest fs /project --scanners vuln --format json --output /reports/product-service-trivy.json
docker run --rm -v "%cd%\order-service:/project" -v "%cd%\docs\trivy-reports:/reports" aquasec/trivy:latest fs /project --scanners vuln --format json --output /reports/order-service-trivy.json

echo.
echo ==========================================
echo        SCAN COMPLETE!
echo ==========================================
echo Reports saved in: docs\trivy-reports\
echo.
pause
