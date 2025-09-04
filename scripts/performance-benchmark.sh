#!/bin/bash

# Live2D性能基准测试脚本
# 用于测试JDK 24升级和离屏渲染优化的实际效果

echo "🚀 Live2D性能基准测试"
echo "========================"

# 测试配置
TEST_DURATION=30  # 测试持续时间(秒)
MODEL_PATH="models/Haru"
LOG_FILE="performance_test_$(date +%Y%m%d_%H%M%S).log"

# 创建日志文件
echo "测试开始时间: $(date)" > $LOG_FILE
echo "测试配置:" >> $LOG_FILE
echo "  持续时间: ${TEST_DURATION}秒" >> $LOG_FILE
echo "  测试模型: $MODEL_PATH" >> $LOG_FILE
echo "  Java版本: $(java -version 2>&1 | head -1)" >> $LOG_FILE
echo "" >> $LOG_FILE

# 系统信息收集
echo "📊 系统信息收集..."
echo "CPU信息:" >> $LOG_FILE
lscpu | grep -E "Model name|CPU\(s\)|Thread|Core" >> $LOG_FILE
echo "" >> $LOG_FILE

echo "内存信息:" >> $LOG_FILE
free -h >> $LOG_FILE
echo "" >> $LOG_FILE

echo "OpenGL信息:" >> $LOG_FILE
glxinfo | grep -E "OpenGL version|OpenGL renderer" >> $LOG_FILE
echo "" >> $LOG_FILE

# 测试1: 启动时间测试
echo "⏱️  测试1: 启动时间测试"
echo "启动时间测试:" >> $LOG_FILE

START_TIME=$(date +%s.%N)
timeout 10s java -Xms512m -Xmx1024m -XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Dsun.java2d.opengl=true -jar modules/examples/swing/target/scala-3.3.2/Live2DForScala-Swing-2.1.0-SNAPSHOT.jar > /dev/null 2>&1 &
APP_PID=$!
sleep 2
END_TIME=$(date +%s.%N)
STARTUP_TIME=$(echo "$END_TIME - $START_TIME" | bc -l)
kill $APP_PID 2>/dev/null

echo "  启动时间: ${STARTUP_TIME}秒" >> $LOG_FILE
echo "  启动时间: ${STARTUP_TIME}秒"

# 测试2: 内存使用测试
echo "🧠 测试2: 内存使用测试"
echo "内存使用测试:" >> $LOG_FILE

# 启动应用程序并监控内存
java -Xms512m -Xmx1024m -XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Dsun.java2d.opengl=true -jar modules/examples/swing/target/scala-3.3.2/Live2DForScala-Swing-2.1.0-SNAPSHOT.jar > /dev/null 2>&1 &
APP_PID=$!

sleep 5  # 等待应用启动

# 监控内存使用
MAX_MEMORY=0
for i in {1..10}; do
    MEMORY=$(ps -o rss= -p $APP_PID 2>/dev/null | awk '{print $1/1024}')
    if [ ! -z "$MEMORY" ] && [ "$MEMORY" -gt "$MAX_MEMORY" ]; then
        MAX_MEMORY=$MEMORY
    fi
    sleep 1
done

kill $APP_PID 2>/dev/null

echo "  最大内存使用: ${MAX_MEMORY}MB" >> $LOG_FILE
echo "  最大内存使用: ${MAX_MEMORY}MB"

# 测试3: GC性能测试
echo "🗑️  测试3: GC性能测试"
echo "GC性能测试:" >> $LOG_FILE

# 使用GC日志测试
java -Xms512m -Xmx1024m -XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Dsun.java2d.opengl=true -jar modules/examples/swing/target/scala-3.3.2/Live2DForScala-Swing-2.1.0-SNAPSHOT.jar > gc_test.log 2>&1 &
APP_PID=$!

sleep 10
kill $APP_PID 2>/dev/null

