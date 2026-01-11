#!/bin/bash
# Script de scan des images Docker avec Trivy

echo "========================================"
echo "   Scan Trivy des images Docker"
echo "========================================"

# Vérifier si Trivy est installé
if ! command -v trivy &> /dev/null; then
    echo "❌ Trivy n'est pas installé"
    echo "   Installation: https://aquasecurity.github.io/trivy/latest/getting-started/installation/"
    echo ""
    echo "   Sur Windows avec Chocolatey: choco install trivy"
    echo "   Sur macOS avec Homebrew: brew install trivy"
    echo "   Sur Linux: sudo apt-get install trivy"
    exit 1
fi

# Répertoire de sortie
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "${SCRIPT_DIR}")"
OUTPUT_DIR="${PROJECT_ROOT}/devsecops/reports/trivy"

# Créer le répertoire de sortie
mkdir -p "${OUTPUT_DIR}"

# Liste des images à scanner
IMAGES=(
    "product-service:latest"
    "order-service:latest"
    "api-gateway:latest"
    "frontend:latest"
)

# Fonction pour scanner une image
scan_image() {
    local image_name=$1
    local report_name=$(echo "${image_name}" | tr ':' '-')
    
    echo ""
    echo "🔍 Scan de l'image ${image_name}..."
    
    # Vérifier si l'image existe
    if ! docker image inspect "${image_name}" &> /dev/null; then
        echo "⚠️ Image ${image_name} non trouvée. Construction en cours..."
        return 1
    fi
    
    # Scan avec sortie console
    trivy image --severity HIGH,CRITICAL "${image_name}"
    
    # Génération du rapport HTML
    trivy image \
        --format template \
        --template "@contrib/html.tpl" \
        --output "${OUTPUT_DIR}/${report_name}.html" \
        "${image_name}"
    
    # Génération du rapport JSON
    trivy image \
        --format json \
        --output "${OUTPUT_DIR}/${report_name}.json" \
        "${image_name}"
    
    echo "✅ Rapport généré: ${OUTPUT_DIR}/${report_name}.html"
}

# Construire les images si nécessaire
echo "Construction des images Docker..."
docker-compose build

# Scanner chaque image
for image in "${IMAGES[@]}"; do
    scan_image "${image}"
done

# Résumé
echo ""
echo "========================================"
echo "   Scan Trivy terminé"
echo "   Rapports: ${OUTPUT_DIR}"
echo "========================================"

# Afficher les vulnérabilités critiques
echo ""
echo "📊 Résumé des vulnérabilités critiques:"
for image in "${IMAGES[@]}"; do
    report_name=$(echo "${image}" | tr ':' '-')
    if [ -f "${OUTPUT_DIR}/${report_name}.json" ]; then
        critical=$(cat "${OUTPUT_DIR}/${report_name}.json" | grep -c '"Severity": "CRITICAL"' 2>/dev/null || echo "0")
        high=$(cat "${OUTPUT_DIR}/${report_name}.json" | grep -c '"Severity": "HIGH"' 2>/dev/null || echo "0")
        echo "   ${image}: ${critical} CRITICAL, ${high} HIGH"
    fi
done
