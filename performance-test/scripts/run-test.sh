#!/bin/bash
# ===========================================
# Gatling Performance Test Runner
# ===========================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
ROOT_DIR="$(dirname "$PROJECT_DIR")"

cd "$ROOT_DIR"

# 기본 설정
TARGET_URL="${TARGET_URL:-http://localhost:8080}"
GATLING_USERS="${GATLING_USERS:-50}"
GATLING_DURATION="${GATLING_DURATION:-600}"
GATLING_RAMP_UP="${GATLING_RAMP_UP:-60}"

# 사용법
usage() {
    echo "Usage: $0 <simulation> [options]"
    echo ""
    echo "Simulations:"
    echo "  baseline      Baseline 테스트 (사용자 부하만)"
    echo "  batch         Batch + 사용자 부하 테스트"
    echo "  spike         Spike 테스트 (부하 급증)"
    echo "  full          전체 실험 (35분)"
    echo "  reco-stress   추천 API 스트레스 테스트 (16분)"
    echo ""
    echo "Options:"
    echo "  -u, --users N       동시 사용자 수 (기본: $GATLING_USERS)"
    echo "  -d, --duration N    테스트 시간 초 (기본: $GATLING_DURATION)"
    echo "  -r, --ramp-up N     램프업 시간 초 (기본: $GATLING_RAMP_UP)"
    echo "  -t, --target URL    대상 서버 URL (기본: $TARGET_URL)"
    echo "  -h, --help          도움말"
    echo ""
    echo "Examples:"
    echo "  $0 baseline"
    echo "  $0 batch -u 200 -d 900"
    echo "  $0 full -t https://api.example.com"
    exit 1
}

# 인자 파싱
SIMULATION=""
while [[ $# -gt 0 ]]; do
    case $1 in
        baseline)
            SIMULATION="bookvoyage.simulations.BaselineSimulation"
            shift
            ;;
        batch)
            SIMULATION="bookvoyage.simulations.BatchWithLoadSimulation"
            shift
            ;;
        spike)
            SIMULATION="bookvoyage.simulations.SpikeTestSimulation"
            shift
            ;;
        full)
            SIMULATION="bookvoyage.simulations.FullExperimentSimulation"
            shift
            ;;
        reco-stress|recommendation-stress)
            SIMULATION="bookvoyage.simulations.RecommendationMaxRpsSimulation"
            shift
            ;;
        -u|--users)
            GATLING_USERS="$2"
            shift 2
            ;;
        -d|--duration)
            GATLING_DURATION="$2"
            shift 2
            ;;
        -r|--ramp-up)
            GATLING_RAMP_UP="$2"
            shift 2
            ;;
        -t|--target)
            TARGET_URL="$2"
            shift 2
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo "Unknown option: $1"
            usage
            ;;
    esac
done

[ -z "$SIMULATION" ] && usage

# 환경변수 설정
export TARGET_URL
export GATLING_USERS
export GATLING_DURATION
export GATLING_RAMP_UP

echo "============================================"
echo "  BookVoyage Gatling Performance Test"
echo "============================================"
echo ""
echo "Simulation: $SIMULATION"
echo "Target URL: $TARGET_URL"
echo "Users: $GATLING_USERS"
echo "Duration: ${GATLING_DURATION}s"
echo "Ramp-up: ${GATLING_RAMP_UP}s"
echo ""
echo "Starting in 5 seconds..."
sleep 5

# Gatling 실행 (루트 프로젝트에서)
./gradlew :performance-test:gatlingRun --simulation "$SIMULATION"

echo ""
echo "============================================"
echo "  Test Complete"
echo "============================================"
echo ""
echo "Reports: ${PROJECT_DIR}/build/reports/gatling/"