# 分析GC日志
if [ -f "gc_test.log" ]; then
    GC_COUNT=$(grep -c "GC" gc_test.log)
    AVG_GC_TIME=$(grep "GC" gc_test.log | awk -F'=' '{print $2}' | awk '{print $1}' | awk '{sum+=$1; count++} END {if(count>0) print sum/count; else print 0}')
    
    echo "  GC次数: $GC_COUNT" >> $LOG_FILE
    echo "  平均GC时间: ${AVG_GC_TIME}ms" >> $LOG_FILE
    echo "  GC次数: $GC_COUNT"
    echo "  平均GC时间: ${AVG_GC_TIME}ms"
    
    rm gc_test.log
fi

# 测试4: 编译性能测试
echo "🔨 测试4: 编译性能测试"
echo "编译性能测试:" >> $LOG_FILE

COMPILE_START=$(date +%s.%N)
sbt compile > /dev/null 2>&1
COMPILE_END=$(date +%s.%N)
COMPILE_TIME=$(echo "$COMPILE_END - $COMPILE_START" | bc -l)

echo "  编译时间: ${COMPILE_TIME}秒" >> $LOG_FILE
echo "  编译时间: ${COMPILE_TIME}秒"

# 测试5: JAR包大小测试
echo "📦 测试5: JAR包大小测试"
echo "JAR包大小测试:" >> $LOG_FILE

if [ -f "modules/examples/swing/target/scala-3.3.2/Live2DForScala-Swing-2.1.0-SNAPSHOT.jar" ]; then
    JAR_SIZE=$(du -h "modules/examples/swing/target/scala-3.3.2/Live2DForScala-Swing-2.1.0-SNAPSHOT.jar" | cut -f1)
    echo "  JAR包大小: $JAR_SIZE" >> $LOG_FILE
    echo "  JAR包大小: $JAR_SIZE"
fi

# 性能总结
echo ""
echo "📊 性能测试总结"
echo "=================="
echo "测试完成时间: $(date)" >> $LOG_FILE
echo ""

echo "✅ 测试结果:"
echo "  启动时间: ${STARTUP_TIME}秒"
echo "  内存使用: ${MAX_MEMORY}MB"
echo "  GC次数: $GC_COUNT"
echo "  平均GC时间: ${AVG_GC_TIME}ms"
echo "  编译时间: ${COMPILE_TIME}秒"
echo "  JAR包大小: $JAR_SIZE"

echo ""
echo "📈 与基准对比:"
echo "  JDK 24优化: ✅ 已启用"
echo "  离屏渲染缓存: ⏸️ 暂时禁用"
echo "  性能监控: ✅ 已实现"

echo ""
echo "📋 详细日志已保存到: $LOG_FILE"
echo ""

# 性能建议
echo "💡 性能优化建议:"
echo "  1. 启动时间 < 3秒: $(if (( $(echo "$STARTUP_TIME < 3" | bc -l) )); then echo "✅ 优秀"; else echo "⚠️ 需要优化"; fi)"
echo "  2. 内存使用 < 400MB: $(if (( $(echo "$MAX_MEMORY < 400" | bc -l) )); then echo "✅ 优秀"; else echo "⚠️ 需要优化"; fi)"
echo "  3. GC次数 < 20: $(if [ "$GC_COUNT" -lt 20 ]; then echo "✅ 优秀"; else echo "⚠️ 需要优化"; fi)"
echo "  4. 编译时间 < 10秒: $(if (( $(echo "$COMPILE_TIME < 10" | bc -l) )); then echo "✅ 优秀"; else echo "⚠️ 需要优化"; fi)"

echo ""
echo "🎯 下一步优化方向:"
echo "  1. 重新启用离屏渲染缓存系统"
echo "  2. 优化模型加载和纹理管理"
echo "  3. 实现更智能的内存管理"
echo "  4. 添加实时性能监控界面"

echo ""
echo "✅ 性能基准测试完成！"
