#!/bin/bash
# ===========================================
# JVM Options for Performance Testing & Profiling
# ===========================================

# 프로파일링 출력 디렉토리
PROFILE_DIR="${PROFILE_DIR:-/tmp/bookvoyage-profiling}"
mkdir -p "$PROFILE_DIR"

# 타임스탬프
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# ===========================================
# JVM 기본 옵션
# ===========================================
JVM_BASE_OPTS="
-server
-Xms2g
-Xmx2g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication
"

# ===========================================
# JFR (Java Flight Recorder) 옵션
# ===========================================
JFR_OPTS="
-XX:+UnlockDiagnosticVMOptions
-XX:+DebugNonSafepoints
-XX:StartFlightRecording=duration=0s,filename=${PROFILE_DIR}/bookvoyage_${TIMESTAMP}.jfr,settings=profile,dumponexit=true,name=BookVoyageRecording
"

# ===========================================
# GC 로깅 옵션
# ===========================================
GC_LOG_OPTS="
-Xlog:gc*,gc+heap=debug,gc+phases=debug:file=${PROFILE_DIR}/gc_${TIMESTAMP}.log:time,uptime,level,tags:filecount=5,filesize=100m
-Xlog:safepoint*:file=${PROFILE_DIR}/safepoint_${TIMESTAMP}.log:time,uptime,level,tags
"

# ===========================================
# 메모리 분석 옵션
# ===========================================
MEMORY_OPTS="
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=${PROFILE_DIR}/heapdump_${TIMESTAMP}.hprof
-XX:NativeMemoryTracking=detail
"

# ===========================================
# 모니터링 옵션 (JMX)
# ===========================================
JMX_OPTS="
-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9999
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.local.only=false
-Djava.rmi.server.hostname=localhost
"

# ===========================================
# async-profiler 연동 옵션 (네이티브 에이전트)
# ===========================================
# async-profiler 경로 (설치 후 수정)
ASYNC_PROFILER_PATH="${ASYNC_PROFILER_PATH:-/opt/async-profiler}"
ASYNC_PROFILER_OPTS=""

if [ -f "${ASYNC_PROFILER_PATH}/lib/libasyncProfiler.so" ]; then
    ASYNC_PROFILER_OPTS="-agentpath:${ASYNC_PROFILER_PATH}/lib/libasyncProfiler.so=start,event=cpu,file=${PROFILE_DIR}/cpu_${TIMESTAMP}.jfr"
elif [ -f "${ASYNC_PROFILER_PATH}/lib/libasyncProfiler.dylib" ]; then
    # macOS
    ASYNC_PROFILER_OPTS="-agentpath:${ASYNC_PROFILER_PATH}/lib/libasyncProfiler.dylib=start,event=cpu,file=${PROFILE_DIR}/cpu_${TIMESTAMP}.jfr"
fi

# ===========================================
# 전체 옵션 조합
# ===========================================
export JAVA_OPTS="${JVM_BASE_OPTS} ${JFR_OPTS} ${GC_LOG_OPTS} ${MEMORY_OPTS} ${JMX_OPTS}"

# async-profiler가 있으면 추가
if [ -n "$ASYNC_PROFILER_OPTS" ]; then
    export JAVA_OPTS="${JAVA_OPTS} ${ASYNC_PROFILER_OPTS}"
fi

echo "=== JVM Profiling Options ==="
echo "Profile directory: ${PROFILE_DIR}"
echo ""
echo "JFR recording: ${PROFILE_DIR}/bookvoyage_${TIMESTAMP}.jfr"
echo "GC log: ${PROFILE_DIR}/gc_${TIMESTAMP}.log"
echo "Safepoint log: ${PROFILE_DIR}/safepoint_${TIMESTAMP}.log"
echo ""
echo "JAVA_OPTS:"
echo "$JAVA_OPTS" | tr ' ' '\n' | grep -v '^$'
echo ""
echo "To use these options, source this file:"
echo "  source $0"
echo "  java \$JAVA_OPTS -jar your-app.jar"
