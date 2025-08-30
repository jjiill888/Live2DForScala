#!/bin/bash

# Live2DForScala 优化启动脚本
# 启动速度优化版本

# 检测系统环境
if [ "$XDG_SESSION_TYPE" = "wayland" ] || [ -n "$WAYLAND_DISPLAY" ]; then
  export GDK_BACKEND=x11
fi

# 检测Java版本
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "21" ]; then
  echo "警告: 检测到Java版本 $JAVA_VERSION，建议使用Java 21以获得最佳性能"
fi

# 检测系统内存
TOTAL_MEM=$(free -m | awk 'NR==2{printf "%.0f", $2}')
if [ "$TOTAL_MEM" -gt 8192 ]; then
  HEAP_SIZE="1024m"
  MAX_HEAP="2048m"
elif [ "$TOTAL_MEM" -gt 4096 ]; then
  HEAP_SIZE="512m"
  MAX_HEAP="1024m"
else
  HEAP_SIZE="256m"
  MAX_HEAP="512m"
fi

# 检测CPU核心数
CPU_CORES=$(nproc)
if [ "$CPU_CORES" -gt 4 ]; then
  GC_THREADS=$((CPU_CORES / 2))
else
  GC_THREADS=2
fi

# 优化JVM参数
JVM_OPTS=(
  "-Xms${HEAP_SIZE}"
  "-Xmx${MAX_HEAP}"
  "-XX:+UseG1GC"
  "-XX:G1HeapRegionSize=16m"
  "-XX:MaxGCPauseMillis=200"
  "-XX:+UseStringDeduplication"
  "-XX:+UseCompressedOops"
  "-XX:+TieredCompilation"
  "-XX:TieredStopAtLevel=1"
  "-Dsun.java2d.opengl=true"
  "-Dfile.encoding=UTF-8"
)

# 启动应用
echo "启动Live2DForScala (优化版本)..."
echo "内存配置: ${HEAP_SIZE} -> ${MAX_HEAP}"
echo "GC线程数: ${GC_THREADS}"

exec java "${JVM_OPTS[@]}" -jar Live2DForScala-SWT-Linux-2.1.0-SNAPSHOT.jar "$@"
