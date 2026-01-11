@echo off
echo ==========================================
echo        SONARQUBE ANALYSIS SCRIPT
echo ==========================================
echo.

echo [1/2] Analyzing product-service...
echo ------------------------------------------
docker run --rm ^
  --network project_system_distribuer_ecommerce-network ^
  -v "%cd%\product-service:/app" ^
  -w /app ^
  maven:3.9-eclipse-temurin-17 ^
  mvn sonar:sonar ^
    -Dsonar.host.url=http://sonarqube:9000 ^
    -Dsonar.token=squ_43d75e12aa2d65bc70cdbb067c68fa6b6f7a23c8 ^
    -Dsonar.projectKey=product-service

echo.
echo [2/2] Analyzing order-service...
echo ------------------------------------------
docker run --rm ^
  --network project_system_distribuer_ecommerce-network ^
  -v "%cd%\order-service:/app" ^
  -w /app ^
  maven:3.9-eclipse-temurin-17 ^
  mvn sonar:sonar ^
    -Dsonar.host.url=http://sonarqube:9000 ^
    -Dsonar.token=squ_43d75e12aa2d65bc70cdbb067c68fa6b6f7a23c8 ^
    -Dsonar.projectKey=order-service

echo.
echo ==========================================
echo        ANALYSIS COMPLETE!
echo ==========================================
echo.
pause