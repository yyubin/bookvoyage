#!/bin/bash
# ===========================================
# Recommendation API Max RPS Test
# ===========================================

set -e

TARGET_URL="${TARGET_URL:-http://localhost:8080}"
BASE_RPS="${BASE_RPS:-20}"
DURATION="${DURATION:-960}" # 16분

echo "============================================"
echo " Recommendation API - Max RPS Test"
echo "============================================"
echo ""
echo "Target URL : $TARGET_URL"
echo "Base RPS   : $BASE_RPS"
echo "Duration   : ${DURATION}s"
echo ""

echo "Checking server..."
curl -sf "$TARGET_URL/api/recommendations/books" > /dev/null || {
  echo "Server not responding"
  exit 1
}

echo ""
echo "Starting Gatling..."
echo ""

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(dirname "$(dirname "$SCRIPT_DIR")")"

cd "$ROOT_DIR"
./gradlew :performance-test:gatlingRun \
  --simulation bookvoyage.simulations.RecommendationMaxRpsSimulation \
  -Dgatling.baseUrl="$TARGET_URL" \
  -Dgatling.users="$BASE_RPS" \
  -Dgatling.duration="$DURATION"

echo ""
echo "============================================"
echo " Test Finished"
echo "============================================"
