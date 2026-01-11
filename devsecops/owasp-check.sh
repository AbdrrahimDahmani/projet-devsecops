#!/bin/bash
# Script d'analyse des dépendances avec OWASP Dependency-Check

echo "========================================"
echo "   Analyse OWASP Dependency-Check"
echo "========================================"

# Répertoire de sortie
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "${SCRIPT_DIR}")"
OUTPUT_DIR="${PROJECT_ROOT}/devsecops/reports/owasp"

# Créer le répertoire de sortie
mkdir -p "${OUTPUT_DIR}"

# Fonction pour analyser un service
analyze_service() {
    local service_name=$1
    local service_path=$2
    
    echo ""
    echo "🔍 Analyse des dépendances de ${service_name}..."
    
    cd "${service_path}"
    
    mvn dependency-check:check \
        -Dformat=ALL \
        -DowaSpChecksConfig=true \
        -DdataDirectory="${OUTPUT_DIR}/.dependency-check" \
        -DoutputDirectory="${OUTPUT_DIR}/${service_name}" \
        -DfailBuildOnAnyVulnerability=false
    
    if [ $? -eq 0 ]; then
        echo "✅ Analyse de ${service_name} terminée"
        echo "   Rapport: ${OUTPUT_DIR}/${service_name}/dependency-check-report.html"
    else
        echo "⚠️ Vulnérabilités potentielles détectées dans ${service_name}"
    fi
    
    cd - > /dev/null
}

# Analyser chaque service
analyze_service "product-service" "${PROJECT_ROOT}/product-service"
analyze_service "order-service" "${PROJECT_ROOT}/order-service"
analyze_service "api-gateway" "${PROJECT_ROOT}/api-gateway"

echo ""
echo "========================================"
echo "   Analyse OWASP terminée"
echo "   Rapports: ${OUTPUT_DIR}"
echo "========================================"
