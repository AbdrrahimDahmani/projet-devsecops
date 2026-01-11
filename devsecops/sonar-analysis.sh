#!/bin/bash
# Script d'analyse de sécurité avec SonarQube

echo "========================================"
echo "   Analyse SonarQube des services"
echo "========================================"

# Configuration SonarQube
SONAR_HOST_URL="${SONAR_HOST_URL:-http://localhost:9000}"
SONAR_TOKEN="${SONAR_TOKEN:-}"

# Vérifier si SonarQube est accessible
echo "Vérification de la connexion à SonarQube..."
if ! curl -s "${SONAR_HOST_URL}/api/system/status" | grep -q "UP"; then
    echo "❌ SonarQube n'est pas accessible sur ${SONAR_HOST_URL}"
    echo "   Démarrez SonarQube avec: docker-compose -f docker-compose.devsecops.yml up -d sonarqube"
    exit 1
fi

echo "✅ SonarQube est accessible"

# Fonction pour analyser un service
analyze_service() {
    local service_name=$1
    local service_path=$2
    
    echo ""
    echo "📊 Analyse de ${service_name}..."
    
    cd "${service_path}"
    
    if [ -n "${SONAR_TOKEN}" ]; then
        mvn sonar:sonar \
            -Dsonar.projectKey="${service_name}" \
            -Dsonar.projectName="${service_name}" \
            -Dsonar.host.url="${SONAR_HOST_URL}" \
            -Dsonar.token="${SONAR_TOKEN}"
    else
        mvn sonar:sonar \
            -Dsonar.projectKey="${service_name}" \
            -Dsonar.projectName="${service_name}" \
            -Dsonar.host.url="${SONAR_HOST_URL}"
    fi
    
    if [ $? -eq 0 ]; then
        echo "✅ Analyse de ${service_name} terminée"
    else
        echo "❌ Échec de l'analyse de ${service_name}"
    fi
    
    cd - > /dev/null
}

# Analyser chaque service
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "${SCRIPT_DIR}")"

analyze_service "product-service" "${PROJECT_ROOT}/product-service"
analyze_service "order-service" "${PROJECT_ROOT}/order-service"
analyze_service "api-gateway" "${PROJECT_ROOT}/api-gateway"

echo ""
echo "========================================"
echo "   Analyse terminée"
echo "   Résultats: ${SONAR_HOST_URL}/projects"
echo "========================================"
