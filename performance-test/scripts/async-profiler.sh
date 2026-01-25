#!/bin/bash
# ===========================================
# async-profiler 프로파일링 스크립트
# ===========================================
#
# 사전 요구사항:
#   - async-profiler 설치
#     macOS: brew install async-profiler
#     Linux: https://github.com/async-profiler/async-profiler/releases
#
# ===========================================

set -e

# 설정
ASYNC_PROFILER_PATH="${ASYNC_PROFILER_PATH:-/opt/async-profiler}"
PROFILE_DIR="${PROFILE_DIR:-./reports/profiling}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# 설치 경로 탐색
if command -v asprof >/dev/null 2>&1; then
    ASPROF="$(command -v asprof)"
elif [ -f "/opt/homebrew/bin/asprof" ]; then
    ASPROF="/opt/homebrew/bin/asprof"
elif [ -f "/opt/homebrew/opt/async-profiler/bin/asprof" ]; then
    ASPROF="/opt/homebrew/opt/async-profiler/bin/asprof"
elif [ -f "/usr/local/bin/asprof" ]; then
    ASPROF="/usr/local/bin/asprof"
elif [ -f "/usr/local/opt/async-profiler/bin/asprof" ]; then
    ASPROF="/usr/local/opt/async-profiler/bin/asprof"
elif [ -f "${ASYNC_PROFILER_PATH}/bin/asprof" ]; then
    ASPROF="${ASYNC_PROFILER_PATH}/bin/asprof"
else
    echo "Error: async-profiler not found"
    echo "Install with: brew install async-profiler (macOS)"
    echo "Or set ASYNC_PROFILER_PATH to the install directory."
    exit 1
fi

mkdir -p "$PROFILE_DIR"

# 사용법
usage() {
    echo "Usage: $0 <command> [options]"
    echo ""
    echo "Commands:"
    echo "  cpu <pid> [duration]     CPU 프로파일링 (기본 60초)"
    echo "  alloc <pid> [duration]   메모리 할당 프로파일링"
    echo "  lock <pid> [duration]    Lock contention 프로파일링"
    echo "  wall <pid> [duration]    Wall-clock 프로파일링"
    echo "  all <pid> [duration]     모든 이벤트 프로파일링"
    echo "  list                     실행 중인 Java 프로세스 목록"
    echo ""
    echo "Examples:"
    echo "  $0 list                  # Java 프로세스 목록"
    echo "  $0 cpu 12345 120         # PID 12345를 120초간 CPU 프로파일링"
    echo "  $0 alloc 12345           # 메모리 할당 프로파일링 (60초)"
    exit 1
}

# Java 프로세스 목록
list_java() {
    echo "=== Running Java Processes ==="
    jps -l 2>/dev/null || ps aux | grep java | grep -v grep
}

# CPU 프로파일링
profile_cpu() {
    local PID=$1
    local DURATION=${2:-60}
    local OUTPUT="${PROFILE_DIR}/cpu_${PID}_${TIMESTAMP}"

    echo "=== CPU Profiling ==="
    echo "PID: $PID"
    echo "Duration: ${DURATION}s"
    echo "Output: ${OUTPUT}"
    echo ""

    $ASPROF -d "$DURATION" \
        -e cpu \
        -o flat,traces=200 \
        -f "${OUTPUT}.txt" \
        "$PID"

    # Flame graph 생성
    $ASPROF -d "$DURATION" \
        -e cpu \
        -o flamegraph \
        -f "${OUTPUT}.html" \
        "$PID"

    # JFR 포맷으로도 저장 (JMC에서 분석 가능)
    $ASPROF -d "$DURATION" \
        -e cpu \
        -o jfr \
        -f "${OUTPUT}.jfr" \
        "$PID"

    echo ""
    echo "Results:"
    echo "  Text: ${OUTPUT}.txt"
    echo "  Flamegraph: ${OUTPUT}.html"
    echo "  JFR: ${OUTPUT}.jfr"
}

# 메모리 할당 프로파일링
profile_alloc() {
    local PID=$1
    local DURATION=${2:-60}
    local OUTPUT="${PROFILE_DIR}/alloc_${PID}_${TIMESTAMP}"

    echo "=== Allocation Profiling ==="
    echo "PID: $PID"
    echo "Duration: ${DURATION}s"
    echo ""

    $ASPROF -d "$DURATION" \
        -e alloc \
        -o flat,traces=200 \
        -f "${OUTPUT}.txt" \
        "$PID"

    $ASPROF -d "$DURATION" \
        -e alloc \
        -o flamegraph \
        -f "${OUTPUT}.html" \
        "$PID"

    echo ""
    echo "Results:"
    echo "  Text: ${OUTPUT}.txt"
    echo "  Flamegraph: ${OUTPUT}.html"
}

# Lock contention 프로파일링
profile_lock() {
    local PID=$1
    local DURATION=${2:-60}
    local OUTPUT="${PROFILE_DIR}/lock_${PID}_${TIMESTAMP}"

    echo "=== Lock Contention Profiling ==="
    echo "PID: $PID"
    echo "Duration: ${DURATION}s"
    echo ""

    $ASPROF -d "$DURATION" \
        -e lock \
        -o flat,traces=200 \
        -f "${OUTPUT}.txt" \
        "$PID"

    $ASPROF -d "$DURATION" \
        -e lock \
        -o flamegraph \
        -f "${OUTPUT}.html" \
        "$PID"

    echo ""
    echo "Results:"
    echo "  Text: ${OUTPUT}.txt"
    echo "  Flamegraph: ${OUTPUT}.html"
}

# Wall-clock 프로파일링 (대기 시간 포함)
profile_wall() {
    local PID=$1
    local DURATION=${2:-60}
    local OUTPUT="${PROFILE_DIR}/wall_${PID}_${TIMESTAMP}"

    echo "=== Wall-clock Profiling ==="
    echo "PID: $PID"
    echo "Duration: ${DURATION}s"
    echo ""

    $ASPROF -d "$DURATION" \
        -e wall \
        -o flat,traces=200 \
        -f "${OUTPUT}.txt" \
        "$PID"

    $ASPROF -d "$DURATION" \
        -e wall \
        -o flamegraph \
        -f "${OUTPUT}.html" \
        "$PID"

    echo ""
    echo "Results:"
    echo "  Text: ${OUTPUT}.txt"
    echo "  Flamegraph: ${OUTPUT}.html"
}

# 종합 프로파일링 (모든 이벤트)
profile_all() {
    local PID=$1
    local DURATION=${2:-60}

    echo "=== Comprehensive Profiling ==="
    echo "PID: $PID"
    echo "Duration: ${DURATION}s per event"
    echo ""

    profile_cpu "$PID" "$DURATION"
    echo ""
    profile_alloc "$PID" "$DURATION"
    echo ""
    profile_lock "$PID" "$DURATION"
}

# 메인
case "$1" in
    cpu)
        [ -z "$2" ] && usage
        profile_cpu "$2" "$3"
        ;;
    alloc)
        [ -z "$2" ] && usage
        profile_alloc "$2" "$3"
        ;;
    lock)
        [ -z "$2" ] && usage
        profile_lock "$2" "$3"
        ;;
    wall)
        [ -z "$2" ] && usage
        profile_wall "$2" "$3"
        ;;
    all)
        [ -z "$2" ] && usage
        profile_all "$2" "$3"
        ;;
    list)
        list_java
        ;;
    *)
        usage
        ;;
esac
