#!/bin/bash

set -e  # 에러 발생 시 스크립트 즉시 종료

SERVICES=(
  "employee-service"
  "approval-request-service"
  "approval-processing-service"
  "notification-service"
)

PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)

echo "======================================="
echo "   🚀 Starting full MSA deployment"
echo "======================================="

for SERVICE in "${SERVICES[@]}"; do
  echo ""
  echo "======================================="
  echo " ▶ Service: $SERVICE"
  echo "======================================="

  SERVICE_DIR="$PROJECT_ROOT/$SERVICE"
  if [[ ! -d "$SERVICE_DIR" ]]; then
    echo " ❌ Directory not found: $SERVICE_DIR"
    exit 1
  fi

  echo " 🔨 Building Spring Boot JAR..."
  (cd "$SERVICE_DIR" && ./gradlew clean bootJar)

  echo " ✔ $SERVICE build complete."
done

echo ""
echo "======================================="
echo " 🧹 Stopping existing containers..."
echo "======================================="
(cd "$PROJECT_ROOT" && docker compose down)

echo ""
echo "======================================="
echo " 🔧 Building Docker images..."
echo "======================================="
(cd "$PROJECT_ROOT" && docker compose build --no-cache)

echo ""
echo "======================================="
echo " 🚀 Starting containers..."
echo "======================================="
(cd "$PROJECT_ROOT" && docker compose up -d)

echo ""
echo "======================================="
echo " 🎉 ALL SERVICES DEPLOYED SUCCESSFULLY!"
echo "======================================="

docker ps
