#!/bin/bash
# ===========================================
# Full Performance Experiment
# (Gatling + Profiling 동시 실행)
# ===========================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
ROOT_DIR="$(dirname "$PROJECT_DIR")"

# 설정
TARGET_URL="${TARGET_URL:-http://localhost:8080}"
API_PID="${API_PID:-}"
PROFILE_DIR="${PROFILE_DIR:-${PROJECT_DIR}/reports/profiling}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

mkdir -p "$PROFILE_DIR"

echo "============================================"
echo "  BookVoyage Full Performance Experiment"
echo "============================================"
echo ""
echo "Timestamp: $TIMESTAMP"
echo "Target: $TARGET_URL"
echo "Profile dir: $PROFILE_DIR"
echo ""

# API 서버 PID 찾기
if [ -z "$API_PID" ]; then
    echo "Looking for BookVoyage API process..."
    API_PID=$(jps -l 2>/dev/null | grep -E "(bookvoyage|ApiApplication)" | awk '{print $1}' | head -1)

    if [ -z "$API_PID" ]; then
        echo "Warning: Could not find API process automatically"
        echo "Set API_PID environment variable manually if you want profiling"
        echo ""
    else
        echo "Found API process: PID $API_PID"
    fi
fi

# 프로파일링 시작 (백그라운드)
if [ -n "$API_PID" ]; then
    echo ""
    echo "Starting async-profiler in background..."

    # CPU 프로파일링 (전체 테스트 동안)
    nohup "$SCRIPT_DIR/async-profiler.sh" cpu "$API_PID" 2100 > "${PROFILE_DIR}/profiler_cpu.log" 2>&1 &
    PROFILER_CPU_PID=$!

    # 메모리 할당 프로파일링
    nohup "$SCRIPT_DIR/async-profiler.sh" alloc "$API_PID" 2100 > "${PROFILE_DIR}/profiler_alloc.log" 2>&1 &
    PROFILER_ALLOC_PID=$!

    echo "Profiler PIDs: CPU=$PROFILER_CPU_PID, Alloc=$PROFILER_ALLOC_PID"

    # JFR 수동 시작 (이미 JVM 옵션으로 설정되어 있지 않은 경우)
    if command -v jcmd &> /dev/null; then
        echo "Starting JFR recording..."
        jcmd "$API_PID" JFR.start name=PerfTest duration=35m filename="${PROFILE_DIR}/jfr_${TIMESTAMP}.jfr" settings=profile 2>/dev/null || true
    fi
fi

# Gatling 실행
echo ""
echo "Starting Gatling full experiment..."
echo ""

"$SCRIPT_DIR/run-test.sh" full -t "$TARGET_URL"

# 프로파일러 종료 대기
if [ -n "$PROFILER_CPU_PID" ]; then
    echo ""
    echo "Waiting for profilers to finish..."
    wait "$PROFILER_CPU_PID" 2>/dev/null || true
    wait "$PROFILER_ALLOC_PID" 2>/dev/null || true
fi

# JFR 덤프
if [ -n "$API_PID" ] && command -v jcmd &> /dev/null; then
    echo "Dumping JFR recording..."
    jcmd "$API_PID" JFR.dump name=PerfTest 2>/dev/null || true
    jcmd "$API_PID" JFR.stop name=PerfTest 2>/dev/null || true
fi

# 결과 요약
echo ""
echo "============================================"
echo "  Experiment Complete"
echo "============================================"
echo ""
echo "Results:"
echo "  Gatling reports: ${PROJECT_DIR}/build/reports/gatling/"
echo "  Profiling data:  ${PROFILE_DIR}/"
echo ""
echo "Files generated:"
ls -la "$PROFILE_DIR"/ 2>/dev/null || true
echo ""
echo "Next steps:"
echo "  1. Open Gatling HTML report in browser"
echo "  2. Analyze JFR with JDK Mission Control: jmc"
echo "  3. View flame graphs: open ${PROFILE_DIR}/*.html"
